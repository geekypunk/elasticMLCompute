package com.cs5412.utils;

import java.io.File;

public class ServerConstants {
	
	//Only needed if request is Cross-Origin 
	public static final String SERVER_URL = "http://localhost:8080/elasticMLCompute/";
	public static final String UPLOAD_DIRECTORY_ROOT = "C:"+File.separator+"upload";
	public static final String UPLOAD_DIRECTORY_TRAIN = "C:"+File.separator+"upload"+File.separator+"train";
	public static final String UPLOAD_DIRECTORY_TEST = "C:"+File.separator+"upload"+File.separator+"test";
	public static final String UPLOAD_DIRECTORY_OTHER = "C:"+File.separator+"upload"+File.separator+"other";
	public static final String TMP_DIRECTORY = "C:"+File.separator+"tmp";
	public static final int MEMORY_THRESHOLD   = 1024 * 1024 * 100;  // 500MB
	public static final int UPLOAD_BUFFER   = 1024 * 1024 * 5;  // 5MB
	
	//These should not exceed -Xmx argument
	public static final int MAX_FILE_SIZE      = 1024 * 1024 * 1024; // 500MB
	public static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 1024; // 500MB
}
