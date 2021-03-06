package com.cs5412.webservices.ml.dt;

import java.io.BufferedWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.cs5412.filesystem.IFileSystem;
import com.cs5412.http.AsyncClientHttp;
import com.cs5412.taskmanager.TaskDao;
import com.cs5412.taskmanager.TaskManager;
import com.cs5412.taskmanager.TaskStatus;
import com.cs5412.taskmanager.TaskType;
import com.cs5412.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * This is the service code for the random forest.
 * @author rb723
 *
 */
@Path("/dTree")
public class RandomForestService {
	static final Logger LOG = LoggerFactory.getLogger(RandomForestService.class);
	public static int [] choplist = {2,3,5,10,50,80};
	
	@Context ServletContext context;
	public static String loadBalancerAddress;
	TaskManager taskManager;
	IFileSystem fs;
	CouchbaseClient couchbaseClient;
	Gson gson;
	private static String DT_WORK_DIR="dt";
	@PostConstruct
    void initialize() {
		taskManager = new TaskManager((CouchbaseClient)context.getAttribute("couchbaseClient"));
		fs = (IFileSystem) context.getAttribute("fileSystem");
		PropertiesConfiguration config = (PropertiesConfiguration)context.getAttribute("config");
		loadBalancerAddress = config.getString("LOAD_BALANCER_URI");
		couchbaseClient = (CouchbaseClient)context.getAttribute("couchbaseClient");
		gson = new Gson();
    }
	
	/**
	 * Parent API responsible for splitting the parent task and registering the subtasks in couchBase.
	 * The subtasks are then submitted to the load balancer. Parallel tasks are identified and submitted to the
	 * loadbalancer in asynchronized fashion.
	 * @param trainingDataset
	 * @param testDataset
	 * @param context
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
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
		
		try{
			LOG.debug("Using "+trainingDataset+" dataset for Decision Tree");
			HttpSession session = request.getSession(false);
			String username = (String) session.getAttribute("user");
			
			long startTime = System.currentTimeMillis();
			String json = gson.toJson(startTime);
			couchbaseClient.set(username + "dtStartTime", json).get();
			
			ArrayList<ArrayList<Double>> accList = new ArrayList<ArrayList<Double>>();
			json = gson.toJson(accList);
			couchbaseClient.set(username + "DTAcc", json).get();
			
			TaskManager taskManager = new TaskManager((CouchbaseClient)context.getAttribute("couchbaseClient"));
			
	        String wsURL = "/ml/dTree/runDistributedService";
	        TaskDao dtTask = new TaskDao(username, "Decision Tree execution for "+trainingDataset+"/"+testDataset, "complete", TaskStatus.RUNNING, false, wsURL);
	    	dtTask.setTaskType(TaskType.ALGORITHM_EXEC.toString());
	    	dtTask.setTaskDescription("Decision Tree algorithm");
	    	
	    	taskManager.registerTask(dtTask);
	    	
	    	ArrayList<String> parentIds = new ArrayList<String>();
	    	String wsURL1 = "/ml/dTree/beginService" + "/" + username + "/" + trainingDataset;
	    	TaskDao dtTask1 = new TaskDao(username, "serviceBegin", "dtSerBegin", TaskStatus.INITIALIZED, false, wsURL1);
	    	wsURL1 += "/" + dtTask1.getTaskId();
	    	dtTask1.setWsURL(wsURL1);
	    	dtTask1.setTaskType(TaskType.ALGORITHM_EXEC.toString());
	    	dtTask1.setTaskDescription("Begin the decision tree task");
	    	dtTask1.setSeen(true);
	    	dtTask1.setParentTaskId(parentIds);
	    	taskManager.registerTask(dtTask1);
	    	
	    	parentIds = new ArrayList<String>();
	    	parentIds.add(dtTask1.getTaskId());
	    	
	    	ArrayList<String> mParentIds = new ArrayList<String>();
	    	String[] wsURL2s = new String[5];
	    	for(int i=1;i<=5;i++){
	    		String wsURL2 = "/ml/dTree/generateEachService" + "/" + username + "/" + i;
	    		dtTask1 = new TaskDao(username, "CrossValidation " + i, "CV " + i, TaskStatus.INITIALIZED, false, wsURL2);
	        	wsURL2 += "/" + dtTask1.getTaskId();
	        	dtTask1.setWsURL(wsURL2);
	        	wsURL2s[i-1] = wsURL2;
	        	dtTask1.setTaskType(TaskType.ALGORITHM_EXEC.toString());
	        	dtTask1.setTaskDescription("Begin the cross validation task " + i);
	        	dtTask1.setSeen(true);
	        	dtTask1.setParentTaskId(parentIds);
	        	mParentIds.add(dtTask1.getTaskId());
	        	taskManager.registerTask(dtTask1);
	    	}
	    	
	    	
	    	String wsURL3 = "/ml/dTree/calcBestService" + "/" + username;
	    	dtTask1 = new TaskDao(username, "calcBestHeight", "calcBestHeight", TaskStatus.INITIALIZED, false, wsURL3);
	    	wsURL3 += "/" + dtTask1.getTaskId();
	    	dtTask1.setWsURL(wsURL3);
	    	dtTask1.setTaskType(TaskType.ALGORITHM_EXEC.toString());
	    	dtTask1.setTaskDescription("Begin the Calucation of best height task");
	    	dtTask1.setSeen(true);
	    	dtTask1.setParentTaskId(mParentIds);
	    	taskManager.registerTask(dtTask1);
	    	
	    	parentIds = new ArrayList<String>();
	    	parentIds.add(dtTask1.getTaskId());
	    	
	    	String wsURL4 = "/ml/dTree/AccuracyService" + "/" + username + "/" + trainingDataset + "/" + testDataset;
	    	dtTask1 = new TaskDao(username, "calcAcc", "calcAcc", TaskStatus.INITIALIZED, false, wsURL4);
	    	wsURL4 += "/" + dtTask1.getTaskId();
	    	dtTask1.setWsURL(wsURL4);
	    	dtTask1.setTaskType(TaskType.ALGORITHM_EXEC.toString());
	    	dtTask1.setTaskDescription("Begin the calculation of validation accuracy task");
	    	dtTask1.setSeen(true);
	    	dtTask1.setParentTaskId(parentIds);
	    	taskManager.registerTask(dtTask1);
	    	
	    	parentIds = new ArrayList<String>();
	    	parentIds.add(dtTask1.getTaskId());
	    	
	    	String wsURL5 = "/ml/dTree/generateReport" + "/" + username + "/" + trainingDataset + "/" + testDataset;
	    	dtTask1 = new TaskDao(username, "GenerateReport", "GenerateReport", TaskStatus.INITIALIZED, false, wsURL5);
	    	wsURL5 += "/" + dtTask1.getTaskId() + "/" + dtTask.getTaskId();
	    	dtTask1.setWsURL(wsURL5);
	    	dtTask1.setTaskType(TaskType.ALGORITHM_EXEC.toString());
	    	dtTask1.setTaskDescription("Begin the GenerateReport task");
	    	dtTask1.setSeen(true);
	    	dtTask1.setParentTaskId(parentIds);
	    	taskManager.registerTask(dtTask1);
	    	
	    	dtTask.setParent(true);
	    	
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
	        
	      //Issue Async/Non blocking HTTP calls
	        AsyncClientHttp.executeRequests(loadBalancerAddress, wsURL2s);
	   	   
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
	        
	        return Response.status(200).entity("HelloDT").build();
		}
		catch(Exception e){
			LOG.debug("Error",e);
			return Response.status(500).entity("Error").build();
		}
	}
	
	@Path("/getReport/{reportId}")
	@GET
	public Response getReportData(
			@PathParam("reportId") String reportId,
			@Context ServletContext context,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response
			) throws Exception {
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		LOG.debug("Reading report"+reportId);
		String path = fs.getUserPath(username)+Utils.linuxSeparator+"reports"+Utils.linuxSeparator+reportId;
		return Response.status(200).entity(fs.readFileToString(path)).build();
	}
	
	/**
	 * generate the report
	 * @param username
	 * @param trainingDataset
	 * @param testingDataset
	 * @param taskId
	 * @param masterTaskId
	 * @param context
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
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
		  TaskDao task = taskManager.getTaskById(tId);
		  task.setHostAddress(Utils.getIP());
		  taskManager.setTaskStatus(task, TaskStatus.RUNNING);
		  
		  String mTid = masterTaskId;
		  TaskDao masterTask = taskManager.getTaskById(mTid);
		  
	      JSONArray result = new JSONArray();
	      try{
	    	Type collectionType = new TypeToken<ArrayList<ArrayList<Double>>>(){}.getType();
	      	ArrayList<ArrayList<Double>> yVal = gson.fromJson((String) couchbaseClient.get(username + "DTAcc"), collectionType);;
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
	      	
	      	taskManager.setTaskStatus(task, TaskStatus.SUCCESS);
	      	taskManager.setTaskStatus(masterTask, TaskStatus.SUCCESS);
			collectionType = new TypeToken<Long>(){}.getType();
			long startTime = gson.fromJson((String) couchbaseClient.get(username + "dtStartTime"), collectionType);	
	      	long endTime = System.currentTimeMillis();
	      	LOG.debug("Time elapsed in Decision Tree execution (user: " + username + ") : " + (endTime - startTime)/(double)1000 + " seconds");
	      }catch(Exception e){
	    	  taskManager.setTaskStatus(task, TaskStatus.FAILURE);
	    	  LOG.debug("Error",e);
	      }
		
		return Response.status(200).entity("Hello World!").build();		
	}
	
	/**
	 * this begins the decision tree service
	 * @param username
	 * @param trainingDataset
	 * @param taskId
	 * @param context
	 * @return
	 * @throws Exception
	 */
	@Path("/beginService/{username}/{trainingDataSet}/{taskId}")
	@GET
	public Response beginService(
			@PathParam("username") String username,
			@PathParam("trainingDataSet") String trainingDataset,
			@PathParam("taskId") String taskId,
			@Context ServletContext context
			)throws Exception{
		
		String tId = taskId;
		TaskDao task = taskManager.getTaskById(tId);
		task.setHostAddress(Utils.getIP());
		taskManager.setTaskStatus(task, TaskStatus.RUNNING);
		try{
			IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
			LOG.debug("Using "+trainingDataset+" to begin the service");
			String crossvalidation = fs.getUserPath(username)+Utils.linuxSeparator+DT_WORK_DIR+Utils.linuxSeparator+"crossvalidation";
			String trainFile = fs.getFilePathForUploads(trainingDataset, username);
			
			CrossValidation CV = new CrossValidation();
			CV.createCrossValidationFiles(trainFile, fs, crossvalidation);
			taskManager.removeParentDependency(tId, username);
			taskManager.setTaskStatus(task, TaskStatus.SUCCESS);
		}catch(Exception e){
			taskManager.setTaskStatus(task, TaskStatus.FAILURE);
			LOG.debug("Error",e);
		}
		return Response.status(200).entity("Hello World1").build();
	}
	
	/**
	 * this generates the service for decision tree
	 * @param username
	 * @param i
	 * @param taskId
	 * @param context
	 * @return
	 * @throws Exception
	 */
	@Path("/generateEachService/{username}/{i}/{taskId}")
	@GET
	public Response generateEachService(
			@PathParam("username") String username,
			@PathParam("i") String i,
			@PathParam("taskId") String taskId,
			@Context ServletContext context
			)throws Exception{
		
		String tId = taskId;
		TaskDao task = taskManager.getTaskById(tId);
		task.setHostAddress(Utils.getIP());
		taskManager.setTaskStatus(task, TaskStatus.RUNNING);
		try{
			IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
			LOG.debug("Generating "+ i +" service");
			String crossvalidation = fs.getUserPath(username)+Utils.linuxSeparator+DT_WORK_DIR+Utils.linuxSeparator+"crossvalidation";
			ArrayList<Double> accuracy = new ArrayList<Double>();
			
			ExecuteMain EM = new ExecuteMain();
			String partTrainFile = crossvalidation + Utils.linuxSeparator + "trainFile" + i + ".txt";
			String partValFile = crossvalidation + Utils.linuxSeparator + "validFile" + i + ".txt";
			String outputFile = crossvalidation + Utils.linuxSeparator + "output" + i + ".txt";
			EM.Construct(partTrainFile, partValFile, outputFile, fs, accuracy);
			
			Type collectionType = new TypeToken<ArrayList<ArrayList<Double>>>(){}.getType();
			ArrayList<ArrayList<Double>> allAccuracies = gson.fromJson((String) couchbaseClient.get(username + "DTAcc"), collectionType);
			allAccuracies.add(accuracy);
			String json = gson.toJson(allAccuracies);
			couchbaseClient.set(username + "DTAcc", json).get();
			taskManager.removeParentDependency(tId, username);
			taskManager.setTaskStatus(task, TaskStatus.SUCCESS);
		}catch(Exception e){
			taskManager.setTaskStatus(task, TaskStatus.FAILURE);
			LOG.debug("Error",e);
		}
		
		return Response.status(200).entity("Hello World!").build();
	}
	
	/**
	 * This calculates the best height
	 * @param username
	 * @param taskId
	 * @param context
	 * @return
	 * @throws Exception
	 */
	@Path("/calcBestService/{username}/{taskId}")
	@GET
	public Response calcBestService(
			@PathParam("username") String username,
			@PathParam("taskId") String taskId,
			@Context ServletContext context
			)throws Exception{
		
		String tId = taskId;
		TaskDao task = taskManager.getTaskById(tId);
		task.setHostAddress(Utils.getIP());
		taskManager.setTaskStatus(task, TaskStatus.RUNNING);
		try{
			IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
			LOG.debug("Calculating the best service");
			String crossvalidation = fs.getUserPath(username)+Utils.linuxSeparator+DT_WORK_DIR+Utils.linuxSeparator+"crossvalidation";
			ArrayList<Double> avgAcc = new ArrayList<Double>();
			
			BestHeight BH = new BestHeight();
			Integer bestHeight = BH.calculateAccuracy(crossvalidation, fs, avgAcc);
			
			Type collectionType = new TypeToken<Integer>(){}.getType();
			String json = gson.toJson(bestHeight);
			couchbaseClient.set(username + "DTBestHeight", json).get();
			
			collectionType = new TypeToken<ArrayList<ArrayList<Double>>>(){}.getType();
			ArrayList<ArrayList<Double>> allAccuracies = gson.fromJson((String) couchbaseClient.get(username + "DTAcc"), collectionType);
			allAccuracies.add(avgAcc);
			json = gson.toJson(allAccuracies);
			couchbaseClient.set(username + "DTAcc", json).get();
			taskManager.removeParentDependency(tId, username);
			taskManager.setTaskStatus(task, TaskStatus.SUCCESS);
		}catch(Exception e){
			taskManager.setTaskStatus(task, TaskStatus.FAILURE);
			LOG.debug("Error",e);
		}
		
		return Response.status(200).entity("Hello World!").build();
	}
	
	/**
	 * Calculate accuracy for Decision tree
	 * @param username
	 * @param trainingDataset
	 * @param testingDataset
	 * @param taskId
	 * @param context
	 * @return
	 * @throws Exception
	 */
	@Path("/AccuracyService/{username}/{trainingDataset}/{testingDataset}/{taskId}")
	@GET
	public Response AccuracyService(
			@PathParam("username") String username,
			@PathParam("trainingDataset") String trainingDataset,
			@PathParam("testingDataset") String testingDataset,
			@PathParam("taskId") String taskId,
			@Context ServletContext context
			)throws Exception{	
		String tId = taskId;
		TaskDao task = taskManager.getTaskById(tId);
		task.setHostAddress(Utils.getIP());
		taskManager.setTaskStatus(task, TaskStatus.RUNNING);
		try{
			IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
			LOG.debug("Accuracy Service");
			String crossvalidation = fs.getUserPath(username)+Utils.linuxSeparator+DT_WORK_DIR+Utils.linuxSeparator+"crossvalidation";
			String trainFile = fs.getFilePathForUploads(trainingDataset, username);
			String testFile = fs.getFilePathForUploads(testingDataset, username);
			
			
			Type collectionType = new TypeToken<Integer>(){}.getType();
			Integer bh = gson.fromJson((String) couchbaseClient.get(username + "DTBestHeight"), collectionType);	
			
			ExecuteMain EM = new ExecuteMain();
			EM.FinalAccuracy(trainFile,testFile, bh, fs, crossvalidation);
			taskManager.removeParentDependency(tId, username);
			taskManager.setTaskStatus(task, TaskStatus.SUCCESS);
		}catch(Exception e){
			taskManager.setTaskStatus(task, TaskStatus.FAILURE);
			LOG.debug("Error",e);
		}
		return Response.status(200).entity("Hello World").build();
	}
		
	/**
	 * get the training data set for decision tree
	 * @param context
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@Path("/getTrainingDataSets")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTrainingDataSets(
			@Context ServletContext context,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response
			) throws Exception {
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		@SuppressWarnings("unchecked")
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
	
	/**
	 * get the test data sets for decision tree
	 * @param context
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@Path("/getTestDataSets")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTestDataSets(
			@Context ServletContext context,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response
			) throws Exception {
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		@SuppressWarnings("unchecked")
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

