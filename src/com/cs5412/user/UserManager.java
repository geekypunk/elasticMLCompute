package com.cs5412.user;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.cs5412.filesystem.IFileSystem;

public interface UserManager {
	
	void createUser(Map<String,String> requestParams);
	boolean authenticateUser(String username,String password) throws Exception;
	void createHDFSNamespace(IFileSystem fs,String username) throws IOException;
	void createEmptyTaskList(String username) throws InterruptedException, ExecutionException;
	
}
