package com.cs5412.user;

import java.util.Map;

public interface UserManager {
	
	void createUser(Map<String,String> requestParams);
	boolean authenticateUser(String username,String password) throws Exception;

}
