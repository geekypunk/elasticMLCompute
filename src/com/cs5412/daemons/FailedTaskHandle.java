package com.cs5412.daemons;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TimerTask;

import javax.servlet.ServletContext;

import net.spy.memcached.CASValue;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.cs5412.taskmanager.TaskDao;
import com.cs5412.taskmanager.TaskDaoAdaptor;
import com.cs5412.taskmanager.TaskManager;
import com.cs5412.taskmanager.TaskStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class FailedTaskHandle extends TimerTask{
	private String loadBalancerAddress;
	private CouchbaseClient couchbaseClient;
	private Gson gson;
	TaskManager taskManager;
	private PropertiesConfiguration config;
	Logger LOG = LoggerFactory.getLogger(FailedTaskHandle.class);
	public FailedTaskHandle(ServletContext ctx){
		this.couchbaseClient = (CouchbaseClient)ctx.getAttribute("couchbaseClient");
		this.config = (PropertiesConfiguration)ctx.getAttribute("config");
		final GsonBuilder gsonBuilder = new GsonBuilder();
	    gsonBuilder.registerTypeAdapter(TaskDao.class, new TaskDaoAdaptor());
	    gsonBuilder.setPrettyPrinting();
	    gson = gsonBuilder.create();
	    taskManager = new TaskManager(couchbaseClient);
	    loadBalancerAddress = config.getString("LOAD_BALANCER_URI");
	}
	@Override
	public void run() {
		System.out.println("Handle of the failed tasks");
		
		final GsonBuilder gsonBuilder = new GsonBuilder();
	    gsonBuilder.registerTypeAdapter(TaskDao.class, new TaskDaoAdaptor());
	    gsonBuilder.setPrettyPrinting();
	    gson = gsonBuilder.create();
	    
	    try{
		    Type type = new TypeToken<HashMap<String,TaskDao>>(){}.getType();
		    //CASValue<Object> obj =  couchbaseClient.getAndLock("AllUser"+"Tasks", 30);
		    //long casValue = obj.getCas();
		    String obj =  (String) couchbaseClient.get("AllUser"+"Tasks");
		    HashMap<String,TaskDao> tasks = gson.fromJson(obj, type);
		    TaskDao repairTask = null;
		    if(tasks != null){
			    for(Entry<String, TaskDao> ent : tasks.entrySet()){
			    	TaskDao td = ent.getValue();
			    	if(td.getStatus() == TaskStatus.FAILURE && td.getParentTaskId().size() == 0){
			    		repairTask = td;
			    		break;
			    	}
			    }
		    }
		    if(repairTask != null){
		    	repairTask.setHostAddress("dummy");
		    	taskManager.setTaskStatus(repairTask, TaskStatus.INITIALIZED);
		       	String taskUrl = loadBalancerAddress + repairTask.getWsURL();
		       	LOG.debug(taskUrl);
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
		        LOG.debug(conn.getResponseMessage());
		    }else{
		    	LOG.debug("No failed tasks");
		    }
	    }catch(Exception e){
	    	LOG.debug("Error",e.getCause());
	    }
	}
}
