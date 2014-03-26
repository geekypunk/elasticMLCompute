package com.cs5412.webservices.ml.svm;

import java.io.BufferedWriter;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
import com.cs5412.dataobjects.TaskDao;
import com.cs5412.filesystem.IFileSystem;

import com.cs5412.taskmanager.TaskManager;
import com.cs5412.taskmanager.TaskStatus;
import com.cs5412.utils.ServerConstants;
import com.cs5412.webservices.fileupload.FileUploadServlet;

@Path("/svm")
public class SVMService{
	static final Logger LOG = LoggerFactory.getLogger(FileUploadServlet.class);
	static final int NUM_MODELS = 5;
	@Context ServletContext context;

	public static String LoadBalancerAddress = "http://localhost:1246";
	TaskManager taskManager;
	IFileSystem fs;
	
	@PostConstruct
    void initialize() {
		taskManager = new TaskManager((CouchbaseClient)context.getAttribute("couchbaseClient"));
		fs = (IFileSystem) context.getAttribute("fileSystem");

    }
	
/*	@Path("/runDistributedService")
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
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
		
		String taskUrl = LoadBalancerAddress + "/elasticMLCompute/ml/svm/crossvalidation/createfiles" + "/" + username + "/" + trainingDataset;
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
        int cores = Runtime.getRuntime().availableProcessors();
        LOG.debug("Cores:"+cores);
        
        ExecutorService es = Executors.newFixedThreadPool(2);
        for(int i=1;i<=NUM_MODELS;i++){
			for(int j=0;j<=6;j++) {
				Runnable job = new ModelCreationJob(i,j,username,LoadBalancerAddress);
				Thread modelThread = new Thread(job);
				es.execute(modelThread);
			}
		}
        es.shutdown();
        
        try {
			es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();			  
		}
        if(es.isTerminated()) LOG.debug("Models created");
        
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
        httpclient.start();

        for(int i=1;i<=NUM_MODELS;i++){
			for(int j=0;j<=6;j++) {
		        // Execute request
		        final HttpGet request1 = new HttpGet(LoadBalancerAddress + "/elasticMLCompute/ml/svm/crossvalidation/createmodel" + "/" + username + "/" + j + "/" + i);
		        Future<HttpResponse> future = httpclient.execute(request1, null);
		        //HttpResponse response1 = future.get();
		        LOG.debug(i+"|"+j);
				
			}
		}
        return Response.status(200).entity("").build();
	}*/

	
	@Path("/runService")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response runService(
			@FormParam("trainingDataset") String trainingDataset,
			@FormParam("testDataset") String testDataset,

			@Context ServletContext context,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response

			) throws Exception {
		
		LOG.debug("Using "+trainingDataset+" dataset for SVM");
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");

		
		String crossvalidation = fs.getUserPath(username)+ServerConstants.linuxSeparator+"work"+ServerConstants.linuxSeparator+"crossvalidation";
		String modelPath = crossvalidation+ServerConstants.linuxSeparator+"model"+ServerConstants.linuxSeparator;

	

		String trainFile = fs.getFilePathForUploads(trainingDataset, username);
		String testFile = fs.getFilePathForUploads(testDataset, username);
		
		TaskDao cvfileCreationTask = new TaskDao(username, "cvfileCreationTask", "cvfilecreation", TaskStatus.RUNNING, false);
		try{
			taskManager.registerTask(cvfileCreationTask);
			CrossValidationFiles.createFiles(trainFile, fs,crossvalidation);
			taskManager.setTaskStatus(cvfileCreationTask, TaskStatus.SUCCESS);
		}catch(Exception e){
			taskManager.setTaskStatus(cvfileCreationTask, TaskStatus.FAILURE);
			e.printStackTrace();
		}
		TaskDao cvmodelCreationTask = new TaskDao(username, "cvmodelCreationTask", "cvmodelcreation", TaskStatus.RUNNING, false);
		LOG.debug("Creating Models");
		//ExecutorService es = Executors.newFixedThreadPool(2);
		try{
			taskManager.registerTask(cvmodelCreationTask);
			for(int i=1;i<=NUM_MODELS;i++){
				for(int j=0;j<=6;j++) {
					Runnable job = new ModelCreationJob(i,j,crossvalidation,modelPath,fs);
					Thread modelThread = new Thread(job);
					//es.execute(modelThread);
					LOG.debug("Creating Model"+i+j);
					//modelThread.start();
					modelThread.run();
				}
			}
			/*es.shutdown();
			try {
			
				es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			
			} catch (InterruptedException e) {
				LOG.error("Timeout", e);
			  
			}*/
		//	if(es.isTerminated())
				LOG.debug("Finished Creating Models");
				taskManager.setTaskStatus(cvmodelCreationTask, TaskStatus.SUCCESS);
		}catch(Exception e){
			taskManager.setTaskStatus(cvmodelCreationTask, TaskStatus.FAILURE);
			LOG.error("Error", e);
		}
		
		String bestC = "3";
		TaskDao bestCTask = new TaskDao(username, "bestCTask", "bestC", TaskStatus.RUNNING, false);
		try{
			taskManager.registerTask(bestCTask);
			LOG.debug("Calculating bestC");
			bestC = Classifier.valClassifyCaller(fs,crossvalidation,modelPath,"testOutput.txt");
			LOG.debug("bestC="+bestC.split(" ")[0]);
			taskManager.setTaskStatus(bestCTask, TaskStatus.SUCCESS);
		}catch(Exception e){
			taskManager.setTaskStatus(bestCTask, TaskStatus.FAILURE);
			LOG.error("Error", e);
		}
		TaskDao testClassificationTask = new TaskDao(username, "testClassificationTask", "testClassification", TaskStatus.RUNNING, false);
		try{
			taskManager.registerTask(testClassificationTask);
			TestClassification.testClassify(Integer.parseInt(bestC.split(" ")[0]), "0", fs,username,trainFile,"cv.txt",testFile);
			taskManager.setTaskStatus(testClassificationTask, TaskStatus.SUCCESS);
		}catch(Exception e){
			taskManager.setTaskStatus(testClassificationTask, TaskStatus.FAILURE);
			LOG.error("Error", e);
		}
		
		TaskDao reportGenerationTask = new TaskDao(username, "reportGenerationTask", "reportGeneration", TaskStatus.RUNNING, false);
		JSONArray result = new JSONArray();
		try{
			taskManager.registerTask(reportGenerationTask);
			ArrayList<ArrayList<Double>> yVal = Classifier.valAccuracies;
			ArrayList<Double> avgValAccuracies = Classifier.avgValAccuracies;
			double xVal[] = TestClassification.C;
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
					dataPoint.put(Math.log(10000*xVal[i]));
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
				dataPoint.put(Math.log(10000*xVal[i]));
				dataPoint.put(yList);
				dataPoints.put(dataPoint);
				i++;		
			}
			result.put(dataPoints);
			String filePath = fs.getUserPath(username)+ServerConstants.linuxSeparator+"reports"+ServerConstants.linuxSeparator+ trainingDataset+"-"+testDataset+IFileSystem.CHART_DATA_FORMAT;
			BufferedWriter bw = fs.createFileToWrite(filePath,true);
			bw.write(result.toString());
			bw.close();
			LOG.debug("FINISHED SVM EXECUTION for"+trainingDataset+"|"+testDataset);
			taskManager.setTaskStatus(reportGenerationTask, TaskStatus.SUCCESS);
		}catch(Exception e){
			LOG.error("Error" + e);
			taskManager.setTaskStatus(reportGenerationTask, TaskStatus.FAILURE);
		}
		return Response.status(200).entity(result.toString()).build();
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
		String path = fs.getUserPath(username)+ServerConstants.linuxSeparator+"reports"+ServerConstants.linuxSeparator+reportId;
		return Response.status(200).entity(fs.readFileToString(path)).build();
	}
	
	@Path("/crossvalidation/createfiles/{username}/{trainingDataSet}")
	@GET
	public Response createSVMFiles(

			@PathParam("username") String username,
			@PathParam("trainingDataSet") String trainingDataset,
	

			@FormParam("testDataset") String testDataset,
			@Context HttpServletRequest request
			

			) throws Exception {



		HttpSession session = request.getSession(false);
		//String username = (String) session.getAttribute("user");

		LOG.debug("Using "+trainingDataset+" dataset for SVM");
		String trainFile = fs.getFilePathForUploads(trainingDataset, username);
		String crossvalidation = fs.getUserPath(username)+ServerConstants.linuxSeparator+"work"+ServerConstants.linuxSeparator+"crossvalidation";
		CrossValidationFiles.createFiles(trainFile, fs, crossvalidation);
		return Response.status(200).entity("Hello").build();
	}
	
	
	@Path("/crossvalidation/createmodel/{username}/{c}/{fileNum}")
	@GET
	public Response createModel(
			@PathParam("username") String username,
			@PathParam("c") int c,
			@PathParam("fileNum") int fileNum) throws Exception {
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
		LOG.debug("Creating model: c: " + c + " fileNum: " + fileNum);
		String crossvalidation = fs.getUserPath(username)+ServerConstants.linuxSeparator+"work"+ServerConstants.linuxSeparator+"crossvalidation";
		String modelPath = crossvalidation+ServerConstants.linuxSeparator+"model"+ServerConstants.linuxSeparator;
		Model.create(crossvalidation +File.separator+ "SVM" , fileNum, c, "0", modelPath,fs);
		LOG.debug("Created Model"+c+fileNum);
		return Response.status(200).entity("Hello").build();
	}
	/*
	@Path("/crossvalidation/bestTradeOff")
	@GET
	public Response crossvalidation(
			@Context ServletContext context
			){
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
		String username="admin";
		String returnValue = Classifier.valClassifyCaller(fs,username, WebAppListener.output + "testOutput.txt");
	
		return Response.status(200).entity(returnValue).build();
	}
	
	@Path("/crossvalidation/predictTest/{c}")
	@GET
	public Response testPrediction(
			@PathParam("c") int c){
		TestClassification.testClassify(c, "0", WebAppListener.crossvalidation + "SVM.train", WebAppListener.output + "cv.txt", WebAppListener.crossvalidation + "SVM.test");
		return Response.status(200).entity("Hello").build();
	}

	*/
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
