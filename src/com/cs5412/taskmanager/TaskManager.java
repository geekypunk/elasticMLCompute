package com.cs5412.taskmanager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class TaskManager implements ITaskManager{

	private CouchbaseClient couchbaseClient;
	final private Gson gson;
	public TaskManager(CouchbaseClient client){
		couchbaseClient = client;
		final GsonBuilder gsonBuilder = new GsonBuilder();
	    gsonBuilder.registerTypeAdapter(TaskDao.class, new TaskDaoAdaptor());
	    gsonBuilder.setPrettyPrinting();
	    gson = gsonBuilder.create();
	
	}

	@Override
	public void registerTask(TaskDao task) throws InterruptedException, ExecutionException {
		String username = task.getUserId();
		HashMap<String,TaskDao> tasks = getUserTasksMap(username);
		tasks.put(task.getTaskId(), task);
		couchbaseClient.set(username+"Tasks", gson.toJson(tasks)).get();
		
	}

	@Override
	public TaskDao getTaskById(int id,String username) {
		HashMap<String,TaskDao> tasks = getUserTasksMap(username);
		return tasks.get(id);
		
	}

	@Override
	public List<TaskDao> getAllTasksForUser(String username){
		
		return new ArrayList<TaskDao>(getUserTasksMap(username).values());
	}
	@Override
	public void setTaskStatus(TaskDao task,TaskStatus status) throws InterruptedException, ExecutionException {
		
		HashMap<String,TaskDao> tasks = getUserTasksMap(task.getUserId());
		TaskDao _task = tasks.get(task.getTaskId());
		_task.setStatus(status);
		couchbaseClient.set(_task.getUserId()+"Tasks", gson.toJson(tasks)).get();
		
	}

	
	
	@Override
	public void markAsSeen(String taskId) {
		TaskDao task = (TaskDao) couchbaseClient.get(taskId);
		task.setSeen(true);
	}

	@Override
	public List<TaskDao>  getFinishedAndUnseenByUserId(String username) {
		HashMap<String,TaskDao> tasks = getUserTasksMap(username);
		List<TaskDao> unfinished = new ArrayList<TaskDao>();
		TaskDao task;
		for (Entry<String, TaskDao> entry : tasks.entrySet()) {
			task = entry.getValue();
			if(task.getStatus() == TaskStatus.SUCCESS && !task.isSeen()){
				unfinished.add(task);
			}
		}
		return unfinished;
		
	}

	private HashMap<String,TaskDao> getUserTasksMap(String username){
		
		Type type = new TypeToken<HashMap<String,TaskDao>>(){}.getType();
		HashMap<String,TaskDao> tasks = new HashMap<String, TaskDao>();
		tasks = gson.fromJson((String) couchbaseClient.get(username+"Tasks"), type);
		return tasks;
	}

	@Override
	public void markAllAsSeen(String username) throws InterruptedException, ExecutionException {
		HashMap<String,TaskDao> tasks = getUserTasksMap(username);
		for (Entry<String, TaskDao> entry : tasks.entrySet()) {
			entry.getValue().setSeen(true);
		}
		couchbaseClient.set(username+"Tasks", gson.toJson(tasks)).get();
	}
}
