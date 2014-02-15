package com.cs5412.webservices.fileupload;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cs5412.filesystem.impl.FileSystemImpl;
import com.cs5412.utils.HTTPConstants;
import com.cs5412.utils.ServerConstants;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Java servlet that handles file upload from client.
 * 
 * @author kt466
 */
@WebServlet("/FileUpload")
public class FileUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static final Logger LOG = LoggerFactory.getLogger(FileUploadServlet.class);
     
    // upload settings
  
    
    //In-memory object-Maintains list of files on server
    JSONObject filesJSONobj = new JSONObject();
    JSONArray filesJSONArray = new JSONArray();
    FileSystemImpl fs = new FileSystemImpl();
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
    public void init(ServletConfig config)
            throws ServletException{
    	try {
    		
			filesJSONArray = createJsonArrayForUploads();
    		filesJSONobj.put("files", filesJSONArray);
    		LOG.info("SERVER UP!");
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
    		LOG.error("Error",e);

		}
    	
    }
    /**
     * Upon receiving file upload submission, parses the request to read
     * upload data and saves the file on disk.
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
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
        
        
        // configures upload settings
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // sets memory threshold - beyond which files are stored in disk, controls in-memory storage 
        factory.setSizeThreshold(ServerConstants.MEMORY_THRESHOLD);
        // sets temporary location to store files
        factory.setRepository(new File(ServerConstants.TMP_DIRECTORY));
 
        ServletFileUpload upload = new ServletFileUpload(factory);
         
        // sets maximum size of upload file
        upload.setFileSizeMax(ServerConstants.MAX_FILE_SIZE);
         
        // sets maximum size of request (include file + form data)
        upload.setSizeMax(ServerConstants.MAX_REQUEST_SIZE);
        upload.setProgressListener(progressListener);
 
        // constructs the directory path to store upload file
        // this path is relative to application's directory
        String uploadPath = ServerConstants.UPLOAD_DIRECTORY;
         
        // creates the directory if it does not exist
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }
        JSONObject result = new JSONObject();
        try {
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
                        JSONObject jsono = new JSONObject();
                        if(isAlreadyUploaded(fileName)){
                        	jsono = createJsonObjWithErrorMsg(item,"Duplicate File!");
                        	JSONArray array = new JSONArray();
 	                    	array.put(jsono);
 	                    	result.put("files", array);
                        } else{
	                        String filePath = uploadPath + File.separator + fileName;
	                        jsono = createJsonObj(item);
	                        InputStream uploadedStream = item.getInputStream();
	                        fs.createFile(uploadedStream, filePath);
	                        request.setAttribute("message",
	                            "Upload has been done successfully!");
	                        JSONArray array = new JSONArray();
	                    	array.put(jsono);
	                    	result.put("files", array);
	                        filesJSONArray.put(jsono);
                        }
                    }
                }
            }
            System.out.println("File sent!");
            response.setContentType("application/json");
            
           
        } catch (Exception ex) {
            request.setAttribute("message",
                    "There was an error: " + ex.getMessage());
            LOG.error("Error",ex);
        }
        finally{
        	writer.write(result.toString());
            writer.close();
        }
     
    }
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException{
    	PrintWriter writer = response.getWriter();
    	response.setContentType("application/json");
    	String delFileName = request.getParameter(HTTPConstants.DELETE_DATASET);
    	JSONObject responseJson = null;
    	try{
    		boolean flag = fs.deleteFile(delFileName);
    		if(flag){
    			removeFromInMemoryJson(delFileName);
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
    	
    	PrintWriter writer = response.getWriter();
    	response.setContentType("application/json");
    	try{
	    	
    	}catch(Exception e){
    		request.setAttribute("message",
                    "There was an error: " + e.getMessage());
    		LOG.error("Error",e);
    	}finally{
    		writer.write(filesJSONobj.toString());
    	}
    	
    
    }
   
    private boolean isAlreadyUploaded(String fileName) throws JSONException{
    	for(int i=0;i<filesJSONArray.length();i++){
    		JSONObject obj = filesJSONArray.getJSONObject(i);
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
        jsono.put("url", ServerConstants.SERVER_URL+"FileUpload?getfile=" + item.getName());
        jsono.put("thumbnailUrl", "http://localhost/uifork/js/jquery-upload/img/document_thumbnail.PNG");
        jsono.put("deleteUrl", ServerConstants.SERVER_URL+"FileUpload?"+HTTPConstants.DELETE_DATASET+"=" + item.getName());
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
    
        
   
    private void removeFromInMemoryJson(String fileName) throws JSONException{
    	
    	for(int i=0;i<filesJSONArray.length();i++){
    		String item = filesJSONArray.getJSONObject(i).getString("name"); 
    		if(item.equalsIgnoreCase(fileName))
    			filesJSONArray.remove(i);
    	}
    }
    
    private JSONArray createJsonArrayForUploads() throws JSONException{
		 
		 Collection<File> dir = fs.getUploadedDatasets();
		 JSONArray filesJSONArray = new JSONArray();
		 JSONObject jsono;
		 for(File file:dir){
			 jsono = new JSONObject();
			 jsono.put("name", file.getName());
	         jsono.put("size", file.length());
	         jsono.put("type", "file");
	         jsono.put("url", ServerConstants.SERVER_URL+"FileUpload?getfile=" + file.getName());
	         jsono.put("thumbnailUrl", "http://localhost/uifork/js/jquery-upload/img/document_thumbnail.PNG");
	         jsono.put("deleteUrl", ServerConstants.SERVER_URL+"FileUpload?"+HTTPConstants.DELETE_DATASET+"=" + file.getName());
	         jsono.put("deleteType", "DELETE");
	         filesJSONArray.put(jsono);
		 }
		 return filesJSONArray;
	 }
}