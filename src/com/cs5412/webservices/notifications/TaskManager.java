package com.cs5412.webservices.notifications;

import java.util.List;

import com.couchbase.client.CouchbaseClient;
import com.cs5412.dataobjects.TaskDao;
import com.google.gson.Gson;

public class TaskManager implements ITaskManager{

	private CouchbaseClient couchbaseClient;
	private Gson gson;
	public TaskManager(CouchbaseClient client){
		couchbaseClient = client;
		gson = new Gson();
		
	}

	@Override
	public void addTask(TaskDao task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getTaskById(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void markAsFinished() {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void maskAsSeen(String taskId) {
		// TODO Auto-generated method stub
		TaskDao task = (TaskDao) couchbaseClient.get(taskId);
		task.setSeen(true);
	}

	@Override
	public List<TaskDao>  getFinishedAndUnseenByUserId(String username) {
		// TODO Auto-generated method stub
		return null;
		
	}

}
