package com.cs5412.listeners;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.cs5412.filesystem.HDFSFileSystemImpl;
import com.cs5412.filesystem.IFileSystem;
import com.cs5412.taskmanager.TaskDao;
import com.cs5412.taskmanager.TaskDaoAdaptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebListener
public class WebAppListener implements ServletContextListener {
	static final Logger LOG = LoggerFactory.getLogger(WebAppListener.class);
    public WebAppListener() {
        // TODO Auto-generated constructor stub
    }

    public void contextInitialized(ServletContextEvent sce) {
    	/**
    	 * Only for testing
    	 * Always load HDFSFileSystemImpl in production
    	 * */
		try{
			ServletContext application = sce.getServletContext();
			
			PropertiesConfiguration config = new PropertiesConfiguration();
			config.load(application.getResourceAsStream("/WEB-INF/config.properties"));
			HDFSFileSystemImpl fs = new HDFSFileSystemImpl(config);
			application.setAttribute("config", config);
			application.setAttribute("fileSystem", fs);	
			//List of couchbase nodes
			List<URI> hosts = Arrays.asList(
				      new URI(config.getString("COUCH_URI")+"/pools")
				    );
		    CouchbaseClient couchbaseClient = new CouchbaseClient(
		    		hosts, config.getString("COUCH_BUCKET_NAME"), config.getString("COUCH_BUCKET_PWD"));
		    
		    /*Code added to add all users task status */
		    Gson gson = new Gson();
		    ArrayList<String> taskIds = new ArrayList<String>();
		    couchbaseClient.add("AllUser"+"TaskIds", gson.toJson(taskIds)).get();
		    /*End of Code added*/
		    
		  	application.setAttribute("couchbaseClient", couchbaseClient);	
			
			
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
