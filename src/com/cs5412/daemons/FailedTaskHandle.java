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
		    Type type = new TypeToken<HashMap<String,String>>(){}.getType();
		    String allUserTasks =  (String) couchbaseClient.get("AllUserTasks");
		    HashMap<String,String> tasks = gson.fromJson(allUserTasks, type);
		    TaskDao repairTask = null;
		    String taskId = null,status = null;
		    long taskCASValue = 0;
		    CASValue<Object> taskCASObj = null;
		    String taskCASJson = null;
		    if(tasks != null){
			    for(Entry<String, String> ent : tasks.entrySet()){
			    	taskId = ent.getKey();
			    	status = ent.getValue();
			    	if(status == TaskStatus.FAILURE.name()){
			    		taskCASObj = couchbaseClient.gets(taskId); 
			    		taskCASValue = taskCASObj.getCas();
			    		taskCASJson = (String)taskCASObj.getValue();
			    		repairTask = gson.fromJson(taskCASJson,TaskDao.class);
			    		repairTask.setHostAddress("dummy");
			    		//Check if someone already changes this task's status
				    	if(couchbaseClient.gets(taskId).getCas() ==taskCASValue){
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
					        break;
				    	}else{
				    		//Someone already picked up this failed task..move on
				    		continue;
				    	} 
				    	
			    		
			    	}
			    }
		    }
		    if(taskId == null){
		    	
		      	LOG.debug("No failed tasks");
		    }
	    }catch(Exception e){
	    	LOG.debug("Error",e.getCause());
	    }
	}
}
