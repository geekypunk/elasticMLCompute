package com.cs5412.taskmanager;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ITaskManager {
	public void registerTask(TaskDao task) throws InterruptedException, ExecutionException;
	public TaskDao getTaskById(String id);
	public void setTaskStatus(TaskDao task,TaskStatus status) throws InterruptedException, ExecutionException;
	public List<TaskDao> getFinishedAndUnseenByUserId(String username);
	public void markAsSeen(String taskId) throws InterruptedException, ExecutionException;
	public void markAllAsSeen(String username) throws InterruptedException, ExecutionException;
	public void removeParentDependency(String taskId, String username)throws Exception;
	List<TaskDao> getAllTasksForUser(String username);

}
