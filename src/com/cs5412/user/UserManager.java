package com.cs5412.user;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.cs5412.filesystem.IFileSystem;

/**
 * <p><b>Interface governing a set of actions for while registeration.</b></p> 
 * @author kt466
 *
 */
public interface UserManager {
	
	/**
	 * Create a user
	 * @param requestParams
	 */
	void createUser(Map<String,String> requestParams);
	
	/**
	 * Authenticate a registered user
	 * @param username
	 * @param password
	 * @return
	 * @throws Exception
	 */
	boolean authenticateUser(String username,String password) throws Exception;
	
	/**
	 * Create a HDFS namespace for a registered user
	 * @param fs
	 * @param username
	 * @throws IOException
	 */
	void createHDFSNamespace(IFileSystem fs,String username) throws IOException;
	
	/**
	 * Create a empty task list in session store for a registered user
	 * @param username
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	void createEmptyTaskList(String username) throws InterruptedException, ExecutionException;
	
}
