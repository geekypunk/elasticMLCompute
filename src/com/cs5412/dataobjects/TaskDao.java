package com.cs5412.dataobjects;

import java.util.List;

import com.google.common.collect.Lists;

public class TaskDao {
	private String usedId;
	private String taskName;
	private String reportUrl;
	private boolean isFinished;
	private boolean isSeen;
	private int taskId;
	List<TaskDao> subTasks = Lists.newArrayList();
	public int getTaskId(){
		return taskId;
	}
	public void setTaskId(int id){
		
		taskId = id;
	}
	/**
	 * @return the usedId
	 */
	public String getUsedId() {
		return usedId;
	}
	/**
	 * @param usedId the usedId to set
	 */
	public void setUsedId(String usedId) {
		this.usedId = usedId;
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
	 * @return the isFinished
	 */
	public boolean isFinished() {
		return isFinished;
	}
	/**
	 * @param isFinished the isFinished to set
	 */
	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
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
}
