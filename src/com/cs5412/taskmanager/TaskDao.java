package com.cs5412.taskmanager;

import java.net.Inet4Address;
import java.util.*;

import com.cs5412.utils.Utils;

public class TaskDao {
	private String userId;
	private String taskName;
	private String taskDescription;
	private String taskType;
	private String reportUrl;
	private String hostAddress;
	private String hostVersion;
	private String wsURL;
	private TaskStatus status;
	private boolean isSeen;
	private String taskId;
	private boolean isParent;
	private ArrayList<String> parentTaskIds = new ArrayList<String>();
	//private HttpServletRequest httpRequest
	List<Integer> subTasks = new ArrayList<Integer>();
	
	public String getHostVersion() {
		return hostVersion;
	}

	public void setHostVersion(String hostVersion) {
		this.hostVersion = hostVersion;
	}
	
	public String getHostAddress() {
		return hostAddress;
	}

	public void setHostAddress(String hostAddress) {
		this.hostAddress = hostAddress;
	}

	public TaskDao(String userId,String taskName,String reportUrl,TaskStatus status,boolean isSeen,String wsURL) throws Exception{
		
		this.userId = userId;
		this.taskName = taskName;
		this.reportUrl = reportUrl;
		this.status = status;
		this.isSeen = isSeen;
		this.taskId = Utils.getUUID();
		
		this.hostAddress = Utils.getIP();
		this.wsURL = wsURL;
	}
	
	public String getTaskId(){
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
		//if(this.subTasks.size() ==0)
			return status;
	//	else{
	//		for(TaskDao task: this.subTasks){
	//			if(task.status == TaskStatus.FAILURE)
	//				return TaskStatus.FAILURE;
	//		}
	//		return TaskStatus.SUCCESS;
	//	}
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(TaskStatus status) {
		this.status = status;
	}
	
	public void addSubTask(int task){
		
		this.subTasks.add(task);
		
	}

	/*
	public TaskDao getSubTaskById(int id){
		
		for(TaskDao task:this.subTasks){
			if(task.getTaskId() == id)
				return task;
		}
		return null;
	}*/
	public List<Integer>getAllSubTasks(){
		
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

	/**
	 * @return the taskType
	 */
	public String getTaskType() {
		return taskType;
	}

	/**
	 * @param taskType the taskType to set
	 */
	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	/**
	 * @return the parentTaskId
	 */
	public ArrayList<String> getParentTaskId() {
		return parentTaskIds;
	}

	/**
	 * @param parentTaskId the parentTaskId to set
	 */
	public void setParentTaskId(ArrayList<String> parentTaskIds) {
		this.parentTaskIds = parentTaskIds;
	}

	public String getWsURL() {
		return wsURL;
	}

	public void setWsURL(String wsURL) {
		this.wsURL = wsURL;
	}

	/**
	 * @return the isParent
	 */
	public boolean isParent() {
		return isParent;
	}

	/**
	 * @param isParent the isParent to set
	 */
	public void setParent(boolean isParent) {
		this.isParent = isParent;
	}

	
}
