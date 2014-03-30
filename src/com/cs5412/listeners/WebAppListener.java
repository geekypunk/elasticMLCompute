package com.cs5412.listeners;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.cs5412.daemons.FailedTaskPoller;
import com.cs5412.filesystem.HDFSFileSystemImpl;
import com.cs5412.filesystem.IFileSystem;

@WebListener
public class WebAppListener implements ServletContextListener {
	static final Logger LOG = LoggerFactory.getLogger(WebAppListener.class);
	private static int deamonStartPeriodMillis = 100*1000; // Time intervals in which session cleanup daemon is invoked
    public WebAppListener() {
        // TODO Auto-generated constructor stub
    }

    public void contextInitialized(ServletContextEvent sce) {
    	/**
    	 * Only for testing
    	 * Always load HDFSFileSystemImpl in production
    	 * */
		try{
			ServletContext context = sce.getServletContext();
			
			PropertiesConfiguration config = new PropertiesConfiguration();
			config.load(context.getResourceAsStream("/WEB-INF/config.properties"));
			HDFSFileSystemImpl fs = new HDFSFileSystemImpl(config);
			context.setAttribute("config", config);
			context.setAttribute("fileSystem", fs);	
			//List of couchbase nodes
			List<URI> hosts = Arrays.asList(
				      new URI(config.getString("COUCH_URI")+"/pools")
				    );
		    CouchbaseClient couchbaseClient = new CouchbaseClient(
		    		hosts, config.getString("COUCH_BUCKET_NAME"), config.getString("COUCH_BUCKET_PWD"));
		  	context.setAttribute("couchbaseClient", couchbaseClient);	
		  	
		  	//Set up daemon tasks
		  	Timer time = new Timer(); // Instantiate Timer Object
		  	FailedTaskPoller st = new FailedTaskPoller(context); // Instantiate SheduledTask class
			time.schedule(st, 0, deamonStartPeriodMillis); // Create Repetitively task for every 1 secs
		}catch(Exception e){
			LOG.error("Error", e);
		}
    }

    public void contextDestroyed(ServletContextEvent sce) {
    	ServletContext application = sce.getServletContext();
        IFileSystem fs = (IFileSystem) application.getAttribute("fileSystem");
        CouchbaseClient couchbaseClient = (CouchbaseClient) application.getAttribute("couchbaseClient");
        try {
        	if(fs!=null)
        		fs.close();
        	if(couchbaseClient!=null)
        		couchbaseClient.shutdown();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error("Error", e);
		}
    }
    
   	
}
