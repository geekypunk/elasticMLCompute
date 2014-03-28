package com.cs5412.user;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import jersey.repackaged.com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.cs5412.filesystem.IFileSystem;
import com.cs5412.listeners.WebAppListener;
import com.cs5412.taskmanager.TaskDao;
import com.cs5412.utils.PasswordHash;
import com.cs5412.utils.ServerConstants;
import com.google.gson.Gson;

public class UserManagerImpl implements UserManager{

	static final Logger LOG = LoggerFactory.getLogger(WebAppListener.class);
	private CouchbaseClient couchbaseClient;
	private Gson gson;
	public UserManagerImpl(CouchbaseClient _couchbaseClient){
		couchbaseClient = _couchbaseClient;
		gson = new Gson();
		
	}
	@Override
	public void createUser(Map<String,String> params) {
		try{
		UserDao user = new UserDao();	
		user.setFullName((String) params.get(UserDao.FULLNAME));
		user.setEmail((String) params.get(UserDao.EMAIL));
		user.setPassword(PasswordHash.generatePasswordHash((String) params.get(UserDao.PASSWORD)));
		user.setUsername((String) params.get(UserDao.USERNAME));
		couchbaseClient.set((String) params.get(UserDao.USERNAME), gson.toJson(user)).get();
		}catch(Exception e){
			LOG.error("Error", e);
			
		}
		
	}
	@Override
	public boolean authenticateUser(String username,String password) throws Exception {
		// TODO Auto-generated method stub
		UserDao user = gson.fromJson((String)couchbaseClient.get(username),UserDao.class);
		if(user!=null){
			String storedHash = user.getPassword();
			boolean passwordHash = PasswordHash.validatePassword(password, storedHash);
			return passwordHash;
		}
		return false;
		
	}
	@Override
	public void createHDFSNamespace(IFileSystem fs,String username) throws IOException {
		// TODO Auto-generated method stub
		fs.getUserPath(username);
		fs.createDir(fs.getUserPath(username).toString()+ServerConstants.linuxSeparator+"train", true);
		fs.createDir(fs.getUserPath(username).toString()+ServerConstants.linuxSeparator+"test", true);
		fs.createDir(fs.getUserPath(username).toString()+ServerConstants.linuxSeparator+"reports", true);
		fs.createDir(fs.getUserPath(username).toString()+ServerConstants.linuxSeparator+"others", true);
	}
	
	@Override
	public void createEmptyTaskList(String username) throws InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		//List<TaskDao> tasks = Lists.newArrayList();
		Map<Integer,TaskDao> tasks = new HashMap<Integer, TaskDao>();
		couchbaseClient.set(username+"Tasks",gson.toJson(tasks)).get();
	}

}
