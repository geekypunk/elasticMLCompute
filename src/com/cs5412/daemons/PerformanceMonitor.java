/**
 * 
 */
package com.cs5412.daemons;

import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletContext;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for autoscaling
 * @author kt466
 *
 */
public class PerformanceMonitor extends TimerTask{

	Logger LOG = LoggerFactory.getLogger(PerformanceMonitor.class);
	
	//2GB. Buffer memory
	private static final long USED_MEMORY_THRESHOLD = 2*1024*1024*1024;
	
	//5GB
	private static final long FREE_MEMORY_THRESHOLD = 4*1024*1024*1024;

	private PropertiesConfiguration config;
	private String NODE_NAME;
	private String SERVER_POOL_NAME="servers";
	private String HASOCKET="/var/run/haproxy.stat";
	private boolean isServerUp;
	private static String CMD_DISABLE;
	private static String CMD_ENABLE;
	private static final ExecutorService THREAD_POOL 
								= Executors.newCachedThreadPool();
	private static long PROCESS_EXEC_TIMEOUT = 3;
	public PerformanceMonitor(ServletContext ctx){
		this.config = (PropertiesConfiguration)ctx.getAttribute("config");
		this.NODE_NAME = SERVER_POOL_NAME+"/"+this.config.getString("NODE_NAME");
		CMD_DISABLE  = "echo \"disable server "+this.NODE_NAME+"\" | socat stdio "+HASOCKET;
		CMD_ENABLE   = "echo \"enable server "+this.NODE_NAME+"\" | socat stdio "+HASOCKET;
		this.isServerUp = this.config.getBoolean("SERVER_UP");
	}
	
	/**
	 * Total memory available - buffer 
	 * @return totalMemory
	 */
	public long getMaxMemory(){
		return Runtime.getRuntime().maxMemory();
	}
	public long getFreeMemory(){
		return  Runtime.getRuntime().maxMemory() - getUsedMemory();
	}
	
	public long getUsedMemory(){
		
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}
	@Override
	public void run() {
		try {
			long maxMemory = getMaxMemory();
			long usedMemory = getUsedMemory();
			long freeMemory = getFreeMemory();
			if(this.isServerUp == true && usedMemory+USED_MEMORY_THRESHOLD>=maxMemory){
				deRegisterFromLB();
			}
			else if(this.isServerUp == false && freeMemory>=FREE_MEMORY_THRESHOLD){
				registerWithLB();
			}
		} catch (Exception e) {
			LOG.debug("Error",e);
		}
		
	}
	
	private void deRegisterFromLB(){
		try {
			LOG.debug("Deregistering "+this.NODE_NAME);
		    int returnCode = timedCall(new Callable<Integer>() {
		        public Integer call() throws Exception
		        {
		            Process process = Runtime.getRuntime().exec(CMD_DISABLE); 
		            return process.waitFor();
		        }}, PROCESS_EXEC_TIMEOUT, TimeUnit.SECONDS);
		    LOG.debug("SUCCESS :  Deregisteration of "+this.NODE_NAME);
		} 
		    
		catch (TimeoutException e) {
			LOG.debug("FAILED :  Deregisteration of "+this.NODE_NAME);
			LOG.debug("Error",e);
		} catch (InterruptedException e) {
			LOG.debug("FAILED :  Deregisteration of "+this.NODE_NAME);
			LOG.debug("Error",e);
		} catch (ExecutionException e) {
			LOG.debug("FAILED :  Deregisteration of "+this.NODE_NAME);
			LOG.debug("Error",e);
		} 
        
	}
	
	private void registerWithLB(){
		
		try {
			LOG.debug("Registering "+this.NODE_NAME);
		    int returnCode = timedCall(new Callable<Integer>() {
		        public Integer call() throws Exception
		        {
		            Process process = Runtime.getRuntime().exec(CMD_ENABLE); 
		            return process.waitFor();
		        }}, PROCESS_EXEC_TIMEOUT, TimeUnit.SECONDS);
		    LOG.debug("SUCCESS :  Registeration of "+this.NODE_NAME);
		} 
		    
		catch (TimeoutException e) {
			LOG.debug("FAILED :  Registeration of "+this.NODE_NAME);
			LOG.debug("Error",e);
		} catch (InterruptedException e) {
			LOG.debug("FAILED :  Registeration of "+this.NODE_NAME);
			LOG.debug("Error",e);
		} catch (ExecutionException e) {
			LOG.debug("FAILED :  Registeration of "+this.NODE_NAME);
			LOG.debug("Error",e);
		}
        
		
	}
	private static <T> T timedCall(Callable<T> c, long timeout, TimeUnit timeUnit)
		    throws InterruptedException, ExecutionException, TimeoutException
		{
		    FutureTask<T> task = new FutureTask<T>(c);
		    THREAD_POOL.execute(task);
		    return task.get(timeout, timeUnit);
		}

}
