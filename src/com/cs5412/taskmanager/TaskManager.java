package com.cs5412.taskmanager;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import com.couchbase.client.CouchbaseClient;
import com.cs5412.dataobjects.TaskDao;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TaskManager implements ITaskManager{

	private CouchbaseClient couchbaseClient;
	private Gson gson;
	public TaskManager(CouchbaseClient client){
		couchbaseClient = client;
		gson = new Gson();
		
	}

	@Override
	public void registerTask(TaskDao task) throws InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		String username = task.getUserId();
		Map<Integer,TaskDao> tasks = getUserTasksMap(username);
		tasks.put(task.getTaskId(), task);
		couchbaseClient.set(username+"Tasks", gson.toJson(tasks)).get();
		
	}

	@Override
	public void getTaskById(int id) {
		
	}

	@Override
	public void setTaskStatus(TaskDao task,TaskStatus status) throws InterruptedException, ExecutionException {
		
		Map<Integer,TaskDao> tasks = getUserTasksMap(task.getUserId());
		TaskDao _task = tasks.get(task.getTaskId());
		_task.setStatus(status);
		couchbaseClient.set(_task.getUserId()+"Tasks", gson.toJson(tasks)).get();
		
		
	}

	
	
	@Override
	public void markAsSeen(String taskId) {
		// TODO Auto-generated method stub
		TaskDao task = (TaskDao) couchbaseClient.get(taskId);
		task.setSeen(true);
	}

	@Override
	public List<TaskDao>  getFinishedAndUnseenByUserId(String username) {
		// TODO Auto-generated method stub
		Map<Integer,TaskDao> tasks = getUserTasksMap(username);
		List<TaskDao> unfinished = Lists.newArrayList();
		TaskDao task;
		for (Entry<Integer, TaskDao> entry : tasks.entrySet()) {
			task = entry.getValue();
			if(task.getStatus() == TaskStatus.SUCCESS && !task.isSeen()){
				unfinished.add(task);
			}
		}
		return unfinished;
		
	}

	private Map<Integer,TaskDao> getUserTasksMap(String username){
		
		Type type = new TypeToken<Map<Integer,TaskDao>>(){}.getType();
		Map<Integer,TaskDao> tasks = Maps.newHashMap();
		tasks = gson.fromJson((String) couchbaseClient.get(username+"Tasks"), type);
		return tasks;
	}

	@Override
	public void markAllAsSeen(String username) throws InterruptedException, ExecutionException {
		Map<Integer,TaskDao> tasks = getUserTasksMap(username);
		for (Entry<Integer, TaskDao> entry : tasks.entrySet()) {
			entry.getValue().setSeen(true);
		}
		couchbaseClient.set(username+"Tasks", gson.toJson(tasks)).get();
	}
}
