
package com.cs5412.webservices.fileupload;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.couchbase.client.CouchbaseClient;
import com.cs5412.filesystem.IFileSystem;
import com.cs5412.taskmanager.TaskDao;
import com.cs5412.taskmanager.TaskManager;
import com.cs5412.taskmanager.TaskStatus;
import com.cs5412.taskmanager.TaskType;
import com.cs5412.utils.HTTPConstants;
import com.cs5412.utils.Utils;

/**
 * <p><b>Handles file upload from client.</b></p>
 * 
 * @author kt466
 */
@WebServlet("/FileUpload")
public class FileUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static final Logger LOG = LoggerFactory.getLogger(FileUploadServlet.class);
   
    ServletContext ctx;
    IFileSystem fs;
    TaskManager taskManager;
    PropertiesConfiguration config;
    ProgressListener progressListener = new ProgressListener(){
    	   public void update(long pBytesRead, long pContentLength, int pItems) {
    	       LOG.info("We are currently reading item " + pItems);
    	       if (pContentLength == -1) {
    	    	   LOG.info("So far, " + pBytesRead + " bytes have been read.");
    	       } else {
    	    	   LOG.info("So far, " + pBytesRead + " of " + pContentLength
    	                              + " bytes have been read.");
    	       }
    	   }
    };
    
    //Initialize global objects
    public void init(ServletConfig servConfig)
            throws ServletException{
    	super.init(servConfig);
    	ctx = getServletContext();
    	fs = (IFileSystem) ctx.getAttribute("fileSystem");
		taskManager = new TaskManager((CouchbaseClient)ctx.getAttribute("couchbaseClient"));
		config = (PropertiesConfiguration)ctx.getAttribute("config");
    }

    /**
     * Upon receiving file upload submission, parses the request to read
     * upload data and saves the file on disk.
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
    	HttpSession session = request.getSession();
    	String username = (String)session.getAttribute("user");
    	// checks if the request actually contains upload file
        if (!ServletFileUpload.isMultipartContent(request)) {
            // if not, we stop here
            PrintWriter writer = response.getWriter();
            writer.println("Error: Form must has enctype=multipart/form-data.");
            writer.flush();
            return;
        }
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");
        JSONObject result = new JSONObject();
        try {
	    
        	JSONArray uploadedFiles = createJsonArrayForUploads(fs, username);
	        
	        // configures upload settings
	        DiskFileItemFactory factory = new DiskFileItemFactory();
	        // sets memory threshold - beyond which files are stored in disk, controls in-memory storage 
	        factory.setSizeThreshold(config.getInt("MEMORY_THRESHOLD"));
	        // sets temporary location to store files
	        factory.setRepository(FileUtils.getTempDirectory());
	 
	        ServletFileUpload upload = new ServletFileUpload(factory);
	         
	        // sets maximum size of upload file
	        upload.setFileSizeMax(config.getInt("MAX_FILE_SIZE"));
	         
	        // sets maximum size of request (include file + form data)
	        upload.setSizeMax(config.getInt("MAX_REQUEST_SIZE"));
	        upload.setProgressListener(progressListener);
	 
            List<FileItem> formItems = upload.parseRequest(request);
            System.out.println("Sending file");
            if (formItems != null && formItems.size() > 0) {
                // iterates over form's fields
                for (FileItem item : formItems) {
                	String name = item.getFieldName();
                    // processes only fields that are not form fields
                	if (item.isFormField()) {
                        System.out.println("Form field " + name);
                    } 
                	else {
                        System.out.println("File field " + name + " with file name "
                            + item.getName() + " detected.");
                        String fileName = new File(item.getName()).getName();
                        
                        if("s3".equalsIgnoreCase(extensionOf(fileName))){
                        	
                        	InputStream uploadedStream = item.getInputStream();
                            BufferedReader br = new BufferedReader(new InputStreamReader(uploadedStream));
                            String line = null;
                            List<String> urls = new ArrayList<String>();
                            
                            while((line=br.readLine())!=null){
                            	urls.add(line);
                            }

                            AmazonS3 s3Client = new AmazonS3Client();
                            JSONArray array = new JSONArray(); 				//filesUploadResult
                            StringBuffer message = new StringBuffer();
                            
                            for(String fileLocationURL: urls){
	                            String[] tokens = getS3FileParameters(fileLocationURL);
	                         	String bucketName = tokens[0];
	                       		String s3FileName = tokens[1];
	                       		
	                       		//s3 file upload start
	                       		if(isAlreadyUploaded(s3FileName,uploadedFiles)){
	                       			JSONObject jsono = createJsonObjWithErrorMsgS3(s3FileName,"Duplicate File!");
		 	                    	array.put(jsono);
		                        } else{
		                        	
		                        	TaskDao uploadTask = new TaskDao(username, s3FileName, "upload", TaskStatus.RUNNING, false, "/upload");
		                        	uploadTask.setTaskType(TaskType.DATASET_UPLOAD.toString());
		                        	uploadTask.setTaskDescription(s3FileName);
		                        	uploadTask.setParent(true);
		                        	
		                        	try{
			                        	taskManager.registerTask(uploadTask);
			                        	S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, s3FileName));
				                        InputStream uploadedStreamS3 = object.getObjectContent();
				                        String filePath = fs.getFilePathForUploads(s3FileName, username);
				                        fs.createFile(uploadedStreamS3, filePath);
				                        message.append("Uploaded "+s3FileName+" successfully!");
				                        JSONObject jsono = createJsonObjS3(s3FileName,object.getObjectMetadata());
				                    	array.put(jsono);
				                    	//result.put("files", array);
				                    	taskManager.setTaskStatus(uploadTask, TaskStatus.SUCCESS);
		                        	}catch(Exception e){
		                        		  LOG.error("Error",e);
		                        		message.append("Could not upload "+s3FileName);
		                        		taskManager.setTaskStatus(uploadTask, TaskStatus.FAILURE);
		                        	}
		                        }
	                       		//s3 file upload end
                            }
                            result.put("files", array);
                            request.setAttribute("message",message.toString());
                        }else if("http".equalsIgnoreCase(extensionOf(fileName))){
                        	
                        	InputStream uploadedStream = item.getInputStream();
                            BufferedReader br = new BufferedReader(new InputStreamReader(uploadedStream));
                            String line = null;
                            List<String> urls = new ArrayList<String>();
                            
                            while((line=br.readLine())!=null){
                            	urls.add(line);
                            }

                            JSONArray array = new JSONArray(); 	
                            StringBuffer message = new StringBuffer();
                            
                            for(String fileLocationURL: urls){
	                            String uploadFileName = getFileName(fileLocationURL);

	                       		if(isAlreadyUploaded(uploadFileName,uploadedFiles)){
	                       			JSONObject jsono = createJsonObjWithErrorMsgS3(uploadFileName,"Duplicate File!");
		 	                    	array.put(jsono);
		                        } else{
		                        	
		                        	TaskDao uploadTask = new TaskDao(username, uploadFileName, "upload", TaskStatus.RUNNING, false, "/upload");
		                        	uploadTask.setTaskType(TaskType.DATASET_UPLOAD.toString());
		                        	uploadTask.setTaskDescription(uploadFileName);
		                        	uploadTask.setParent(true);
		                        	
		                        	try{
			                        	taskManager.registerTask(uploadTask);
			                        	URL url = new URL(fileLocationURL);
			                        	URLConnection  uConn = url.openConnection();
			                        	InputStream inputStream = uConn.getInputStream();
				                        String filePath = fs.getFilePathForUploads(uploadFileName, username);
				                        fs.createFile(inputStream, filePath);
				                        message.append("Uploaded "+uploadFileName+" successfully!");
				                        
				                        //TODO change createFile API to return number of bytes read
				                        uConn = url.openConnection();
			                        	inputStream = uConn.getInputStream();
			                        	int size = getSize(uConn, inputStream);
				                        JSONObject jsono = createJsonObjHTTP(uploadFileName,size);
				                    	array.put(jsono);
				                    	taskManager.setTaskStatus(uploadTask, TaskStatus.SUCCESS);
		                        	}catch(Exception e){
		                        		  LOG.error("Error",e);
		                        		message.append("Could not upload "+uploadFileName);
		                        		taskManager.setTaskStatus(uploadTask, TaskStatus.FAILURE);
		                        	}
		                        }
	                       		//http file upload end
                            }
                            result.put("files", array);
                            request.setAttribute("message",message.toString());
                        }else{
	                        JSONObject jsono = new JSONObject();
	                        if(isAlreadyUploaded(fileName,uploadedFiles)){
	                        	jsono = createJsonObjWithErrorMsg(item,"Duplicate File!");
	                        	JSONArray array = new JSONArray();
	 	                    	array.put(jsono);
	 	                    	result.put("files", array);
	                        } else{
	                        	
	                        	TaskDao uploadTask = new TaskDao(username, fileName, "upload", TaskStatus.RUNNING, false, "/upload");
	                        	//uploadTask.setHttpRequest(request);
	                        	uploadTask.setTaskType(TaskType.DATASET_UPLOAD.toString());
	                        	uploadTask.setTaskDescription(fileName);
	                        	uploadTask.setParent(true);
	                        	
	                        	try{
		                        	taskManager.registerTask(uploadTask);
		                        	jsono = createJsonObj(item);
			                        InputStream uploadedStream = item.getInputStream();
			                        String filePath = fs.getFilePathForUploads(fileName, username);
			                        fs.createFile(uploadedStream, filePath);
			                        request.setAttribute("message",
			                            "Upload has been done successfully!");
			                        JSONArray array = new JSONArray();
			                    	array.put(jsono);
			                    	result.put("files", array);
			                    	taskManager.setTaskStatus(uploadTask, TaskStatus.SUCCESS);
	                        	}catch(Exception e){
	                        		  LOG.error("Error",e);
	                        		taskManager.setTaskStatus(uploadTask, TaskStatus.FAILURE);
	                        	}
	                        }
                        }
                    }// end of main else 
                }
            }
            System.out.println("File sent!");
            response.setContentType("application/json");
            
           
        } catch (FileSizeLimitExceededException e) {
            request.setAttribute("message",
                    "There was an error: " + e.getMessage());
            LOG.debug("Error",e);
        }catch(Exception ex){
        	request.setAttribute("message",
                    "There was an error: " + ex.getMessage());
        	LOG.debug("Error",ex);
        }
        finally{
        	writer.write(result.toString());
            writer.close();
        }
     
    }
    
	private int getSize(URLConnection uConn, InputStream inputStream) throws IOException {
    	inputStream = uConn.getInputStream();
    	 int bytesRead,bytesWritten = 0,size=1024;
         byte[] buf = new byte[size];
         while ((bytesRead = inputStream.read(buf)) != -1) {
             bytesWritten += bytesRead;
         }
		return bytesWritten;
	}

	private String getFileName(String fileLocationURL) {
    	int lastIndexOfSlash = fileLocationURL.lastIndexOf('/');
		return fileLocationURL.substring(lastIndexOfSlash+1).trim();
	}

    private String[] getS3FileParameters(String fileName) {
    	fileName=fileName.replaceAll("https://", "");
    	fileName=fileName.replaceAll("http://", "");
    	fileName=fileName.replaceAll(".s3.amazonaws.com", "");
    	fileName=fileName.replaceAll("s3.amazonaws.com/", "");
    	return fileName.split("/");
	}
    
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException{
    	HttpSession session = request.getSession();
    	String username = (String)session.getAttribute("user");
      	PrintWriter writer = response.getWriter();
    	response.setContentType("application/json");
    	String delFileName = request.getParameter(HTTPConstants.DELETE_DATASET);
    	JSONObject responseJson = null;
    	try{
    		boolean flag = fs.deleteFile(delFileName,username);
    		if(flag){
    			responseJson = createDeleteFileJson(delFileName,flag);
    		}
    		responseJson = createDeleteFileJson(delFileName,flag);
	    	
    	}catch(Exception e){
    		request.setAttribute("message",
                    "There was an error: " + e.getMessage());
    		LOG.error("Error",e);
    	}finally{
    		writer.write(responseJson.toString());
    	}
    	
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException{
    	HttpSession session = request.getSession(false);
    	String username=(String)session.getAttribute("user");
    	JSONObject result = new JSONObject();
    	try {
    		
			JSONArray uploadedFiles = createJsonArrayForUploads(fs,username);
			result.put("files", uploadedFiles);
    		LOG.info("SERVER UP!");
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
    		LOG.error("Error",e);

		}
    	PrintWriter writer = response.getWriter();
    	response.setContentType("application/json");
    	try{
	    	
    	}catch(Exception e){
    		request.setAttribute("message",
                    "There was an error: " + e.getMessage());
    		LOG.error("Error",e);
    	}finally{
    		writer.write(result.toString());
    	}
    	
    
    }
   
    private boolean isAlreadyUploaded(String fileName,JSONArray uploadedFiles) throws JSONException{
    	for(int i=0;i<uploadedFiles.length();i++){
    		JSONObject obj = uploadedFiles.getJSONObject(i);
    		if(fileName.equalsIgnoreCase(obj.get("name").toString()))
    			return true;	
    	}
    	return false;
    	
    }
    
    
    private JSONObject createJsonObj(FileItem item) throws JSONException{
    	JSONObject jsono = new JSONObject();
    	jsono.put("name", item.getName());
        jsono.put("size", item.getSize());
        jsono.put("type", item.getContentType());
        jsono.put("url", Utils.SERVER_URL+"FileUpload?getfile=" + item.getName());
        jsono.put("thumbnailUrl", "js/jquery-upload/img/document_thumbnail.PNG");
        jsono.put("deleteUrl", Utils.SERVER_URL+"FileUpload?"+HTTPConstants.DELETE_DATASET+"=" + item.getName());
        jsono.put("deleteType", "DELETE");
        return jsono;
    }
    private JSONObject createJsonObjS3(String item, ObjectMetadata objectMetadata) throws JSONException{
    	JSONObject jsono = new JSONObject();
    	jsono.put("name", item);
    	jsono.put("size", objectMetadata.getContentLength());
        jsono.put("type", objectMetadata.getContentType());
        jsono.put("url", Utils.SERVER_URL+"FileUpload?getfile=" + item);
        jsono.put("thumbnailUrl", "js/jquery-upload/img/document_thumbnail.PNG");
        jsono.put("deleteUrl", Utils.SERVER_URL+"FileUpload?"+HTTPConstants.DELETE_DATASET+"=" + item);
        jsono.put("deleteType", "DELETE");
        return jsono;
    }
    
    private JSONObject createJsonObjHTTP(String item, int size) throws JSONException {
    	JSONObject jsono = new JSONObject();
    	jsono.put("name", item);
    	jsono.put("size", size);
        jsono.put("url", Utils.SERVER_URL+"FileUpload?getfile=" + item);
        jsono.put("thumbnailUrl", "js/jquery-upload/img/document_thumbnail.PNG");
        jsono.put("deleteUrl", Utils.SERVER_URL+"FileUpload?"+HTTPConstants.DELETE_DATASET+"=" + item);
        jsono.put("deleteType", "DELETE");
        return jsono;
	}
    private JSONObject createJsonObjWithErrorMsg(FileItem item,String message) throws JSONException{
    	JSONObject jsono = new JSONObject();
    	jsono.put("error", message);
    	jsono.put("name", item.getName());
        jsono.put("size", item.getSize());
        jsono.put("type", item.getContentType());
        jsono.put("url", "");
        jsono.put("thumbnailUrl", "");
        jsono.put("deleteUrl", "");
        jsono.put("deleteType", "DELETE");
        return jsono;
    }
    private JSONObject createJsonObjWithErrorMsgS3(String item,String message) throws JSONException{
    	JSONObject jsono = new JSONObject();
    	jsono.put("error", message);
    	jsono.put("name", item);
        jsono.put("url", "");
        jsono.put("thumbnailUrl", "");
        jsono.put("deleteUrl", "");
        jsono.put("deleteType", "DELETE");
        return jsono;
    }
    private JSONObject createDeleteFileJson(String fileName,boolean flag) throws JSONException{
    	JSONObject result = new JSONObject();
    	JSONArray array = new JSONArray();
    	JSONObject jsono = new JSONObject();
    	if(flag)
    		jsono.put(fileName, true);
    	else
    		jsono.put(fileName, false);
    	array.put(jsono);
    	result.put("files", array);
    	return result;
    }
    
        
   
    
    
    @SuppressWarnings("unchecked")
	private JSONArray createJsonArrayForUploads(IFileSystem fs,String username) throws JSONException, FileNotFoundException, IOException{
		 
    	JSONArray filesJSONArray = new JSONArray();
    	try{ 
    	List<LocatedFileStatus> dir = (List<LocatedFileStatus>) fs.getUploadedTrainingDatasets(username);
    	dir.addAll((List<LocatedFileStatus>)fs.getUploadedTestDatasets(username));
		
		JSONObject jsono;
		
		for(FileStatus file:dir){
			if(file.isFile()){
				jsono = new JSONObject();
				jsono.put("name", file.getPath().getName());
		        jsono.put("size", file.getLen());
		        jsono.put("type", "file");
		        jsono.put("url", Utils.SERVER_URL+"FileUpload?getfile=" + file.getPath().getName());
		        jsono.put("thumbnailUrl", "js/jquery-upload/img/document_thumbnail.PNG");
		        jsono.put("deleteUrl", Utils.SERVER_URL+"FileUpload?"+HTTPConstants.DELETE_DATASET+"=" + file.getPath().getName());
		        jsono.put("deleteType", "DELETE");
		        filesJSONArray.put(jsono);
			}
		}
		
		}catch(Exception e){
			LOG.debug("Error",e);
		}finally{
			
		}
    	return filesJSONArray;
	 }
    
    private String extensionOf(String fileName){
		int lastIndexOfPeriod = fileName.lastIndexOf('.');
		return fileName.substring(lastIndexOfPeriod+1);
	}
}
