package com.cs5412.testing;

import com.couchbase.client.CouchbaseClient;
import com.cs5412.taskmanager.TaskDao;
import com.cs5412.taskmanager.TaskDaoAdaptor;
import com.cs5412.taskmanager.TaskStatus;
import com.cs5412.webservices.fileupload.FileUploadServlet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaemonTestClass {
	public static void main(String[] args)throws Exception{
		final GsonBuilder gsonBuilder = new GsonBuilder();
	    gsonBuilder.registerTypeAdapter(TaskDao.class, new TaskDaoAdaptor());
	    gsonBuilder.setPrettyPrinting();
	    Gson gson = gsonBuilder.create();
	    String loadBalancerAddress = "http://localhost:8080";
	    Logger LOG = LoggerFactory.getLogger(FileUploadServlet.class);
	    
		List<URI> hosts = Arrays.asList(
			      new URI("http://192.168.56.101:8091/pools")
			    );
	    CouchbaseClient couchbaseClient = new CouchbaseClient(
	    		hosts, "default", "");
	    Type type = new TypeToken<HashMap<Integer,TaskDao>>(){}.getType();
	    HashMap<Integer,TaskDao> tasks = gson.fromJson((String) couchbaseClient.get("AllUser"+"Tasks"), type);
	    TaskDao repairTask = null;
	    if(tasks != null){
		    for(Entry<Integer, TaskDao> ent : tasks.entrySet()){
		    	TaskDao td = ent.getValue();
		    	if(td.getStatus() == TaskStatus.FAILURE && td.getParentTaskId().size() == 0){
		    		repairTask = td;
		    		break;
		    	}
		    }
	    }
	    if(repairTask != null){
	    	String taskUrl = loadBalancerAddress  + repairTask.getWsURL();
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
	    }
	}
}
