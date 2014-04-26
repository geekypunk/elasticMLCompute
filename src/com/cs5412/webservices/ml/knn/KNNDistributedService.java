package com.cs5412.webservices.ml.knn;

import java.io.BufferedWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.google.gson.reflect.TypeToken;

@Path("/knn")
public class KNNDistributedService{
	static final Logger LOG = LoggerFactory.getLogger(KNNDistributedService.class);
	static final int NUM_MODELS = 5;
	
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
		gson = new Gson();
	}
	
	@Path("/runDistributedService")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response runDistributedService(
			@FormParam("trainingDataset") String trainingDataset,
			@FormParam("testDataset") String testDataset,
			@Context ServletContext context,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response
			) throws Exception{
		LOG.debug("Using "+trainingDataset+" dataset for KNN");
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		
		Map<Integer,ArrayList<Double>> accList = new HashMap<Integer,ArrayList<Double>>();
		String json = gson.toJson(accList);
		couchbaseClient.set(username + "KNNAcc", json).get();
		
		TaskManager taskManager = new TaskManager((CouchbaseClient)context.getAttribute("couchbaseClient"));
		
		LOG.debug(".........................................BEGIN..............................................................");
		
        String wsURL = "/ml/knn/runDistributedService";
        TaskDao knnTask = new TaskDao(username, "KNNRun", "complete", TaskStatus.RUNNING, false, wsURL);
    	knnTask.setTaskType(TaskType.ALGORITHM_EXEC.toString());
    	knnTask.setTaskDescription("K-Nearest Neighbor Algorithm");
    	knnTask.setParent(true);
    	taskManager.registerTask(knnTask);
    	
    	ArrayList<String> parentIds = new ArrayList<String>();
    	String wsCreateFilesForCVURL = "/ml/knn/crossvalidation/createfiles" + "/" + username + "/" + trainingDataset;
    	TaskDao knnCreateFilesForCVTask = new TaskDao(username, "KNNCreateFilesForCV", "KNNCreateFilesForCV", TaskStatus.INITIALIZED, false, wsCreateFilesForCVURL);
    	wsCreateFilesForCVURL += "/" + knnCreateFilesForCVTask.getTaskId();
    	knnCreateFilesForCVTask.setWsURL(wsCreateFilesForCVURL);
    	knnCreateFilesForCVTask.setTaskType(TaskType.ALGORITHM_EXEC.toString());
    	knnCreateFilesForCVTask.setTaskDescription("Create the KNN cross validation files");
    	knnCreateFilesForCVTask.setSeen(true);
    	knnCreateFilesForCVTask.setParentTaskId(parentIds);
    	taskManager.registerTask(knnCreateFilesForCVTask);
    	
    	parentIds = new ArrayList<String>();
    	parentIds.add(knnCreateFilesForCVTask.getTaskId());
    	
    	ArrayList<String> mParentIds = new ArrayList<String>();
    	int[] kSet = new int[]{1,3,5,7,9};
    	
    	String[] wsTuneParamURLs = new String[kSet.length*NUM_MODELS];
    	int index=0;
        for(int k: kSet){
			for(int modelNo=1;modelNo<=NUM_MODELS;modelNo++) {
				String wsTuneParamURL = "/ml/knn/runcrossvalidation" + "/" + username + "/" + k + "/" + modelNo;
				TaskDao knnCVTask = new TaskDao(username, "CrossValidation " + k, "CV " + modelNo, TaskStatus.INITIALIZED, false, wsTuneParamURL);
	        	wsTuneParamURL += "/" + knnCVTask.getTaskId();
	        	knnCVTask.setWsURL(wsTuneParamURL);
	        	wsTuneParamURLs[index] = wsTuneParamURL;
	        	index++;
	        	knnCVTask.setTaskType(TaskType.ALGORITHM_EXEC.toString());
	        	knnCVTask.setTaskDescription("Begin the cross validation task " + modelNo);
	        	knnCVTask.setSeen(true);
	        	knnCVTask.setParentTaskId(parentIds);
	        	mParentIds.add(knnCVTask.getTaskId());
	        	taskManager.registerTask(knnCVTask);
			}
        }
    	
    	String wsClassifyURL = "/ml/knn/classify" + "/" + username + "/" + trainingDataset + "/" + testDataset;
    	TaskDao knnClassifyTask = new TaskDao(username, "KNNTestDataClassification", "KNNTestDataClassification", TaskStatus.INITIALIZED, false, wsClassifyURL);
    	wsClassifyURL += "/" + knnClassifyTask.getTaskId();
    	knnClassifyTask.setWsURL(wsClassifyURL);
    	knnClassifyTask.setTaskType(TaskType.ALGORITHM_EXEC.toString());
    	knnClassifyTask.setTaskDescription("KNN Test data classification task");
    	knnClassifyTask.setSeen(true);
    	knnClassifyTask.setParentTaskId(parentIds);
    	taskManager.registerTask(knnClassifyTask);
    	
    	parentIds = new ArrayList<String>();
    	parentIds.add(knnClassifyTask.getTaskId());
    	
    	String wsReportGenerationURL = "/ml/knn/generateReport" + "/" + username + "/" + trainingDataset + "/" + testDataset;
    	TaskDao knnReportGenerationTask = new TaskDao(username, "GenerateReport", "GenerateReport", TaskStatus.INITIALIZED, false, wsReportGenerationURL);
    	wsReportGenerationURL += "/" + knnReportGenerationTask.getTaskId() + "/" + knnTask.getTaskId();
    	knnReportGenerationTask.setWsURL(wsReportGenerationURL);
    	knnReportGenerationTask.setTaskType(TaskType.ALGORITHM_EXEC.toString());
    	knnReportGenerationTask.setTaskDescription("Begin the GenerateReport task");
    	knnReportGenerationTask.setSeen(true);
    	knnReportGenerationTask.setParentTaskId(parentIds);
    	taskManager.registerTask(knnReportGenerationTask);
    	
    	LOG.debug("Creating CV files");
    	
		String taskUrl = loadBalancerAddress + wsCreateFilesForCVURL;
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
        
        LOG.debug("Running Cross Validation tasks");
        
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
        httpclient.start();
        Future<HttpResponse> future = null;
        int tuneParamIndex = 0;
        for(int k:kSet){
			for(int j=1;j<=NUM_MODELS;j++) {
		        final HttpGet reqURL = new HttpGet(loadBalancerAddress + wsTuneParamURLs[tuneParamIndex]);
		        future = httpclient.execute(reqURL, null);				
		        tuneParamIndex++;
		        //future.get();
			}
		}
        HttpResponse response1 = future.get();

        LOG.debug("Predicting the test");
        
        taskUrl = loadBalancerAddress + wsClassifyURL;
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
        
        LOG.debug("Generating the report");

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
			@Context HttpServletRequest request
			) throws Exception {
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		LOG.debug("Reading report"+reportId);
		String path = fs.getUserPath(username)+Utils.linuxSeparator+"reports"+Utils.linuxSeparator+reportId;
		return Response.status(200).entity(fs.readFileToString(path)).build();
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

				Type collectionType = new TypeToken<Map<Integer,ArrayList<Double>>>(){}.getType();
				Map<Integer,ArrayList<Double>> knnAccMap = gson.fromJson((String) couchbaseClient.get(username + "KNNAcc"), collectionType);
				
				JSONArray dataPoints;
				JSONArray dataPoint;

				DecimalFormat df = new DecimalFormat("#");
		        df.setMaximumFractionDigits(8);
		        
		        dataPoints = new JSONArray();
				for(int k: knnAccMap.keySet()){
					List<Double> accList = knnAccMap.get(k);
					for(double acc: accList){
						dataPoint = new JSONArray();
						dataPoint.put(k);
						dataPoint.put(acc);
						dataPoints.put(dataPoint);
					}
				}
				result.put(dataPoints);
				
				String filePath = fs.getUserPath(username)+Utils.linuxSeparator+"reports"+Utils.linuxSeparator+ trainingDataset+"-"+testDataset+IFileSystem.CHART_DATA_FORMAT;
				BufferedWriter bw = fs.createFileToWrite(filePath,true);
				bw.write(result.toString());
				bw.close();
				
				LOG.debug("FINISHED KNN EXECUTION for"+trainingDataset+"|"+testDataset);
	      	taskManager.setTaskStatus(task, TaskStatus.SUCCESS);
	      	taskManager.setTaskStatus(masterTask, TaskStatus.SUCCESS);
	      }catch(Exception e){
	    	  taskManager.setTaskStatus(task, TaskStatus.FAILURE);
	      	LOG.debug("Error: " + e);
	      }
		
		return Response.status(200).entity("Hello World!").build();		
	}
	
	@Path("/crossvalidation/createfiles/{username}/{trainingDataSet}/{taskId}")
	@GET
	public Response createKNNCVFiles(
			@PathParam("username") String username,
			@PathParam("trainingDataSet") String trainingDataset,
			@PathParam("taskId") String taskId,
			@Context ServletContext context		
			) throws Exception {
		String tId = taskId;
		TaskDao task = taskManager.getTaskById(tId);
		task.setHostAddress(Utils.getIP());
		taskManager.setTaskStatus(task, TaskStatus.RUNNING);
		try{
			IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
			LOG.debug("Using "+trainingDataset+" to begin the service");
			String crossvalidation = fs.getUserPath(username)+Utils.linuxSeparator+"work"+Utils.linuxSeparator+"crossvalidation"+Utils.linuxSeparator;
			String trainFile = fs.getFilePathForUploads(trainingDataset, username);
			
			KNN.createFiles(trainFile, fs,crossvalidation);
			taskManager.removeParentDependency(tId, username);
			taskManager.setTaskStatus(task, TaskStatus.SUCCESS);
		}catch(Exception e){
			taskManager.setTaskStatus(task, TaskStatus.FAILURE);
			LOG.debug(e.getMessage());
		}
		return Response.status(200).entity("Hello").build();
	}
	
	@Path("/runcrossvalidation/{username}/{k}/{modelNo}/{taskId}")
	@GET
	public Response runCrossValidatation(
			@PathParam("username") String username,
			@PathParam("k") int k,
			@PathParam("modelNo") int modelNo,
			@PathParam("taskId") String taskId,
			@Context ServletContext context) throws Exception {
		String tId = taskId;
		TaskDao task = taskManager.getTaskById(tId);
		task.setHostAddress(Utils.getIP());
		taskManager.setTaskStatus(task, TaskStatus.RUNNING);
		try{
			IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
			LOG.debug("Running crossvalidation with model no " + modelNo + " and k = " + k);
			String crossvalidation = fs.getUserPath(username)+Utils.linuxSeparator+"work"+Utils.linuxSeparator+"crossvalidation";
			String trainingDataHDFSPath = crossvalidation+Utils.linuxSeparator+"knn"+modelNo+".train";
			String validationDataHDFSPath = crossvalidation+Utils.linuxSeparator+"knn"+modelNo+".valid";
			
			double accuracy  = KNN.runValidation(trainingDataHDFSPath, validationDataHDFSPath, k, fs);
			
			Type collectionType = new TypeToken<Map<Integer,ArrayList<Double>>>(){}.getType();
			Map<Integer,ArrayList<Double>> knnAccMap = gson.fromJson((String) couchbaseClient.get(username + "KNNAcc"), collectionType);
			if(null==knnAccMap.get(k)){
				knnAccMap.put(k, new ArrayList<Double>());
			}
			List<Double> accListFork = knnAccMap.get(k);
			accListFork.add(accuracy);
			String json = gson.toJson(knnAccMap);
			couchbaseClient.set(username + "KNNAcc", json).get();
			
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
			
			Type collectionType = new TypeToken<Map<Integer,ArrayList<Double>>>(){}.getType();
			Map<Integer,ArrayList<Double>> knnAccMap = gson.fromJson((String) couchbaseClient.get(username + "KNNAcc"), collectionType);
			int bestK = getBestK(knnAccMap);
			
			LOG.debug("Best k: "+bestK);
			
			KNN.runClassification(username,fs,trainFile, testFile,bestK);
			taskManager.removeParentDependency(tId, username);
			taskManager.setTaskStatus(task, TaskStatus.SUCCESS);
		}catch(Exception e){
			taskManager.setTaskStatus(task, TaskStatus.FAILURE);
			LOG.debug(e.getMessage());
		}
		return Response.status(200).entity("Hello").build();
	}

	
	private int getBestK(Map<Integer, ArrayList<Double>> knnAccMap) {
		Map<Integer, Double> avgAccMap  = new HashMap<Integer, Double>();
		
		for(int k: knnAccMap.keySet()){
			List<Double> accList = knnAccMap.get(k);
			double sum = 0;
			for(double acc: accList){
				sum+=acc;
			}
			sum/=accList.size();
			avgAccMap.put(k, sum);
		}
		
		int maxK = -1;
		double maxAcc = Double.NEGATIVE_INFINITY;
		
		for(int k: avgAccMap.keySet()){
			double currAcc = avgAccMap.get(k);
			if(maxAcc<currAcc){
				maxK = k;
				maxAcc = currAcc;
			}
		}
		
		return maxK;
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
}
