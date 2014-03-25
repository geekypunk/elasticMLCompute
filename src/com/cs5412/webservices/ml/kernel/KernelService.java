package com.cs5412.webservices.ml.kernel;

import java.io.BufferedWriter;
import java.io.File;
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

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.LocatedFileStatus;
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

@Path("/kernel")
public class KernelService {
	static final Logger LOG = LoggerFactory.getLogger(FileUploadServlet.class);
	static final int NUM_MODELS = 5;
	@Context ServletContext context;
	TaskManager taskManager;
	
	@PostConstruct
    void initialize() {
		taskManager = new TaskManager((CouchbaseClient)context.getAttribute("couchbaseClient"));

    }
	
	@Path("/runService/{kernelType}/{kernelParam}")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response runService(
			@FormParam("trainingDataset") String trainingDataset,
			@FormParam("testDataset") String testDataset,
			@Context ServletContext context,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response,
			@PathParam("kernelType") String kernelType,
			@PathParam("kernelParam") String kernelParam
			) throws Exception {
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
		LOG.debug("Using "+trainingDataset+" dataset for Kernel");
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		String kernelNum = "-1";
		if(kernelType.equals("polyKernel")) kernelNum = "1";
		else if(kernelType.equals("rbFunction")) kernelNum = "2";
		else if(kernelType.equals("sigmoidFunction")) kernelNum = "3";
		LOG.debug(kernelNum);
		String crossvalidation = fs.getUserPath(username)+ServerConstants.linuxSeparator+"kernel"+ServerConstants.linuxSeparator+"work"+ServerConstants.linuxSeparator+"crossvalidation";
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
					Runnable job = new ModelCreationJob(i,j,crossvalidation,modelPath,fs,kernelNum,kernelParam);
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
			  
			}
			if(es.isTerminated())*/
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
			TestClassification.testClassify(Integer.parseInt(bestC.split(" ")[0]), kernelNum, fs ,trainFile,"cv.txt",testFile, kernelParam);
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
			String filePath = fs.getUserPath(username)+ServerConstants.linuxSeparator+"kernel"+ServerConstants.linuxSeparator+"reports"+ServerConstants.linuxSeparator+ trainingDataset+"-"+testDataset+IFileSystem.CHART_DATA_FORMAT;
			BufferedWriter bw = fs.createFileToWrite(filePath,true);
			bw.write(result.toString());
			bw.close();
			LOG.debug("FINISHED KERNEL EXECUTION for"+trainingDataset+"|"+testDataset);
			taskManager.setTaskStatus(reportGenerationTask, TaskStatus.SUCCESS);
		}catch(Exception e){
			LOG.error("Error" + e);
			taskManager.setTaskStatus(reportGenerationTask, TaskStatus.FAILURE);
		}
		return Response.status(200).entity("Hello").build();
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
		String path = fs.getUserPath(username)+ServerConstants.linuxSeparator+"kernel"+ServerConstants.linuxSeparator+"reports"+ServerConstants.linuxSeparator+reportId;
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
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
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
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
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
