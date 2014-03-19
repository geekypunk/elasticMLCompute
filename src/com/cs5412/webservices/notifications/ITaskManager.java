package com.cs5412.webservices.notifications;

import java.util.List;

import com.cs5412.dataobjects.TaskDao;

public interface ITaskManager {
	public void addTask(TaskDao task);
	public void getTaskById(int id);
	public void markAsFinished();
	public List<TaskDao> getFinishedAndUnseenByUserId(String username);
	public void maskAsSeen(String taskId);

}
