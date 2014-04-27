package com.cs5412.webservices.ml.wsd;

import java.io.BufferedWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.cs5412.filesystem.IFileSystem;
import com.cs5412.taskmanager.TaskDao;
import com.cs5412.taskmanager.TaskManager;
import com.cs5412.taskmanager.TaskStatus;
import com.cs5412.taskmanager.TaskType;
import com.cs5412.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Path("/wsd")
public class NaiveBayesWSDService {
	static final Logger LOG = LoggerFactory.getLogger(NaiveBayesWSDService.class);
	
	@Context ServletContext context;
	public static String loadBalancerAddress;
	TaskManager taskManager;
	IFileSystem fs;
	CouchbaseClient couchbaseClient;
	Gson gson;
	
	@PostConstruct
    void initialize() {
		taskManager = new TaskManager((CouchbaseClient)context.getAttribute("couchbaseClient"));
		fs = (IFileSystem) context.getAttribute("fileSystem");
		PropertiesConfiguration config = (PropertiesConfiguration)context.getAttribute("config");
		loadBalancerAddress = config.getString("LOAD_BALANCER_URI");
		couchbaseClient = (CouchbaseClient)context.getAttribute("couchbaseClient");
		final GsonBuilder gsonBuilder = new GsonBuilder();
	    gsonBuilder.registerTypeAdapter(WSDConfig.class, new WSDConfigAdaptor());
	    gsonBuilder.setPrettyPrinting();
	    gson = gsonBuilder.create();
    }
	
	@Path("/runDistributedService")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response runService(
			@FormParam("trainingDataset") String trainingDataset,
			@FormParam("validationDataset") String validationDataset,
			@FormParam("testDataset") String testDataset,
			@Context ServletContext context,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response
			) throws Exception {
		
		LOG.debug("Using "+trainingDataset+" dataset for WSD");
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		
		List<WSDConfig> accList = new ArrayList<WSDConfig>();
		String json = gson.toJson(accList);
		couchbaseClient.set(username + "WSDAcc", json).get();
		
		TaskManager taskManager = new TaskManager((CouchbaseClient)context.getAttribute("couchbaseClient"));
		
		LOG.debug(".........................................BEGIN..............................................................");
		
        String wsMainURL = "/ml/wsd/runDistributedService";
        TaskDao wsdTask = new TaskDao(username, "WSDRun", "complete", TaskStatus.RUNNING, false, wsMainURL);
    	wsdTask.setTaskType(TaskType.ALGORITHM_EXEC.toString());
    	wsdTask.setTaskDescription("Word Sense Disambiguation algorithm");
    	wsdTask.setParent(true);
    	taskManager.registerTask(wsdTask);
    	
    	ArrayList<String> parentIds = new ArrayList<String>();
    	ArrayList<String> mParentIds = new ArrayList<String>();
    	
    	int k = 0;
    	int[] coWindowSet = NaiveBayesWSD.coWindowSet;
    	int[] clWindowSet = NaiveBayesWSD.clWindowSet;
    	String[] wsTuneParamSubURLs = new String[coWindowSet.length*clWindowSet.length];
    	
        for(int coWindow: coWindowSet){
			for(int clWindow: clWindowSet) {
				String wsTuneParamSub = "/ml/wsd/tuneparametersub" + "/" + username + "/" + trainingDataset + "/" + validationDataset + "/" + coWindow + "/" + clWindow;
				TaskDao wsdValidationTask = new TaskDao(username, "Tune Parameter " + coWindow+" "+ clWindow, 
						                                "Tune Parameter " + coWindow+" "+ clWindow, TaskStatus.INITIALIZED, 
						                                false, wsTuneParamSub);
	        	wsTuneParamSub += "/" + wsdValidationTask.getTaskId();
	        	wsdValidationTask.setWsURL(wsTuneParamSub);
	        	wsTuneParamSubURLs[k] = wsTuneParamSub;
	        	k++;
	        	wsdValidationTask.setTaskType(TaskType.ALGORITHM_EXEC.toString());
	        	wsdValidationTask.setTaskDescription("Begin the tune parameter task " + coWindow + " "+clWindow);
	        	wsdValidationTask.setSeen(true);
	        	wsdValidationTask.setParentTaskId(parentIds);
	        	mParentIds.add(wsdValidationTask.getTaskId());
	        	taskManager.registerTask(wsdValidationTask);
			}
        }
    	
    	// WSD: Test Classification
    	String wsTestClassificationURL = "/ml/wsd/classify" + "/" + username+ "/" + trainingDataset + "/" + testDataset;
    	TaskDao wsdClassifyTask = new TaskDao(username, "WSDTestDataClassification", "WSDTestDataClassification", TaskStatus.INITIALIZED, false, wsTestClassificationURL);
    	wsTestClassificationURL += "/" + wsdClassifyTask.getTaskId();
    	wsdClassifyTask.setWsURL(wsTestClassificationURL);
    	wsdClassifyTask.setTaskType(TaskType.ALGORITHM_EXEC.toString());
    	wsdClassifyTask.setTaskDescription("WSD: Test Classification");
    	wsdClassifyTask.setSeen(true);
    	wsdClassifyTask.setParentTaskId(mParentIds);
    	taskManager.registerTask(wsdClassifyTask);
    	
    	parentIds = new ArrayList<String>();
    	parentIds.add(wsdClassifyTask.getTaskId());
		
    	// WSD: Report Generation
    	String wsReportGenerationURL = "/ml/wsd/generateReport"  + "/" + username + "/" + trainingDataset + "/" + testDataset;
    	TaskDao wsdReportGenarationTask = new TaskDao(username, "WSDReportGeneration", "WSDReportGeneration", TaskStatus.INITIALIZED, false, wsReportGenerationURL);
    	wsReportGenerationURL += "/" + wsdReportGenarationTask.getTaskId() + "/" + wsdTask.getTaskId();
    	wsdReportGenarationTask.setWsURL(wsReportGenerationURL);
    	wsdReportGenarationTask.setTaskType(TaskType.ALGORITHM_EXEC.toString());
    	wsdReportGenarationTask.setTaskDescription("WSD: Report Generation");
    	wsdReportGenarationTask.setSeen(true);
    	wsdReportGenarationTask.setParentTaskId(parentIds);
    	taskManager.registerTask(wsdReportGenarationTask);
    	
    	parentIds = new ArrayList<String>();
    	parentIds.add(wsdReportGenarationTask.getTaskId());
		
    	LOG.debug("WSD: Tuning Parameters");
		
    	CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
        httpclient.start();
        Future<HttpResponse> future = null;
        int index=0;
        for(int coWindow: coWindowSet){
			for(int clWindow: clWindowSet) {
        	
		        final HttpGet reqURL = new HttpGet(loadBalancerAddress + wsTuneParamSubURLs[index]);
		        future = httpclient.execute(reqURL, null);				
		        index++;
		        future.get();
			}
        }
        
        HttpResponse tempResponse = future.get();
    	
    	LOG.debug("WSD: Test Classification");
    	
    	String taskUrl = loadBalancerAddress + wsTestClassificationURL;
    	URL url = new URL(taskUrl);
    	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(1000000);
        conn.setConnectTimeout(1000000);
        conn.setRequestMethod("GET");
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.connect();
        LOG.debug(conn.getResponseCode() + "");
    	
    	LOG.debug("WSD: Report Generation");
		
    	taskUrl = loadBalancerAddress + wsReportGenerationURL;
        url = new URL(taskUrl);
        conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(1000000);
        conn.setConnectTimeout(1000000);
        conn.setRequestMethod("GET");
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.connect();
        LOG.debug(conn.getResponseCode() + "");
        
        return Response.status(200).entity("").build();
	}
	
	@Path("/getReport/{reportId}")
	@GET
	public Response getReportData(
			@PathParam("reportId") String reportId,
			@Context ServletContext context,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response
			) throws Exception {
		//IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		LOG.debug("Reading report"+reportId);
		String path = fs.getUserPath(username)+Utils.linuxSeparator+"reports"+Utils.linuxSeparator+reportId;
		return Response.status(200).entity(fs.readFileToString(path)).build();
	}
	
	
	@Path("/getTrainingDataSets")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTrainingDataSets(
			@Context ServletContext context,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response
			) throws Exception {
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		List<LocatedFileStatus> files = (List<LocatedFileStatus>) fs.getUploadedTrainingDatasets(username);
		JSONArray filesJson = new JSONArray();
		JSONObject jsonFile;
		for(FileStatus file : files){
			try {
				jsonFile = new JSONObject();
				jsonFile.put("optionValue", file.getPath().getName());
				jsonFile.put("optionDisplay", file.getPath().getName());
				filesJson.put(jsonFile);
			} catch (JSONException e) {
				LOG.error("Error", e);
			}			
		}
		return Response.status(200).entity(filesJson.toString()).build();
	}
	
	@Path("/getTestDataSets")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTestDataSets(
			@Context ServletContext context,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response
			) throws Exception {
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		List<LocatedFileStatus> files = (List<LocatedFileStatus>) fs.getUploadedTestDatasets(username);
		JSONArray filesJson = new JSONArray();
		JSONObject jsonFile;
		for(FileStatus file : files){
			try {
				jsonFile = new JSONObject();
				jsonFile.put("optionValue", file.getPath().getName());
				jsonFile.put("optionDisplay", file.getPath().getName());
				filesJson.put(jsonFile);
			} catch (JSONException e) {
				LOG.error("Error", e);
			}			
		}
		return Response.status(200).entity(filesJson.toString()).build();
	}
	
	@Path("/generateReport/{username}/{trainingDataset}/{testingDataset}/{taskId}/{masterTaskId}")
	@GET
	public Response generateReport(
			@PathParam("username") String username,
			@PathParam("trainingDataset") String trainingDataset,
			@PathParam("testingDataset") String testDataset,
			@PathParam("taskId") String taskId,
			@PathParam("masterTaskId") String masterTaskId,
			@Context ServletContext context,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response
			)throws Exception{
		
		  String tId = taskId;
		  TaskDao task = taskManager.getTaskById(tId);
		  task.setHostAddress(Utils.getIP());
		  taskManager.setTaskStatus(task, TaskStatus.RUNNING);
		  
		  String mTid = masterTaskId;
		  TaskDao masterTask = taskManager.getTaskById(mTid);
		  
		  JSONArray result = new JSONArray();
			try{

				Type collectionType = new TypeToken<List<WSDConfig>>(){}.getType();
				List<WSDConfig> wsdConfigList = gson.fromJson((String) couchbaseClient.get(username + "WSDAcc"), collectionType);
				
				JSONArray dataPoints;
				JSONArray dataPoint;

				DecimalFormat df = new DecimalFormat("#");
		        df.setMaximumFractionDigits(8);
		        
		        //CO
		        dataPoints = new JSONArray();
				for(WSDConfig wsdConfig:wsdConfigList){
					
					dataPoint = new JSONArray();
					dataPoint.put(wsdConfig.CO_WINDOW);
					dataPoint.put(wsdConfig.getScore());
					dataPoints.put(dataPoint);	
				}
				result.put(dataPoints);
				
				String filePath = fs.getUserPath(username)+Utils.linuxSeparator+"reports"+Utils.linuxSeparator+ trainingDataset+"-"+testDataset+"_CO"+IFileSystem.CHART_DATA_FORMAT;
				if(fs.isPathPresent(filePath)){
		      		int version = Integer.parseInt(filePath.split("_")[1]);
		      		version++;
		      		BufferedWriter bw = fs.createFileToWrite(filePath+version,true);
			      	bw.write(result.toString());
			      	bw.close();
		      		
		      	}else{
		      		BufferedWriter bw = fs.createFileToWrite(filePath+"_0",true);
			      	bw.write(result.toString());
			      	bw.close();
		      	}
				
				//CL
				result = new JSONArray();
				
				dataPoints = new JSONArray();
				for(WSDConfig wsdConfig:wsdConfigList){
					
					dataPoint = new JSONArray();
					dataPoint.put(wsdConfig.CL_WINDOW);
					dataPoint.put(wsdConfig.getScore());
					dataPoints.put(dataPoint);	
				}
				result.put(dataPoints);
				
				filePath = fs.getUserPath(username)+Utils.linuxSeparator+"reports"+Utils.linuxSeparator+ trainingDataset+"-"+testDataset+"_CL"+IFileSystem.CHART_DATA_FORMAT;
				if(fs.isPathPresent(filePath)){
		      		int version = Integer.parseInt(filePath.split("_")[1]);
		      		version++;
		      		BufferedWriter bw = fs.createFileToWrite(filePath+version,true);
			      	bw.write(result.toString());
			      	bw.close();
		      		
		      	}else{
		      		BufferedWriter bw = fs.createFileToWrite(filePath+"_0",true);
			      	bw.write(result.toString());
			      	bw.close();
		      	}
				
				LOG.debug("FINISHED WSD EXECUTION for"+trainingDataset+"|"+testDataset);
	      	taskManager.setTaskStatus(task, TaskStatus.SUCCESS);
	      	taskManager.setTaskStatus(masterTask, TaskStatus.SUCCESS);
	      }catch(Exception e){
	    	  taskManager.setTaskStatus(task, TaskStatus.FAILURE);
	      	LOG.debug("Error: " + e);
	      }
		
		return Response.status(200).entity("Hello World!").build();		
	}

	@Path("/tuneparameters/{username}/{trainingDataset}/{validationDataset}/{taskId}")
	@GET
	public Response tuneParameters(
			@PathParam("username") String username,
			@PathParam("trainingDataset") String trainingDataset,
			@PathParam("validationDataset") String validationDataset,
			@PathParam("taskId") String taskId,
			@Context ServletContext context
			)throws Exception{
		String tId = taskId;
		TaskDao task = taskManager.getTaskById(tId);
		task.setHostAddress(Utils.getIP());
		taskManager.setTaskStatus(task, TaskStatus.RUNNING);
		try{
			IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
			String trainingFile = fs.getFilePathForUploads(trainingDataset, username);
			String validationFile = fs.getFilePathForUploads(validationDataset, username);
			
			LOG.debug("Tuning cooccurrence window and collocational window parameters");
			
			NaiveBayesWSD.tuneParameters(trainingFile, validationFile, fs);
			
			WSDConfig bestWSDConfig = NaiveBayesWSD.scoreList.get(0);
			LOG.debug("Best CO_WINDOW = "+bestWSDConfig.getCO_WINDOW()+" CL_WINDOW = "+ bestWSDConfig.getCL_WINDOW());
			
			List<WSDConfig> accuracies = NaiveBayesWSD.scoreList;
			
			String json = gson.toJson(accuracies);
			couchbaseClient.set(username + "WSDAcc", json).get();
			
			json = gson.toJson(bestWSDConfig.getCO_WINDOW());
			couchbaseClient.set(username + "WSDBestCO", json).get();
			
			json = gson.toJson(bestWSDConfig.getCL_WINDOW());
			couchbaseClient.set(username + "WSDBestCL", json).get();
			
			taskManager.removeParentDependency(tId, username);
			taskManager.setTaskStatus(task, TaskStatus.SUCCESS);
		}catch(Exception e){
			taskManager.setTaskStatus(task, TaskStatus.FAILURE);
			LOG.debug(e.getMessage());
		}
		return Response.status(200).entity("Hello").build();
	}
	
	@Path("/tuneparametersub/{username}/{trainingDataset}/{validationDataset}/{coWindow}/{clWindow}/{taskId}")
	@GET
	public Response tuneParameterSub(
			@PathParam("username") String username,
			@PathParam("trainingDataset") String trainingDataset,
			@PathParam("validationDataset") String validationDataset,
			@PathParam("coWindow") String coWindow,
			@PathParam("clWindow") String clWindow,
			@PathParam("taskId") String taskId,
			@Context ServletContext context
			)throws Exception{
		String tId = taskId;
		TaskDao task = taskManager.getTaskById(tId);
		task.setHostAddress(Utils.getIP());
		taskManager.setTaskStatus(task, TaskStatus.RUNNING);
		try{
			IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
			String trainingFile = fs.getFilePathForUploads(trainingDataset, username);
			String validationFile = fs.getFilePathForUploads(validationDataset, username);
			
			LOG.debug("Tuning cooccurrence window and collocational window parameters");
			LOG.debug("coWindow: "+coWindow+" clWindow: "+clWindow);
			
			int coWindowInt = Integer.parseInt(coWindow);
			int clWindowInt = Integer.parseInt(clWindow);
			double accuracy = NaiveBayesWSD.tuneParametersSub(trainingFile, validationFile, coWindowInt, clWindowInt, fs);
			
			WSDConfig wsdConfig = new WSDConfig(coWindowInt,clWindowInt,accuracy);
			
			Type collectionType = new TypeToken<List<WSDConfig>>(){}.getType();
			List<WSDConfig> wsdConfigList = gson.fromJson((String) couchbaseClient.get(username + "WSDAcc"), collectionType);
			wsdConfigList.add(wsdConfig);
			String json = gson.toJson(wsdConfigList);
			couchbaseClient.set(username + "WSDAcc", json).get();
			
			taskManager.removeParentDependency(tId, username);
			taskManager.setTaskStatus(task, TaskStatus.SUCCESS);
		}catch(Exception e){
			taskManager.setTaskStatus(task, TaskStatus.FAILURE);
			LOG.debug(e.getMessage());
		}
		return Response.status(200).entity("Hello").build();
	}
	
	@Path("/classify/{username}/{trainingDataSet}/{testingDataSet}/{taskId}")
	@GET
	public Response classify(
			@PathParam("username") String username,
			@PathParam("trainingDataSet") String trainingDataSet,
			@PathParam("testingDataSet") String testingDataSet,
			@PathParam("taskId") String taskId,
			@Context ServletContext context
			)throws Exception{
		String tId = taskId;
		TaskDao task = taskManager.getTaskById(tId);
		task.setHostAddress(Utils.getIP());
		taskManager.setTaskStatus(task, TaskStatus.RUNNING);
		try{
			String trainFile = fs.getFilePathForUploads(trainingDataSet, username);
			String testFile = fs.getFilePathForUploads(testingDataSet, username);
			
			Type collectionType = new TypeToken<List<WSDConfig>>(){}.getType();
			List<WSDConfig> wsdConfigList = gson.fromJson((String) couchbaseClient.get(username + "WSDAcc"), collectionType);
			Collections.sort(wsdConfigList);
			String json = gson.toJson(wsdConfigList);
			couchbaseClient.set(username + "WSDAcc", json).get();
			
			WSDConfig bestWSDConfig = wsdConfigList.get(0);
			
			int bestCoWindow = bestWSDConfig.getCO_WINDOW();
			int bestClWindow = bestWSDConfig.getCL_WINDOW();
			
			LOG.debug("Best coWindow: "+bestCoWindow+" clWindow: "+bestClWindow);
			
			NaiveBayesWSD.runClassification(username,fs,trainFile, testFile,bestCoWindow,bestClWindow);
			taskManager.removeParentDependency(tId, username);
			taskManager.setTaskStatus(task, TaskStatus.SUCCESS);
		}catch(Exception e){
			taskManager.setTaskStatus(task, TaskStatus.FAILURE);
			LOG.debug(e.getMessage());
		}
		return Response.status(200).entity("Hello").build();
	}
}
