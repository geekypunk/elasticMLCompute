package com.cs5412.webservices.ml.knn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
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

import com.cs5412.filesystem.IFileSystem;
import com.cs5412.utils.ServerConstants;
import com.cs5412.webservices.fileupload.FileUploadServlet;

@Path("/knn")
public class KNNService{
	static final Logger LOG = LoggerFactory.getLogger(FileUploadServlet.class);
	static final int NUM_MODELS = 5;
	@Context ServletContext context;
	IFileSystem fs;
	
	@PostConstruct
    void initialize() {
		fs = (IFileSystem) context.getAttribute("fileSystem");
    }
	
	@Path("/runService")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response runService(
			@FormParam("trainingDataset") String trainingDataset,
			@FormParam("testDataset") String testDataset,
			@Context HttpServletRequest request
			
			) throws Exception {
		LOG.debug("Using "+trainingDataset+" dataset for KNN");
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		String trainFile = fs.getFilePathForUploads(trainingDataset, username);
		String testFile = fs.getFilePathForUploads(testDataset, username);
		String resultFile = fs.getUserPath(username)+File.separator+"reports"+File.separator+trainingDataset+"-"+testDataset+".output";
		String workDir = fs.getUserPath(username)+File.separator+ServerConstants.KNN_CV_BASE_DIR;
		KNN knnService = new KNN(trainFile, testFile, resultFile,workDir, fs);
		knnService.runKNN();
		String result=getCrossValidationResults(fs,resultFile);
		
		return Response.status(200).entity(result).build();
	}

	private String getCrossValidationResults(IFileSystem fs,String resultFile) {
		StringBuffer result = new StringBuffer();
		try {
			InputStream fin = (InputStream) fs.readFile(resultFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
			String line = null;
			while((line=reader.readLine())!=null){
				result.append(line+" ");
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result.toString();
	}


	@Path("/getTrainingDataSets")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTrainingDataSets(
			@Context HttpServletRequest request
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
			@Context HttpServletRequest request
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
	@Path("/getReport/{reportId}")
	@GET
	public Response getReportData(
			@PathParam("reportId") String reportId,
			@Context HttpServletRequest request
			) throws Exception {
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		LOG.debug("Reading report"+reportId);
		String path = fs.getUserPath(username)+File.separator+"reports"+File.separator+reportId;
		return Response.status(200).entity(fs.readFileToString(path)).build();
	}

}
