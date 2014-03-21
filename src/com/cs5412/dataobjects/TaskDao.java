package com.cs5412.dataobjects;

import java.util.List;

import com.cs5412.taskmanager.TaskStatus;
import com.google.common.collect.Lists;

public class TaskDao {
	private String userId;
	private String taskName;
	private String taskDescription;
	private String reportUrl;
	private TaskStatus status;
	private boolean isSeen;
	private int taskId;
	List<TaskDao> subTasks = Lists.newArrayList();
	
	public TaskDao(String userId,String taskName,String reportUrl,TaskStatus status,boolean isSeen){
		
		this.userId = userId;
		this.taskName = taskName;
		this.reportUrl = reportUrl;
		this.status = status;
		this.isSeen = isSeen;
		this.taskId = (this.userId+this.taskName).hashCode();
	
	}
	
	public int getTaskId(){
		return taskId;
	}
	
	/**
	 * @return the usedId
	 */
	public String getUserId() {
		return userId;
	}
	/**
	 * @param usedId the usedId to set
	 */
	public void setUsedId(String usedId) {
		this.userId = usedId;
	}
	/**
	 * @return the taskName
	 */
	public String getTaskName() {
		return taskName;
	}
	/**
	 * @param taskName the taskName to set
	 */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	/**
	 * @return the reportUrl
	 */
	public String getReportUrl() {
		return reportUrl;
	}
	/**
	 * @param reportUrl the reportUrl to set
	 */
	public void setReportUrl(String reportUrl) {
		this.reportUrl = reportUrl;
	}
	
	/**
	 * Return task status. If sub-tasks are present, check all their status too.
	 * @return the status.
	 */
	public TaskStatus getStatus() {
		if(this.subTasks.size() ==0)
			return status;
		else{
			for(TaskDao task: this.subTasks){
				if(task.status == TaskStatus.FAILURE)
					return TaskStatus.FAILURE;
			}
			return TaskStatus.SUCCESS;
		}
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(TaskStatus status) {
		this.status = status;
	}
	
	public void addSubTask(TaskDao task){
		
		this.subTasks.add(task);
		
	}

	public TaskDao getSubTaskById(int id){
		
		for(TaskDao task:this.subTasks){
			if(task.getTaskId() == id)
				return task;
		}
		return null;
	}
	public List<TaskDao>getAllSubTasks(){
		
		return this.subTasks;
	}
	/**
	 * @return the isSeen
	 */
	public boolean isSeen() {
		return isSeen;
	}
	/**
	 * @param isSeen the isSeen to set
	 */
	public void setSeen(boolean isSeen) {
		this.isSeen = isSeen;
	}

	/**
	 * @return the taskDescription
	 */
	public String getTaskDescription() {
		return taskDescription;
	}

	/**
	 * @param taskDescription the taskDescription to set
	 */
	public void setTaskDescription(String taskDescription) {
		this.taskDescription = taskDescription;
	}

	
}
