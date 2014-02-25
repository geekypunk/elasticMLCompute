package com.cs5412.webservices.ml;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cs5412.filesystem.IFileSystem;
import com.cs5412.filesystem.impl.HDFSFileSystemImpl;
import com.cs5412.filesystem.impl.LocalFileSystemImpl;
import com.cs5412.utils.ServerConstants;
import com.cs5412.webservices.fileupload.FileUploadServlet;

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
			application.setAttribute("isLinux", SystemUtils.IS_OS_LINUX);
			if(SystemUtils.IS_OS_LINUX){
				application.setAttribute("fileSystem", new HDFSFileSystemImpl(ServerConstants.HDFS_URI));	
			}else{
				application.setAttribute("fileSystem", new LocalFileSystemImpl());	
			}
			
		}catch(Exception e){
			LOG.error("Error", e);
		}
    }

    public void contextDestroyed(ServletContextEvent sce) {
    	ServletContext application = sce.getServletContext();
        IFileSystem fs = (IFileSystem) application.getAttribute("fileSystem");
        try {
        	if(fs!=null)
        		fs.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error("Error", e);
		}
    }
    
	
}
