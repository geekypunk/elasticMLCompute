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
import com.cs5412.ssh.Machine;
import com.cs5412.ssh.SSHAdaptor;
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
	private static final long FREE_MEMORY_THRESHOLD = 1*GB;

	private PropertiesConfiguration config;
	private String NODE_NAME;
	private String SERVER_POOL_NAME="servers";
	private String HASOCKET="/var/run/haproxy.stat";
	private static String HAPROXYCTL = "/opt/haproxyctl/haproxyctl";
	private boolean isServerUp;
	private static String CMD_DISABLE;
	private static String CMD_ENABLE;
	private static long PROCESS_EXEC_TIMEOUT = 3;
	private static NativeCommandExecutor cmdExecutor;
	private static AutoScale autoScaler;
	private static SSHAdaptor lbShell;
	private static Machine LOAD_BALANCER;
	
	public PerformanceMonitor(ServletContext ctx){
		this.config = (PropertiesConfiguration)ctx.getAttribute("config");
		autoScaler = new AutoScale(config);
		this.NODE_NAME = SERVER_POOL_NAME+"/"+this.config.getString("NODE_NAME");
		CMD_DISABLE  = "echo \"disable server "+this.NODE_NAME+"\" | socat stdio "+HASOCKET;
		CMD_ENABLE   = "echo \"enable server "+this.NODE_NAME+"\" | socat stdio "+HASOCKET;
		this.isServerUp = this.config.getBoolean("SERVER_UP");
		cmdExecutor = NativeCommandExecutor.getInstance();
		
		LOAD_BALANCER = new Machine(config.getString("LOAD_BALANCER_USER"), 
				config.getString("LOAD_BALANCER_PWD"), 
				config.getString("LOAD_BALANCER_IP"));
		lbShell = new SSHAdaptor(LOAD_BALANCER);
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
			LOG.info("Perf stats:"+" Max:"+(double)maxMemory/GB);
			LOG.info("Perf stats:"+" Used:"+(double)usedMemory/GB);
			LOG.info("Perf stats:"+" Free:"+(double)freeMemory/GB);
			if(this.isServerUp == true && usedMemory+USED_MEMORY_THRESHOLD>=maxMemory && !this.config.getBoolean("IS_RESERVE")){
				
				deRegisterFromLB();
				autoScaler.scaleUp();
				this.isServerUp = false;
			}
			else if(this.isServerUp == false && freeMemory>=FREE_MEMORY_THRESHOLD){
				
				
				registerWithLB();
				this.isServerUp = true;
			
			}
		} catch (Exception e) {
			LOG.debug("Error",e);
		}
		
	}
	
	/**
	 * Deregister this node from the load balancer by establishing an SSH tunnel to the unix socket exposed by HAProxy 
	 * load balancer
	 * @throws Exception
	 */
	private void deRegisterFromLB() throws Exception{
		try {
			LOG.debug("Deregistering "+this.NODE_NAME);
			lbShell = lbShell.connect();
			lbShell.execute(CMD_DISABLE);
			lbShell.disconnect();
			//cmdExecutor.execute(CMD_DISABLE, PROCESS_EXEC_TIMEOUT);
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
	
	/**
	 * Register this node from the load balancer by establishing an SSH tunnel to the unix socket exposed by HAProxy 
	 * load balancer
	 * @throws Exception
	 */
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
