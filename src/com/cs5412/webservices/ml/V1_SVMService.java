package com.cs5412.webservices.ml;

import java.io.*;
import java.util.Collection;

import javax.mail.BodyPart;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import jnisvmlight.client.*;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cs5412.filesystem.impl.FileSystemImpl;
import com.cs5412.utils.ServerConstants;
import com.cs5412.webservices.fileupload.FileUploadServlet;

@Path("/svm")
public class V1_SVMService{
	static final Logger LOG = LoggerFactory.getLogger(FileUploadServlet.class);
	private void writeFile(byte[] content, String filename) throws IOException 
	{
		File file = new File(filename);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fop = new FileOutputStream(file);
		fop.write(content);
		fop.flush();
		fop.close();
	}
	FileSystemImpl fs = new FileSystemImpl();
	@Path("/upload")
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes("multipart/form-data")
	public Response storeSVM(javax.mail.internet.MimeMultipart multiPart) throws Exception {
		BodyPart bp1 = multiPart.getBodyPart(0);
		BodyPart bp2 = multiPart.getBodyPart(1);
		InputStream inputStream = bp1.getInputStream();
		byte[] bytes = IOUtils.toByteArray(inputStream);
		String fileName = bp1.getFileName();
		fileName = MyListener.crossvalidation + fileName;
		writeFile(bytes, fileName);
		
		inputStream = bp2.getInputStream();
		bytes = IOUtils.toByteArray(inputStream);
		fileName = bp2.getFileName();
		fileName = MyListener.crossvalidation  + fileName;
		writeFile(bytes, fileName);
		
		System.out.println("Success !!!!!");
		return Response.status(200).entity("Uploaded").build();
	}
	
	@Path("/crossvalidation/createfiles")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response createSVMFiles(
			@FormParam("trainingDataset") String trainingDataset,
			@FormParam("testDataset") String testDataset
			
			) throws Exception {
		LOG.debug("Using "+trainingDataset+" dataset for SVM");
		CrossValidationFiles.createFiles(ServerConstants.UPLOAD_DIRECTORY+File.separator+trainingDataset, MyListener.crossvalidation);
		return Response.status(200).entity("Hello").build();
	}
	
	@Path("/getDataSets")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDataSets() throws Exception {
		Collection<File> files = fs.getUploadedDatasets();
		JSONArray filesJson = new JSONArray();
		JSONObject jsonFile;
		for(File file : files){
			try {
				jsonFile = new JSONObject();
				jsonFile.put("optionValue", file.getName());
				jsonFile.put("optionDisplay", file.getName());
				filesJson.put(jsonFile);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return Response.status(200).entity(filesJson.toString()).build();
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


}
