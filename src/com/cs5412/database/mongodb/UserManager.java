package com.cs5412.database.mongodb;

import java.util.Map;

import com.mongodb.DBObject;

public interface UserManager {
	
	void createUser(Map requestParams);
	boolean authenticateUser(String username,String password) throws Exception;

}
