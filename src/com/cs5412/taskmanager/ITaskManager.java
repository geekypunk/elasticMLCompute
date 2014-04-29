package com.cs5412.taskmanager;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * <p><b>TaskManager semantics. Provides the "Log Before Execution" functionality to complement fault-tolerance. 
 * Uses session store(CouchBase) for persistence</b></p>
 * @author kt466
 *
 */
public interface ITaskManager {
	/**
	 * Register a task
	 * @param task
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	void registerTask(TaskDao task) throws InterruptedException, ExecutionException;
	
	/**
	 * Retrive a task by its id
	 * @param id
	 * @return
	 */
	TaskDao getTaskById(String id);
	
	/**
	 * Set the status of a task
	 * @param task
	 * @param status
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	void setTaskStatus(TaskDao task,TaskStatus status) throws InterruptedException, ExecutionException;
	
	/**
	 * Used by notification service. Complements the UI notification element for alerting user about finished tasks
	 * @param username
	 * @return
	 */
	List<TaskDao> getFinishedAndUnseenByUserId(String username);
	
	/**
	 * Used by notification service
	 * @param taskId
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	void markAsSeen(String taskId) throws InterruptedException, ExecutionException;
	
	/**
	 * Used by notification service
	 * @param username
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	void markAllAsSeen(String username) throws InterruptedException, ExecutionException;
	
	void removeParentDependency(String taskId, String username)throws Exception;
	
	/**
	 * Delete a task by id
	 * @param taskId
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	void removeTaskById(String taskId)throws InterruptedException, ExecutionException;
	
	/**
	 * Get all tasks for a given user
	 * @param username
	 * @return
	 */
	List<TaskDao> getAllTasksForUser(String username);

}
