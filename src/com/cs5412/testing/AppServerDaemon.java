package com.cs5412.testing;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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

public class AppServerDaemon {
	public static void main(String[] args)throws Exception{
		final GsonBuilder gsonBuilder = new GsonBuilder();
	    gsonBuilder.registerTypeAdapter(TaskDao.class, new TaskDaoAdaptor());
	    gsonBuilder.setPrettyPrinting();
	    Gson gson = gsonBuilder.create();
	    Logger LOG = LoggerFactory.getLogger(FileUploadServlet.class);
	    
		List<URI> hosts = Arrays.asList(
			      new URI("http://192.168.56.101:8091/pools")
			    );
	    CouchbaseClient couchbaseClient = new CouchbaseClient(
	    		hosts, "default", "");
	    TaskManager taskManager = new TaskManager(couchbaseClient);
	    Type type = new TypeToken<HashMap<Integer,TaskDao>>(){}.getType();
	    HashMap<Integer,TaskDao> tasks = gson.fromJson((String) couchbaseClient.get("AllUser"+"Tasks"), type);
	    if(tasks != null){
	    	for(Entry<Integer, TaskDao> ent : tasks.entrySet()){
	    		TaskDao td = ent.getValue();
	    		if(td.getStatus() != TaskStatus.SUCCESS && td.getStatus() != TaskStatus.PARENT){
	    			String hostAddr = td.getHostAddress();
	    		//	System.out.println(hostAddr);
	    			String taskUrl = "http://" + hostAddr + ":8080/elasticMLCompute/ui/notifications/poller";
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
	    		}
	    	}
	    	System.out.println("Finished polling all the servers");
	    }
	}
}
