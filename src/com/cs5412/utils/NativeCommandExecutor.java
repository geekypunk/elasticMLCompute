package com.cs5412.utils;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author kt466
 *
 *<p><b>Singleton class for executing a native command. Based on http://howtodoinjava.com/2012/10/22/singleton-design-pattern-in-java/</b></p>
 */
public class NativeCommandExecutor implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final ExecutorService THREAD_POOL 
							= Executors.newCachedThreadPool();
	
	private NativeCommandExecutor() {
		
	}

	private static class NativeCommandExecutorHolder {
		public static final NativeCommandExecutor INSTANCE = new NativeCommandExecutor();
	}

	public static NativeCommandExecutor getInstance() {
		return NativeCommandExecutorHolder.INSTANCE;
	}

	protected Object readResolve() {
		return getInstance();
	}
	public int execute(final String cmd,long timeout) throws InterruptedException, ExecutionException, TimeoutException{
		  return timedCall(new Callable<Integer>() {
		        public Integer call() throws Exception
		        {
		            Process process = Runtime.getRuntime().exec(cmd); 
		            return process.waitFor();
		        }}, timeout, TimeUnit.SECONDS);
		
	}
	private static <T> T timedCall(Callable<T> c, long timeout, TimeUnit timeUnit)
		    throws InterruptedException, ExecutionException, TimeoutException
		{
		    FutureTask<T> task = new FutureTask<T>(c);
		    THREAD_POOL.execute(task);
		    return task.get(timeout, timeUnit);
		}
}