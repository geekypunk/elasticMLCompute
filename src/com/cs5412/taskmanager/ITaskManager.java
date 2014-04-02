package com.cs5412.taskmanager;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ITaskManager {
	public void registerTask(TaskDao task) throws InterruptedException, ExecutionException;
	public TaskDao getTaskById(String id,String username);
	public void setTaskStatus(TaskDao task,TaskStatus status) throws InterruptedException, ExecutionException;
	public List<TaskDao> getFinishedAndUnseenByUserId(String username);
	public void markAsSeen(String taskId);
	public void markAllAsSeen(String username) throws InterruptedException, ExecutionException;
	List<TaskDao> getAllTasksForUser(String username);

}
