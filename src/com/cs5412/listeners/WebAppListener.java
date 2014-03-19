package com.cs5412.listeners;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.cs5412.dataobjects.TaskDao;
import com.cs5412.dataobjects.UserDao;
import com.cs5412.filesystem.IFileSystem;
import com.cs5412.filesystem.impl.HDFSFileSystemImpl;
import com.cs5412.utils.ServerConstants;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

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
			HDFSFileSystemImpl fs = new HDFSFileSystemImpl(ServerConstants.HDFS_URI);
			fs.createUserSpace("admin");
			application.setAttribute("fileSystem", fs);	
			//List of couchbase nodes
			List<URI> hosts = Arrays.asList(
				      new URI("http://127.0.0.1:8091/pools")
				    );
			Gson gson = new Gson();
		    // Name of the Bucket to connect to
		    String bucket = "default";
		   // Password of the bucket (empty) string if none
		    String password = "";
		    // Connect to the Cluster
		    CouchbaseClient couchbaseClient = new CouchbaseClient(hosts, bucket, password);
		    
		    //Setup schema
		   // HashMap<String,UserDao> usersMap = Maps.newHashMap();
		   // HashMap<String,TaskDao> tasksMap = Maps.newHashMap();
		   // couchbaseClient.add("users", gson.toJson(usersMap));
		   // couchbaseClient.add("tasks", gson.toJson(tasksMap));
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
