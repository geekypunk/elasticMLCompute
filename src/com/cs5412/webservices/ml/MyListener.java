package com.cs5412.webservices.ml;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class MyListener implements ServletContextListener {
	public static String workPath = "F:"+File.separator;
	public static String crossvalidation = workPath + "crossvalidation"+File.separator;
	public static String model = crossvalidation + "model"+File.separator;
	public static String output = crossvalidation + "output"+File.separator;
    public MyListener() {
        // TODO Auto-generated constructor stub
    }

    public void contextInitialized(ServletContextEvent sce) {
		try{
			File dir = new File(crossvalidation);
			//FileUtils.cleanDirectory(dir);
			dir.mkdir();
			dir = new File(model);
			dir.mkdir();
			dir = new File(output);
			dir.mkdir();
		}catch(Exception e){
			e.printStackTrace();
		}
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // TODO Auto-generated method stub
    }
    
	
}
