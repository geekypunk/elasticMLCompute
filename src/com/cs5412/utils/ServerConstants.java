package com.cs5412.utils;

import java.io.File;

import org.apache.commons.lang.SystemUtils;

public class ServerConstants {
	
	//Only needed if request is Cross-Origin 
	public static final String SERVER_URL = "http://localhost:8080/elasticMLCompute/";
	public static final String UPLOAD_DIRECTORY_ROOT = "C:"+File.separator+"upload";
	public static final String UPLOAD_DIRECTORY_TRAIN = "C:"+File.separator+"upload"+File.separator+"train";
	public static final String UPLOAD_DIRECTORY_TEST = "C:"+File.separator+"upload"+File.separator+"test";
	public static final String UPLOAD_DIRECTORY_OTHER = "C:"+File.separator+"upload"+File.separator+"other";
	public static final String REPORTS_DIRECTORY = "C:"+File.separator+"reports";
	private static final String TMP_DIRECTORY_WIN = "C:"+File.separator+"tmp";
	
	private static final String TMP_DIRECTORY_LINUX = System.getProperty("user.home")+File.separator+"data"+File.separator+"tmp";
	public static final String DATA_DIRECTORY_LINUX = System.getProperty("user.home")+File.separator+"data";
	
	public static final int MEMORY_THRESHOLD   = 1024 * 1024 * 100;  // 500MB
	public static final int UPLOAD_BUFFER   = 1024 * 1024 * 5;  // 5MB
	public static final String linuxSeparator = "/";
	
	public static final String HDFS_URI = "hdfs://192.168.56.101:9000";
	//These should not exceed -Xmx argument
	public static final int MAX_FILE_SIZE      = 1024 * 1024 * 1024; // 500MB
	public static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 1024; // 500MB
	
	public static final String KNN_CV_BASE_DIR = "knn"+File.separator
	            +"crossvalidation"+File.separator;
	public static final String KNN_CV_RES_FILE = "knnCV.res";

	public static String getUploadDirTmp(){
		
		if(SystemUtils.IS_OS_WINDOWS){
			return TMP_DIRECTORY_WIN;
		}else{
			return TMP_DIRECTORY_LINUX;
		}
			
	}
	
}
