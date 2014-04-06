package com.cs5412.daemons;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;
import java.util.Map.Entry;

import javax.servlet.ServletContext;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.cs5412.taskmanager.TaskDao;
import com.cs5412.taskmanager.TaskDaoAdaptor;
import com.cs5412.taskmanager.TaskManager;
import com.cs5412.taskmanager.TaskStatus;
import com.cs5412.webservices.fileupload.FileUploadServlet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class FailedServerHandle extends TimerTask{
	private CouchbaseClient couchbaseClient;
	private Gson gson;
	TaskManager taskManager;
	private PropertiesConfiguration config;
	private int debug;
	private static final Logger LOG = LoggerFactory.getLogger(FileUploadServlet.class);
	public FailedServerHandle(ServletContext ctx){
		this.couchbaseClient = (CouchbaseClient)ctx.getAttribute("couchbaseClient");
		this.config = (PropertiesConfiguration)ctx.getAttribute("config");
		debug = this.config.getInt("DEBUG");
		final GsonBuilder gsonBuilder = new GsonBuilder();
	    gsonBuilder.registerTypeAdapter(TaskDao.class, new TaskDaoAdaptor());
	    gsonBuilder.setPrettyPrinting();
	    gson = gsonBuilder.create();
	    taskManager = new TaskManager(couchbaseClient);
	}
	@Override
	public void run() {
		System.out.println("Handle of the failed servers");
		
		final GsonBuilder gsonBuilder = new GsonBuilder();
	    gsonBuilder.registerTypeAdapter(TaskDao.class, new TaskDaoAdaptor());
	    gsonBuilder.setPrettyPrinting();
	    gson = gsonBuilder.create();
	   
	    try{
			Type type = new TypeToken<ArrayList<String>>(){}.getType();
		    String allUserTasks =  (String) couchbaseClient.get("AllUserTaskIds");
		    ArrayList<String> tasks = gson.fromJson(allUserTasks, type);
		    if(tasks != null){
		    	for(String taskId : tasks){
		    		TaskDao td = taskManager.getTaskById(taskId);
		    		if(td.getStatus() != TaskStatus.SUCCESS && td.getStatus() != TaskStatus.FAILURE && !td.isParent()){
		    			String hostAddr = td.getHostAddress();
		    			String taskUrl;
		    			if(debug == 1)
		    				taskUrl= "http://" + hostAddr + ":8080/elasticMLCompute/ui/notifications/poller";
		    			else
		    				taskUrl= "http://" + hostAddr + ":8080/ui/notifications/poller";
		    			URL url = new URL(taskUrl);
		    			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    	        conn.setReadTimeout(1000000);
		    	        conn.setConnectTimeout(1000000);
		    	        conn.setRequestMethod("GET");
		    	        conn.setUseCaches(false);
		    	        conn.setDoInput(true);
		    	        conn.setDoOutput(true);
		    	        try{
		    	        	conn.connect();
		    	        }catch(Exception e){
		    	        	try{
		    	        		conn.connect();
		    	        	}catch(Exception e1){
		    	        		
		    	        		System.out.println("Server: " + hostAddr + " down");
		    	        		taskManager.setTaskStatus(td, TaskStatus.FAILURE);
		    	        	}
		    	        }
		    		}/*else if(td.getStatus() == TaskStatus.SUCCESS){
		    			taskManager.removeTaskById(td.getTaskId());
		    		}*/
		    	}
		    	LOG.debug("Finished polling all the servers");
		    }
	    }catch(Exception e){
	    	LOG.debug("Error",e);
	    }
	}
}
