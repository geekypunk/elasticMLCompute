package com.cs5412.listeners;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
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
import com.cs5412.daemons.PerformanceMonitor;
import com.cs5412.filesystem.HDFSFileSystemImpl;
import com.cs5412.filesystem.IFileSystem;
import com.cs5412.ssh.Machine;
import com.cs5412.ssh.SSHAdaptor;
import com.cs5412.utils.Utils;
import com.google.gson.Gson;

/**
 * @author kt466
 *
 */
@WebListener
public class WebAppListener implements ServletContextListener {
	static final Logger LOG = LoggerFactory.getLogger(WebAppListener.class);
	private static SSHAdaptor lbShell;
	private static Machine LOAD_BALANCER;
	private String NODE_NAME;
	private String SERVER_POOL_NAME="servers";
	private String HASOCKET="/var/run/haproxy.stat";
	private static String CMD_DISABLE;
	private static String CMD_ENABLE;
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
		    
		    /*
		    String webAppPath = application.getRealPath("/");
			String log4jPropPath = webAppPath + "WEB-INF/log4j.properties";
			log4jIntialization(log4jPropPath);
		    */
		    
		    /*Code added to add all users task status */
		    Gson gson = new Gson();
		    ArrayList<String> taskIds = new ArrayList<String>();
		    couchbaseClient.add("AllUserTaskIds", gson.toJson(taskIds));
		    /*End of Code added*/
		    
		    /*Code added to add version number of the server to keep track of the number of restarts*/
		    Integer versionNumber = -1;
		    couchbaseClient.add(Utils.getIP(), versionNumber).get();
		    versionNumber = (Integer) couchbaseClient.get(Utils.getIP());
		    
		    if(versionNumber != null){
		    	versionNumber++;
			    couchbaseClient.set(Utils.getIP(), versionNumber);
			    
		    }
		    /*End of Code added*/
		  	application.setAttribute("couchbaseClient", couchbaseClient);	
		  
		  	//Monitor server statistics to prevent OOM crash
		  	PerformanceMonitor perfMonitor = new PerformanceMonitor(application);
			Timer time = new Timer();
			time.schedule(perfMonitor, 0,5*1000);
			
			this.NODE_NAME = SERVER_POOL_NAME+"/"+config.getString("NODE_NAME");
			CMD_DISABLE  = "echo \"disable server "+this.NODE_NAME+"\" | socat stdio "+HASOCKET;
		  	LOAD_BALANCER = new Machine(config.getString("LOAD_BALANCER_USER"), 
					config.getString("LOAD_BALANCER_PWD"), 
					config.getString("LOAD_BALANCER_IP"));
			lbShell = new SSHAdaptor(LOAD_BALANCER);
			lbShell = lbShell.connect();
			lbShell.execute(CMD_DISABLE);
			lbShell.disconnect();
			
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
    /*
    private static void log4jIntialization(String log4jPropPath){
		if(log4jPropPath!= null && new File(log4jPropPath).exists()){
			System.out.println("Intializing log4j with " + log4jPropPath);
			PropertyConfigurator.configure(log4jPropPath);
		}
		else{
			System.err.println("ContextInitializer :" + log4jPropPath + " file not found\n" 
					+ "Initializing log4j with BasicConfigurator");
			BasicConfigurator.configure();
		}
		Logger log = LoggerFactory.getLogger(WebAppListener.class);
		log.info("log4j Initialized");
	}*/
}
