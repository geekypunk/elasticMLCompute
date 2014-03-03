package com.cs5412.database.mongodb;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cs5412.dataobjects.UserDao;
import com.cs5412.listeners.WebAppListener;
import com.cs5412.utils.PasswordHash;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class UserManagerImpl implements UserManager{

	static final Logger LOG = LoggerFactory.getLogger(WebAppListener.class);
	private DB db;
	public UserManagerImpl(MongoClient mongoClient){
		this.db = mongoClient.getDB("user_sessions");
	}
	@Override
	public void createUser(Map params) {
		try{
		DBCollection table = db.getCollection("user");
		UserDao user = new UserDao();
		user.put(UserDao.FULLNAME, params.get(UserDao.FULLNAME));
		user.put(UserDao.EMAIL, params.get(UserDao.EMAIL));
		user.put(UserDao.USERNAME, params.get(UserDao.USERNAME));
		String passwordHash = PasswordHash.generatePasswordHash((String)params.get(UserDao.PASSWORD));
		user.put(UserDao.PASSWORD,passwordHash);
		table.insert(user);
		}catch(Exception e){
			LOG.error("Error", e);
			
		}
		
	}
	@Override
	public boolean authenticateUser(String username,String password) throws Exception {
		// TODO Auto-generated method stub
		DBCollection table = db.getCollection("user");
		BasicDBObject whereQuery = new BasicDBObject();
		whereQuery.put(UserDao.USERNAME, username);
		DBObject user = table.findOne(whereQuery);
		if(user!=null){
			String storedHash = (String) user.get("password");
			boolean passwordHash = PasswordHash.validatePassword(password, storedHash);
			return passwordHash;
		}
		return false;
		
	}

}
