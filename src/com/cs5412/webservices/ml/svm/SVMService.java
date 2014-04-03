package com.cs5412.webservices.ml.svm;

import java.io.BufferedWriter;
import java.io.File;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
import com.cs5412.webservices.fileupload.FileUploadServlet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Path("/svm")
public class SVMService{
	static final Logger LOG = LoggerFactory.getLogger(FileUploadServlet.class);
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
		LOG.debug("Using "+trainingDataset+" dataset for SVM");
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		
		ArrayList<ArrayList<Double>> accList = new ArrayList<ArrayList<Double>>();
		String json = gson.toJson(accList);
		couchbaseClient.set(username + "SVMAcc", json).get();
		
		TaskManager taskManager = new TaskManager((CouchbaseClient)context.getAttribute("couchbaseClient"));
		
		LOG.debug(".........................................Here i am ..............................................................");
		
        String wsURL = "/ml/svm/runDistributedService";
        TaskDao svmTask = new TaskDao(username, "SVMRun", "complete", TaskStatus.RUNNING, false, wsURL);
    	svmTask.setTaskType(TaskType.ALGORITHM_EXEC.toString());
    	svmTask.setTaskDescription("Support Vector Machine algorithm");
    	taskManager.registerTask(svmTask);
    	
    	ArrayList<String> parentIds = new ArrayList<String>();
    	String wsURL1 = "/ml/svm/crossvalidation/createfiles" + "/" + username + "/" + trainingDataset;
    	TaskDao svmTask1 = new TaskDao(username, "SVMCreateFiles", "SVMCreateFiles", TaskStatus.INITIALIZED, false, wsURL1);
    	wsURL1 += "/" + svmTask1.getTaskId();
    	svmTask1.setWsURL(wsURL1);
    	svmTask1.setTaskType(TaskType.ALGORITHM_EXEC.toString());
    	svmTask1.setTaskDescription("Create the SVM cross validation files");
    	svmTask1.setSeen(true);
    	svmTask1.setParentTaskId(parentIds);
    	taskManager.registerTask(svmTask1);
    	
    	parentIds = new ArrayList<String>();
    	parentIds.add(svmTask1.getTaskId());
    	
    	ArrayList<String> mParentIds = new ArrayList<String>();
    	String[] wsURL2s = new String[35];
    	int k = 0;
        for(int i=1;i<=NUM_MODELS;i++){
			for(int j=0;j<=6;j++) {
				String wsURL2 = "/ml/svm/crossvalidation/createmodel" + "/" + username + "/" + j + "/" + i;
	    		svmTask1 = new TaskDao(username, "CrossValidation " + i, "CV " + i, TaskStatus.INITIALIZED, false, wsURL2);
	        	wsURL2 += "/" + svmTask1.getTaskId();
	        	svmTask1.setWsURL(wsURL2);
	        	wsURL2s[k] = wsURL2;
	        	k++;
	        	svmTask1.setTaskType(TaskType.ALGORITHM_EXEC.toString());
	        	svmTask1.setTaskDescription("Begin the cross validation task " + i);
	        	svmTask1.setSeen(true);
	        	svmTask1.setParentTaskId(parentIds);
	        	mParentIds.add(svmTask1.getTaskId());
	        	taskManager.registerTask(svmTask1);
			}
        }
        
        String wsURL3 = "/ml/svm/crossvalidation/bestTradeOff/" + "/" + username;
    	svmTask1 = new TaskDao(username, "calcBestTradeOff", "calcBestTradeOff", TaskStatus.INITIALIZED, false, wsURL3);
    	wsURL3 += "/" + svmTask1.getTaskId();
    	svmTask1.setWsURL(wsURL3);
    	svmTask1.setTaskType(TaskType.ALGORITHM_EXEC.toString());
    	svmTask1.setTaskDescription("Calculation of the best tradeoff parameter c");
    	svmTask1.setSeen(true);
    	svmTask1.setParentTaskId(mParentIds);
    	taskManager.registerTask(svmTask1);
    	
    	parentIds = new ArrayList<String>();
    	parentIds.add(svmTask1.getTaskId());
    	
    	String wsURL4 = "/ml/svm/crossvalidation/predictTest" + "/" + username + "/" + trainingDataset + "/" + testDataset;
    	svmTask1 = new TaskDao(username, "predictTest", "predictTest", TaskStatus.INITIALIZED, false, wsURL4);
    	wsURL4 += "/" + svmTask1.getTaskId();
    	svmTask1.setWsURL(wsURL4);
    	svmTask1.setTaskType(TaskType.ALGORITHM_EXEC.toString());
    	svmTask1.setTaskDescription("Begin the prediction step");
    	svmTask1.setSeen(true);
    	svmTask1.setParentTaskId(parentIds);
    	taskManager.registerTask(svmTask1);
    	
    	parentIds = new ArrayList<String>();
    	parentIds.add(svmTask1.getTaskId());
    	
    	String wsURL5 = "/ml/svm/generateReport" + "/" + username + "/" + trainingDataset + "/" + testDataset;
    	svmTask1 = new TaskDao(username, "GenerateReport", "GenerateReport", TaskStatus.INITIALIZED, false, wsURL5);
    	wsURL5 += "/" + svmTask1.getTaskId() + "/" + svmTask.getTaskId();
    	svmTask1.setWsURL(wsURL5);
    	svmTask1.setTaskType(TaskType.ALGORITHM_EXEC.toString());
    	svmTask1.setTaskDescription("Begin the GenerateReport task");
    	svmTask1.setSeen(true);
    	svmTask1.setParentTaskId(parentIds);
    	taskManager.registerTask(svmTask1);
    	
    	taskManager.setTaskStatus(svmTask, TaskStatus.PARENT);
    	
    	LOG.debug("Creating CV files");
    	
		String taskUrl = loadBalancerAddress + wsURL1;
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
        
        LOG.debug("Creating Models");
        
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
        httpclient.start();
        Future<HttpResponse> future = null;
        k = 0;
        for(int i=1;i<=NUM_MODELS;i++){
			for(int j=0;j<=6;j++) {
		        final HttpGet reqURL = new HttpGet(loadBalancerAddress + wsURL2s[k]);
		        future = httpclient.execute(reqURL, null);				
		        k++;
		        //future.get();
			}
		}
        HttpResponse response1 = future.get();
        
        LOG.debug("Calculating best trade off parameter c");
        
        taskUrl = loadBalancerAddress + wsURL3;
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

        LOG.debug("Predicting the test");
        
        taskUrl = loadBalancerAddress + wsURL4;
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

        taskUrl = loadBalancerAddress + wsURL5;
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
			@PathParam("testingDataset") String testingDataset,
			@PathParam("taskId") String taskId,
			@PathParam("masterTaskId") String masterTaskId,
			@Context ServletContext context,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response
			)throws Exception{
		
		  String tId = taskId;
		  TaskDao task = taskManager.getTaskById(tId, username);
		  task.setHostAddress(Utils.getIP());
		  taskManager.setTaskStatus(task, TaskStatus.RUNNING);
		  
		  String mTid = masterTaskId;
		  TaskDao masterTask = taskManager.getTaskById(mTid, username);
		  
	      JSONArray result = new JSONArray();
	      try{
	    	Type collectionType = new TypeToken<ArrayList<ArrayList<Double>>>(){}.getType();
	      	ArrayList<ArrayList<Double>> yVal = gson.fromJson((String) couchbaseClient.get(username + "SVMAcc"), collectionType);;
	      	ArrayList<Double> avgValAccuracies = yVal.get(yVal.size()-1);
	      	yVal.remove(yVal.size() - 1);
	      	JSONArray dataPoints;
	      	JSONArray dataPoint;
	      	int i=0;
	      	DecimalFormat df = new DecimalFormat("#");
	      	df.setMaximumFractionDigits(8);
	      	// String xValFormatted;
	      	for(List<Double> yList:yVal){
	      		dataPoints = new JSONArray();
	      		for(Double val:yList){
	      			dataPoint = new JSONArray();
	      			//xValFormatted = df.format(xVal[i]);
	      			dataPoint.put(i);
	      			dataPoint.put(val);
	      			dataPoints.put(dataPoint);
	      			i++;	
	      		}
	      		result.put(dataPoints);
	      		i=0;
	      	}
	      	i=0;
	      	dataPoints = new JSONArray();
	      	for(Double yList:avgValAccuracies){
	      		dataPoint = new JSONArray();
	      		dataPoint.put(i);
	      		dataPoint.put(yList);
	      		dataPoints.put(dataPoint);
	      		i++;		
	      	}
	      	result.put(dataPoints);
	      	IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
	      	String filePath = fs.getUserPath(username)+Utils.linuxSeparator+"reports"+Utils.linuxSeparator+ trainingDataset+"-"+testingDataset+IFileSystem.CHART_DATA_FORMAT;
	      	BufferedWriter bw = fs.createFileToWrite(filePath,true);
	      	bw.write(result.toString());
	      	bw.close();
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
	public Response createSVMFiles(
			@PathParam("username") String username,
			@PathParam("trainingDataSet") String trainingDataset,
			@PathParam("taskId") String taskId,
			@Context ServletContext context		
			) throws Exception {
		String tId = taskId;
		TaskDao task = taskManager.getTaskById(tId, username);
		task.setHostAddress(Utils.getIP());
		taskManager.setTaskStatus(task, TaskStatus.RUNNING);
		try{
			IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
			LOG.debug("Using "+trainingDataset+" to begin the service");
			String crossvalidation = fs.getUserPath(username)+Utils.linuxSeparator+"work"+Utils.linuxSeparator+"crossvalidation";
			String trainFile = fs.getFilePathForUploads(trainingDataset, username);
			
			CrossValidationFiles.createFiles(trainFile, fs,crossvalidation);
			taskManager.removeParentDependency(tId, username);
			taskManager.setTaskStatus(task, TaskStatus.SUCCESS);
		}catch(Exception e){
			taskManager.setTaskStatus(task, TaskStatus.FAILURE);
			LOG.debug(e.getMessage());
		}
		return Response.status(200).entity("Hello").build();
	}
	
	
	@Path("/crossvalidation/createmodel/{username}/{c}/{fileNum}/{taskId}")
	@GET
	public Response createModel(
			@PathParam("username") String username,
			@PathParam("c") int c,
			@PathParam("taskId") String taskId,
			@PathParam("fileNum") int fileNum,
			@Context ServletContext context) throws Exception {
		String tId = taskId;
		TaskDao task = taskManager.getTaskById(tId, username);
		task.setHostAddress(Utils.getIP());
		taskManager.setTaskStatus(task, TaskStatus.RUNNING);
		try{
			IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
			LOG.debug("Creating the model files" + fileNum + " " + c);
			String crossvalidation = fs.getUserPath(username)+Utils.linuxSeparator+"work"+Utils.linuxSeparator+"crossvalidation";
			String modelPath = crossvalidation+Utils.linuxSeparator+"model"+Utils.linuxSeparator;
			
			Model.create(crossvalidation +File.separator+ "SVM" , fileNum, c, "0", modelPath,fs);
			taskManager.removeParentDependency(tId, username);
			taskManager.setTaskStatus(task, TaskStatus.SUCCESS);
		}catch(Exception e){
			taskManager.setTaskStatus(task, TaskStatus.FAILURE);
			LOG.debug(e.getMessage());
		}
		return Response.status(200).entity("Hello").build();
	}
	
	@Path("/crossvalidation/bestTradeOff/{username}/{taskId}")
	@GET
	public Response crossvalidation(
			@PathParam("username") String username,
			@PathParam("taskId") String taskId,
			@Context ServletContext context
			)throws Exception{
		String tId = taskId;
		TaskDao task = taskManager.getTaskById(tId, username);
		task.setHostAddress(Utils.getIP());
		taskManager.setTaskStatus(task, TaskStatus.RUNNING);
		try{
			IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
			String crossvalidation = fs.getUserPath(username)+Utils.linuxSeparator+"work"+Utils.linuxSeparator+"crossvalidation";
			String modelPath = crossvalidation+Utils.linuxSeparator+"model"+Utils.linuxSeparator;
			
			LOG.debug("Calculating bestC");
			String bestC = Classifier.valClassifyCaller(fs,crossvalidation,modelPath,"testOutput.txt");
			LOG.debug("bestC="+bestC.split(" ")[0]);
			
			ArrayList<ArrayList<Double>> accuracies = Classifier.valAccuracies;
			ArrayList<Double> avgAcc = Classifier.avgValAccuracies;
			accuracies.add(avgAcc);
			
			String json = gson.toJson(accuracies);
			couchbaseClient.set(username + "SVMAcc", json).get();
			
			json = gson.toJson(Integer.parseInt(bestC.split(" ")[0]));
			couchbaseClient.set(username + "SVMBestC", json).get();
			
			taskManager.removeParentDependency(tId, username);
			taskManager.setTaskStatus(task, TaskStatus.SUCCESS);
		}catch(Exception e){
			taskManager.setTaskStatus(task, TaskStatus.FAILURE);
			LOG.debug(e.getMessage());
		}
		return Response.status(200).entity("Hello").build();
	}
	
	@Path("/crossvalidation/predictTest/{username}/{trainingDataSet}/{testingDataSet}/{taskId}")
	@GET
	public Response testPrediction(
			@PathParam("username") String username,
			@PathParam("taskId") String taskId,
			@PathParam("trainingDataSet") String trainingDataSet,
			@PathParam("testingDataSet") String testingDataSet,
			@Context ServletContext context
			)throws Exception{
		String tId = taskId;
		TaskDao task = taskManager.getTaskById(tId, username);
		task.setHostAddress(Utils.getIP());
		taskManager.setTaskStatus(task, TaskStatus.RUNNING);
		try{
			String trainFile = fs.getFilePathForUploads(trainingDataSet, username);
			String testFile = fs.getFilePathForUploads(testingDataSet, username);
			
			Type collectionType = new TypeToken<Integer>(){}.getType();
			Integer bc = gson.fromJson((String) couchbaseClient.get(username + "SVMBestC"), collectionType);
			
			TestClassification.testClassify(bc, "0", fs,username,trainFile,"cv.txt",testFile);
			taskManager.removeParentDependency(tId, username);
			taskManager.setTaskStatus(task, TaskStatus.SUCCESS);
		}catch(Exception e){
			taskManager.setTaskStatus(task, TaskStatus.FAILURE);
			LOG.debug(e.getMessage());
		}
		return Response.status(200).entity("Hello").build();
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
