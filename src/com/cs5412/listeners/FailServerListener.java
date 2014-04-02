package com.cs5412.listeners;

import java.util.Timer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.cs5412.daemons.FailedServerHandle;

@WebListener
public class FailServerListener implements ServletContextListener{
	FailedServerHandle fsTask;
	private static int deamonStartPeriod = 120*1000;
	
    public void contextInitialized(ServletContextEvent sce) {
    	ServletContext application = sce.getServletContext();
    	PropertiesConfiguration config = (PropertiesConfiguration)application.getAttribute("config");
    	int poll = config.getInt("SET_FAILEDSERVER_POLL");
    	if(poll == 1){
	    	Timer time = new Timer(); // Instantiate Timer Object
	    	fsTask = new FailedServerHandle(application); // Instantiate SheduledTask class
			time.schedule(fsTask, 0, deamonStartPeriod); // Create Repetitively task for every 1 secs
    	}
    }
    
    public void contextDestroyed(ServletContextEvent arg0) {
    	fsTask = null;
    }
}
