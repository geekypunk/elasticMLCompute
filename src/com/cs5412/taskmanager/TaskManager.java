package com.cs5412.taskmanager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.cs5412.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * <p><b>This class implements the ITaskManager. Uses CouchBase for storing Task Objects.</b></p>
 * @author kt466
 *
 */
public class TaskManager implements ITaskManager{
	static final Logger LOG = LoggerFactory.getLogger(TaskManager.class);
	private CouchbaseClient couchbaseClient;
	private Gson gson;
	private String hostVersion = null;
	public TaskManager(CouchbaseClient client){
		hostVersion = null;
		gson = null;
		try{
			couchbaseClient = client;
			final GsonBuilder gsonBuilder = new GsonBuilder();
		    gsonBuilder.registerTypeAdapter(TaskDao.class, new TaskDaoAdaptor());
		    gsonBuilder.setPrettyPrinting();
		    gson = gsonBuilder.create();
		    hostVersion = (Integer) couchbaseClient.get(Utils.getIP()) + "";
		}catch(Exception e){
			LOG.error("Error: ", e);
		}
	}
	
	@Override
	public void registerTask(TaskDao task) throws InterruptedException, ExecutionException {
		task.setHostVersion(hostVersion);
		couchbaseClient.set(task.getTaskId(), gson.toJson(task)).get();
		Type type = new TypeToken<ArrayList<String>>(){}.getType();
		ArrayList<String> taskIds = gson.fromJson((String) couchbaseClient.get("AllUserTaskIds"), type);
		taskIds.add(task.getTaskId());
		couchbaseClient.set("AllUserTaskIds", gson.toJson(taskIds)).get();
	}
	
	@Override
	public void setTaskStatus(TaskDao task,TaskStatus status) throws InterruptedException, ExecutionException {
		task.setHostVersion(hostVersion);
		Type type = new TypeToken<TaskDao>(){}.getType();
		TaskDao _task = gson.fromJson((String) couchbaseClient.get(task.getTaskId()), type);
		_task.setStatus(status);
		couchbaseClient.set(task.getTaskId(), gson.toJson(_task)).get();
	}
	
	@Override
	public TaskDao getTaskById(String id) {
		Type type = new TypeToken<TaskDao>(){}.getType();
		TaskDao task = gson.fromJson((String) couchbaseClient.get(id), type);
		return task;
	}
	
	@Override
	public List<TaskDao> getAllTasksForUser(String username){
		List<TaskDao> returnList = new ArrayList<TaskDao>();
		Type type = new TypeToken<ArrayList<String>>(){}.getType();
		ArrayList<String> taskIds = gson.fromJson((String) couchbaseClient.get("AllUserTaskIds"), type);
		for(String td : taskIds){
			TaskDao task = getTaskById(td);
			if(task.getUserId().equals(username)) returnList.add(task);
		}
		return returnList;
	}
	
	@Override
	public void markAsSeen(String taskId) throws InterruptedException, ExecutionException {
		Type type = new TypeToken<TaskDao>(){}.getType();
		TaskDao task = gson.fromJson((String) couchbaseClient.get(taskId), type);
		task.setSeen(true);
		couchbaseClient.set(taskId, gson.toJson(task)).get();
	}
	
	@Override
	public List<TaskDao>  getFinishedAndUnseenByUserId(String username) {
		List<TaskDao> unfinished = new ArrayList<TaskDao>();
		List<TaskDao> allUserTasks = getAllTasksForUser(username);
		for(TaskDao task : allUserTasks){
			if(task.getStatus() == TaskStatus.SUCCESS && !task.isSeen()){
				unfinished.add(task);
			}
		}
		return unfinished;	
	}
	
	@Override
	public void markAllAsSeen(String username) throws InterruptedException, ExecutionException {
		List<TaskDao> allUserTasks = getAllTasksForUser(username);
		for(TaskDao task : allUserTasks){
			markAsSeen(task.getTaskId());
		}
	}
	
	@Override
	public void removeParentDependency(String taskId, String username)throws Exception{
		List<TaskDao> allUserTasks = getAllTasksForUser(username);
		for(TaskDao task : allUserTasks){
			if(task.getParentTaskId().contains(taskId)){
				task.getParentTaskId().remove(taskId);
				couchbaseClient.set(task.getTaskId(), gson.toJson(task));
			}
		}
	}
	
	@Override
	public void removeTaskById(String taskId)throws InterruptedException, ExecutionException {
		Type type = new TypeToken<ArrayList<String>>(){}.getType();
		ArrayList<String> taskIds = gson.fromJson((String) couchbaseClient.get("AllUserTaskIds"), type);
		taskIds.remove(taskId);
		couchbaseClient.set("AllUserTaskIds", gson.toJson(taskIds)).get();
		couchbaseClient.delete(taskId);
	}
}
