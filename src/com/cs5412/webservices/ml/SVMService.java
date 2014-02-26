package com.cs5412.webservices.ml;

import java.io.BufferedWriter;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
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

import com.cs5412.filesystem.IFileSystem;
import com.cs5412.utils.ServerConstants;
import com.cs5412.webservices.fileupload.FileUploadServlet;

@Path("/svm")
public class SVMService{
	static final Logger LOG = LoggerFactory.getLogger(FileUploadServlet.class);
	static final int NUM_MODELS = 5;
	
	@Path("/runService")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response runService(
			@FormParam("trainingDataset") String trainingDataset,
			@FormParam("testDataset") String testDataset,
			@Context ServletContext context
			) throws Exception {
		
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
		LOG.debug("Using "+trainingDataset+" dataset for SVM");
		String username="admin";
		String crossvalidation = fs.getUserPath(username)+File.separator+"work"+File.separator+"crossvalidation";
		String modelPath = crossvalidation+File.separator+"model"+File.separator;
		String trainFile = fs.getFilePathForUploads(trainingDataset, username);
		String testFile = fs.getFilePathForUploads(testDataset, username);
		CrossValidationFiles.createFiles(trainFile, fs,crossvalidation);
		LOG.debug("Creating Models");
		//ExecutorService es = Executors.newFixedThreadPool(2);
		try{
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
			  
			}
			if(es.isTerminated())*/
				LOG.debug("Finished Creating Models");
		}catch(Exception e){
			LOG.error("Error", e);
		}
		
		LOG.debug("Calculating bestC");
		String bestC;
		bestC = Classifier.valClassifyCaller(fs,crossvalidation,modelPath,"testOutput.txt");
		bestC = "3 0";
		LOG.debug("bestC="+bestC.split(" ")[0]);
		TestClassification.testClassify(Integer.parseInt(bestC.split(" ")[0]), "0", fs,username,trainFile,"cv.txt",testFile);
		ArrayList<ArrayList<Double>> yVal = Classifier.valAccuracies;
		ArrayList<Double> avgValAccuracies = Classifier.avgValAccuracies;
		double xVal[] = TestClassification.C;
		JSONArray result = new JSONArray();
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
		String filePath = fs.getUserPath(username)+File.separator+"reports"+File.separator+ trainingDataset+"-"+testDataset+IFileSystem.CHART_DATA_FORMAT;
		BufferedWriter bw = fs.createFileToWrite(filePath);
		bw.write(result.toString());
		bw.close();
		LOG.debug("FINISHED SVM EXECUTION for"+trainingDataset+"|"+testDataset);
		return Response.status(200).entity(result.toString()).build();
	}
	
	@Path("/getReport/{reportId}")
	@GET
	public Response getReportData(
			@PathParam("reportId") String reportId,
			@Context ServletContext context
			) throws Exception {
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
		String username="admin";
		LOG.debug("Reading report"+reportId);
		String path = fs.getUserPath(username)+File.separator+"reports"+File.separator+reportId;
		return Response.status(200).entity(fs.readFileToString(path)).build();
	}
	
	@Path("/crossvalidation/createfiles")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response createSVMFiles(
			@FormParam("trainingDataset") String trainingDataset,
			@FormParam("testDataset") String testDataset,
			@Context ServletContext context
			
			) throws Exception {
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
		String username="admin";
		LOG.debug("Using "+trainingDataset+" dataset for SVM");
		CrossValidationFiles.createFiles(trainingDataset, fs,username);
		return Response.status(200).entity("Hello").build();
	}
	/*
	@Path("/crossvalidation/createmodel/{c}/{fileNum}")
	@GET
	public Response createModel(
			@PathParam("c") int c,
			@PathParam("fileNum") int fileNum) throws Exception {
		Model.create(WebAppListener.crossvalidation + "SVM" , fileNum, c, "0", WebAppListener.model);
		LOG.debug("Created Model"+c+fileNum);
		return Response.status(200).entity("Hello").build();
	}
	
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
			@Context ServletContext context
			) throws Exception {
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
		String username="admin";
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
			@Context ServletContext context
			) throws Exception {
		IFileSystem fs = (IFileSystem) context.getAttribute("fileSystem");
		String username="admin";
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
