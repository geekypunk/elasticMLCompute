package com.cs5412.webservices.ml;

import java.io.File;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cs5412.filesystem.IFileSystem;
import com.cs5412.filesystem.impl.FileSystemImpl;
import com.cs5412.utils.ServerConstants;
import com.cs5412.webservices.fileupload.FileUploadServlet;

@Path("/svm")
public class SVMService{
	static final Logger LOG = LoggerFactory.getLogger(FileUploadServlet.class);
	static final int NUM_MODELS = 5;
	IFileSystem fs = new FileSystemImpl();
	@Path("/runService")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response runService(
			@FormParam("trainingDataset") String trainingDataset,
			@FormParam("testDataset") String testDataset
			
			) throws Exception {
		LOG.debug("Using "+trainingDataset+" dataset for SVM");
		CrossValidationFiles.createFiles(ServerConstants.UPLOAD_DIRECTORY_TRAIN+File.separator+trainingDataset, MyListener.crossvalidation);
		LOG.debug("Creating Models");
		//ExecutorService es = Executors.newCachedThreadPool();
		try{
			for(int i=1;i<=NUM_MODELS;i++){
				for(int j=0;j<=6;j++) {
					Runnable job = new ModelCreationJob(i,j);
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
		String bestC = Classifier.valClassifyCaller(MyListener.model, MyListener.crossvalidation, MyListener.output + "testOutput.txt");
		LOG.debug("bestC="+bestC.split(" ")[0]);
		TestClassification.testClassify(Integer.parseInt(bestC.split(" ")[0]), "0", ServerConstants.UPLOAD_DIRECTORY_TRAIN+File.separator+trainingDataset, MyListener.output + "cv.txt", ServerConstants.UPLOAD_DIRECTORY_TEST+File.separator+testDataset);
		LOG.debug("FINISHED SVM EXECUTION for"+trainingDataset+"|"+testDataset);
		return Response.status(200).entity("Hello").build();
	}
	
	
	@Path("/crossvalidation/createfiles")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response createSVMFiles(
			@FormParam("trainingDataset") String trainingDataset,
			@FormParam("testDataset") String testDataset
			
			) throws Exception {
		LOG.debug("Using "+trainingDataset+" dataset for SVM");
		CrossValidationFiles.createFiles(ServerConstants.UPLOAD_DIRECTORY_TRAIN+File.separator+trainingDataset, MyListener.crossvalidation);
		return Response.status(200).entity("Hello").build();
	}
	
	@Path("/crossvalidation/createmodel/{c}/{fileNum}")
	@GET
	public Response createModel(
			@PathParam("c") int c,
			@PathParam("fileNum") int fileNum) throws Exception {
		Model.create(MyListener.crossvalidation + "SVM" , fileNum, c, "0", MyListener.model);
		LOG.debug("Created Model"+c+fileNum);
		return Response.status(200).entity("Hello").build();
	}
	
	@Path("/crossvalidation/bestTradeOff")
	@GET
	public Response crossvalidation(){
		String returnValue = Classifier.valClassifyCaller(MyListener.model, MyListener.crossvalidation, MyListener.output + "testOutput.txt");
	
		return Response.status(200).entity(returnValue).build();
	}
	
	@Path("/crossvalidation/predictTest/{c}")
	@GET
	public Response testPrediction(
			@PathParam("c") int c){
		TestClassification.testClassify(c, "0", MyListener.crossvalidation + "SVM.train", MyListener.output + "cv.txt", MyListener.crossvalidation + "SVM.test");
		return Response.status(200).entity("Hello").build();
	}

	
	@Path("/getTrainingDataSets")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTrainingDataSets() throws Exception {
		Collection<File> files = fs.getUploadedTrainingDatasets();
		JSONArray filesJson = new JSONArray();
		JSONObject jsonFile;
		for(File file : files){
			try {
				jsonFile = new JSONObject();
				jsonFile.put("optionValue", file.getName());
				jsonFile.put("optionDisplay", file.getName());
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
	public Response getTestDataSets() throws Exception {
		Collection<File> files = fs.getUploadedTestDatasets();
		JSONArray filesJson = new JSONArray();
		JSONObject jsonFile;
		for(File file : files){
			try {
				jsonFile = new JSONObject();
				jsonFile.put("optionValue", file.getName());
				jsonFile.put("optionDisplay", file.getName());
				filesJson.put(jsonFile);
			} catch (JSONException e) {
				LOG.error("Error", e);
			}
			
		}
		return Response.status(200).entity(filesJson.toString()).build();
	}
	

}
