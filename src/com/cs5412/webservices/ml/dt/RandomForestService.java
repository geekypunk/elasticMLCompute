package com.cs5412.webservices.ml.dt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
import com.cs5412.utils.TaskType;
import com.cs5412.webservices.fileupload.FileUploadServlet;

@Path("/dTree")
public class RandomForestService {
	static final Logger LOG = LoggerFactory.getLogger(FileUploadServlet.class);
	public int bestHeight;
	public static int [] choplist = {2,3,5,10,50,80};
	public static String LoadBalancerAddress = "http://10.32.32.7:8181";
	
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
		TaskManager taskManager = new TaskManager((CouchbaseClient)context.getAttribute("couchbaseClient"));
		String taskUrl = LoadBalancerAddress + "/elasticMLCompute/ml/dTree/beginService" + "/" + username + "/" + trainingDataset;
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
        ArrayList<ArrayList<Double>> allAccuracies = new ArrayList<ArrayList<Double>>();
        ExecutorService es = Executors.newFixedThreadPool(5);
        TaskDao dtTask = new TaskDao(username, trainingDataset, "upload", TaskStatus.RUNNING, false);
    	//uploadTask.setHttpRequest(request);
    	dtTask.setTaskType(TaskType.ALGORITHM_EXEC);
    	dtTask.setTaskDescription("Decision Tree algorithm");
    	taskManager.registerTask(dtTask);
        for(int i=1;i<=5;i++){
        	Runnable job = new ParallelDTJob(username, LoadBalancerAddress, i, LOG, allAccuracies);
        	Thread t = new Thread(job);
			es.execute(t);
        }
        es.shutdown();
        
        try {
			es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();			  
		}
        
        if(es.isTerminated()){
        	taskUrl = LoadBalancerAddress + "/elasticMLCompute/ml/dTree/calcBestService" + "/" + username;
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
	        String returnString = "";
	        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
	        	returnString = readStream(conn.getInputStream());
	        }
	        LOG.debug(returnString);
	        
	        String[] splitStr = returnString.split(" ");
	        String bh = splitStr[0];
	        ArrayList<Double> acc = new ArrayList<Double>();
	        for(int j=1;j<=5;j++){
	        	acc.add(Double.parseDouble(splitStr[j]));
	        }
	        
			taskUrl = LoadBalancerAddress + "/elasticMLCompute/ml/dTree/AccuracyService" + "/" + username + "/" + bh + "/" + trainingDataset + "/" + testDataset;
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
	        
			JSONArray result = new JSONArray();
			try{
				ArrayList<ArrayList<Double>> yVal = allAccuracies;
				ArrayList<Double> avgValAccuracies = acc;
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
				String filePath = fs.getUserPath(username)+ServerConstants.linuxSeparator+"reports"+ServerConstants.linuxSeparator+ trainingDataset+"-"+testDataset+IFileSystem.CHART_DATA_FORMAT;
				BufferedWriter bw = fs.createFileToWrite(filePath,true);
				bw.write(result.toString());
				bw.close();
			}catch(Exception e){
				LOG.debug("Error: " + e);
			}
        }
        taskManager.setTaskStatus(dtTask, TaskStatus.SUCCESS);
        return Response.status(200).entity(conn.getResponseCode()).build();
	}
	
	
/*	@Path("/runService")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response runService(
			@FormParam("trainingDataset") String trainingDataset,
			@FormParam("testDataset") String testDataset,
			@Context ServletContext context,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response
			){
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
		LOG.debug("Using "+trainingDataset+" dataset for SVM");
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		
		String crossvalidation = fs.getUserPath(username)+ServerConstants.linuxSeparator+"dt"+ServerConstants.linuxSeparator+"work"+ServerConstants.linuxSeparator+"crossvalidation";
		String trainFile = fs.getFilePathForUploads(trainingDataset, username);
		String testFile = fs.getFilePathForUploads(testDataset, username);
		
		CrossValidation CV = new CrossValidation();
		CV.createCrossValidationFiles(trainFile, fs, crossvalidation);
		ExecuteMain EM = new ExecuteMain();
		for(int i=1;i<=5;i++){
			String partTrainFile = crossvalidation + ServerConstants.linuxSeparator + "trainFile" + i + ".txt";
			String partValFile = crossvalidation + ServerConstants.linuxSeparator + "validFile" + i + ".txt";
			String outputFile = crossvalidation + ServerConstants.linuxSeparator + "output" + i + ".txt";
			EM.Construct(partTrainFile, partValFile, outputFile, fs, accuracies);
		}
		BestHeight BH = new BestHeight();
		bestHeight= BH.calculateAccuracy(crossvalidation, fs, avgAcc);
		
		EM.FinalAccuracy(trainFile,testFile,bestHeight, fs, crossvalidation);
		
		JSONArray result = new JSONArray();
		try{
			ArrayList<ArrayList<Double>> yVal = accuracies;
			ArrayList<Double> avgValAccuracies = avgAcc;
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
			String filePath = fs.getUserPath(username)+ServerConstants.linuxSeparator+"dt"+ServerConstants.linuxSeparator+"reports"+ServerConstants.linuxSeparator+ trainingDataset+"-"+testDataset+IFileSystem.CHART_DATA_FORMAT;
			BufferedWriter bw = fs.createFileToWrite(filePath,true);
			bw.write(result.toString());
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return Response.status(200).entity("Hello World").build();
	}*/
	
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
		String path = fs.getUserPath(username)+ServerConstants.linuxSeparator+"dt"+ServerConstants.linuxSeparator+"reports"+ServerConstants.linuxSeparator+reportId;
		return Response.status(200).entity(fs.readFileToString(path)).build();
	}
	
	@Path("/beginService/{username}/{trainingDataSet}")
	@GET
	public Response beginService(
			@PathParam("username") String username,
			@PathParam("trainingDataSet") String trainingDataset,
			@Context ServletContext context
			){
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
		LOG.debug("Using "+trainingDataset+" to begin the service");
		String crossvalidation = fs.getUserPath(username)+ServerConstants.linuxSeparator+"dt"+ServerConstants.linuxSeparator+"work"+ServerConstants.linuxSeparator+"crossvalidation";
		String trainFile = fs.getFilePathForUploads(trainingDataset, username);
		
		CrossValidation CV = new CrossValidation();
		CV.createCrossValidationFiles(trainFile, fs, crossvalidation);
		return Response.status(200).entity("Hello World1").build();
	}
	
	@Path("/generateEachService/{username}/{i}")
	@GET
	public Response generateEachService(
			@PathParam("username") String username,
			@PathParam("i") String i,
			@Context ServletContext context
			){
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
		LOG.debug("Generating "+ i +" service");
		String crossvalidation = fs.getUserPath(username)+ServerConstants.linuxSeparator+"dt"+ServerConstants.linuxSeparator+"work"+ServerConstants.linuxSeparator+"crossvalidation";
		ArrayList<Double> accuracy = new ArrayList<Double>();
		
		ExecuteMain EM = new ExecuteMain();
		String partTrainFile = crossvalidation + ServerConstants.linuxSeparator + "trainFile" + i + ".txt";
		String partValFile = crossvalidation + ServerConstants.linuxSeparator + "validFile" + i + ".txt";
		String outputFile = crossvalidation + ServerConstants.linuxSeparator + "output" + i + ".txt";
		EM.Construct(partTrainFile, partValFile, outputFile, fs, accuracy);
		String str = "";
		for(int j=0;j<accuracy.size();j++){
			str += accuracy.get(j) + " ";
		}
		return Response.status(200).entity(str).build();
	}
	
	@Path("/calcBestService/{username}")
	@GET
	public Response calcBestService(
			@PathParam("username") String username,
			@Context ServletContext context
			){	
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
		LOG.debug("Calculating the best service");
		String crossvalidation = fs.getUserPath(username)+ServerConstants.linuxSeparator+"dt"+ServerConstants.linuxSeparator+"work"+ServerConstants.linuxSeparator+"crossvalidation";
		ArrayList<Double> avgAcc = new ArrayList<Double>();
		
		BestHeight BH = new BestHeight();
		bestHeight = BH.calculateAccuracy(crossvalidation, fs, avgAcc);
		String str = "";
		for(int j=0;j<avgAcc.size();j++){
			str += avgAcc.get(j) + " ";
		}
		return Response.status(200).entity(bestHeight + " " + str).build();
	}
	
	@Path("/AccuracyService/{username}/{bh}/{trainingDataset}/{testingDataset}")
	@GET
	public Response AccuracyService(
			@PathParam("username") String username,
			@PathParam("bh") String bh,
			@PathParam("trainingDataset") String trainingDataset,
			@PathParam("testingDataset") String testingDataset,
			@Context ServletContext context
			){	
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
		LOG.debug("Accuracy Service");
		String crossvalidation = fs.getUserPath(username)+ServerConstants.linuxSeparator+"dt"+ServerConstants.linuxSeparator+"work"+ServerConstants.linuxSeparator+"crossvalidation";
		String trainFile = fs.getFilePathForUploads(trainingDataset, username);
		String testFile = fs.getFilePathForUploads(testingDataset, username);
		ExecuteMain EM = new ExecuteMain();
		EM.FinalAccuracy(trainFile,testFile,Integer.parseInt(bh), fs, crossvalidation);
		return Response.status(200).entity("Hello World").build();
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
	
	private static String readStream(InputStream in) {
	    BufferedReader reader = null;
	    StringBuilder builder = new StringBuilder();
	    try {
	        reader = new BufferedReader(new InputStreamReader(in));
	        String line = "";
	        while ((line = reader.readLine()) != null) {
	            builder.append(line);
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        if (reader != null) {
	            try {
	                reader.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    return builder.toString();
	}
}

