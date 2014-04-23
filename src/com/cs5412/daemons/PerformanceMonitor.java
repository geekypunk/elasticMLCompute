/**
 * 
 */
package com.cs5412.daemons;

import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletContext;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cs5412.autoscaling.AutoScale;
import com.cs5412.utils.NativeCommandExecutor;

/**
 * @author kt466
 * <p><b>Class responsible for crash prevention due to OOM</b></p>
 */
public class PerformanceMonitor extends TimerTask{

	Logger LOG = LoggerFactory.getLogger(PerformanceMonitor.class);
	
	
	private static final long GB = 1024*1024*1024;
	//2GB. Buffer memory
	private static final long USED_MEMORY_THRESHOLD = 1*GB;
	
	//5GB
	private static final long FREE_MEMORY_THRESHOLD = 2*GB;

	private PropertiesConfiguration config;
	private String NODE_NAME;
	private String SERVER_POOL_NAME="servers";
	private String HASOCKET="/var/run/haproxy.stat";
	private boolean isServerUp;
	private static String CMD_DISABLE;
	private static String CMD_ENABLE;
	private static long PROCESS_EXEC_TIMEOUT = 3;
	private static NativeCommandExecutor cmdExecutor;
	private static AutoScale autoScaler;
	
	public PerformanceMonitor(ServletContext ctx){
		this.config = (PropertiesConfiguration)ctx.getAttribute("config");
		autoScaler = new AutoScale(config);
		this.NODE_NAME = SERVER_POOL_NAME+"/"+this.config.getString("NODE_NAME");
		CMD_DISABLE  = "echo \"disable server "+this.NODE_NAME+"\" | socat stdio "+HASOCKET;
		CMD_ENABLE   = "echo \"enable server "+this.NODE_NAME+"\" | socat stdio "+HASOCKET;
		this.isServerUp = this.config.getBoolean("SERVER_UP");
		cmdExecutor = NativeCommandExecutor.getInstance();
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
			LOG.info("Perf stats:"+" Max:"+maxMemory/(long)GB);
			LOG.info("Perf stats:"+" Used:"+usedMemory/(long)GB);
			LOG.info("Perf stats:"+" Free:"+freeMemory/(long)GB);
			if(this.isServerUp == true && usedMemory+USED_MEMORY_THRESHOLD>=maxMemory){
				deRegisterFromLB();
				autoScaler.scaleUp();
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
			cmdExecutor.execute(CMD_DISABLE, PROCESS_EXEC_TIMEOUT);
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
			cmdExecutor.execute(CMD_ENABLE, PROCESS_EXEC_TIMEOUT);
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
	

}
