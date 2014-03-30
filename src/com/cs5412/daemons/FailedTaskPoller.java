package com.cs5412.daemons;

import java.util.TimerTask;

import javax.servlet.ServletContext;

import com.couchbase.client.CouchbaseClient;
import com.cs5412.taskmanager.TaskDao;
import com.cs5412.taskmanager.TaskDaoAdaptor;
import com.cs5412.taskmanager.TaskManager;
import com.cs5412.taskmanager.TaskStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FailedTaskPoller extends TimerTask{

	private CouchbaseClient couchbaseClient;
	private Gson gson;
	TaskManager taskManager;
	public FailedTaskPoller(ServletContext ctx){
		this.couchbaseClient = (CouchbaseClient)ctx.getAttribute("couchbaseClient");
		final GsonBuilder gsonBuilder = new GsonBuilder();
	    gsonBuilder.registerTypeAdapter(TaskDao.class, new TaskDaoAdaptor());
	    gsonBuilder.setPrettyPrinting();
	    gson = gsonBuilder.create();
	    taskManager = new TaskManager(couchbaseClient);
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{
			TaskDao task = getArbitraryFailedTask();
			taskManager.setTaskStatus(task, TaskStatus.RUNNING);
			taskManager.registerTask(task);
			//Create an async http request for this task
			//have the onResponse callback to set the task status to success
		}catch(Exception e){
			
			e.printStackTrace();
		}
		
	}
	
	private TaskDao getArbitraryFailedTask(){
		
		//Get the list of all users
		
			//Get the tasklist of this user
				//GetWithLock the first failed task..update its status
				//If this is a sub-task and the only failed task, update master task after successful completion
		
		//With lock timeout of 5 seconds
		couchbaseClient.getAndLock("",5);
		return null;
		
		
	}
	

}
