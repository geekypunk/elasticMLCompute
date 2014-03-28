package com.cs5412.utils;

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.SystemUtils;

public class ServerConstants {
	
	//Only needed if request is Cross-Origin 
	public static final String SERVER_URL = "http://10.32.32.7:8181/elasticMLCompute/";
	private static final String TMP_DIRECTORY_WIN = "C:"+File.separator+"tmp";
	private static final String TMP_DIRECTORY_LINUX = System.getProperty("user.home")+File.separator+"data"+File.separator+"tmp";

	public static final String linuxSeparator = "/";
	
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
