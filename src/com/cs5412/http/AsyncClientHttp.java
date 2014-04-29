/**
 * Class exposing the Apache commons CloseableHttpAsyncClient functionality for aynchronous,non-blocking HTTP requests
 */
package com.cs5412.http;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kt466
 *
 */
public class AsyncClientHttp {
	
	private static final Logger LOG = LoggerFactory.getLogger(AsyncClientHttp.class);
	private RequestConfig requestConfig;
	private CloseableHttpAsyncClient httpclient;
	private HttpGet[] requests;
	
	public AsyncClientHttp(){
		
		httpclient = HttpAsyncClients.createDefault();
		
	}
	
	public void setRequests(String urlPrefix,String[] urlList){
		requests = new HttpGet[urlList.length];
		for(int i=0;i<urlList.length;i++){
			requests[i] = new HttpGet(urlPrefix+urlList[i]);
		}
	}
	public static void executeRequests(String prefix, String[] reqs){
		try{
			 
			CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
			   
			httpclient.start();
			Future<HttpResponse> future = null;
			HttpGet reqURL = null;
			HttpResponse resp = null;
			for(int j=0;j<reqs.length;j++) {
				reqURL = new HttpGet(prefix + reqs[j]);
				future = httpclient.execute(reqURL, null);
				resp = future.get();
				LOG.debug(reqURL.getRequestLine() + "->" + resp.getStatusLine());
			}
			future.get();
			httpclient.close();
		}catch(Exception e){
			LOG.error("Error",e);
		}
		
	}
	public void execute(boolean blockLast) throws InterruptedException, IOException{
		try{
			Future<HttpResponse> resp = null;;
			httpclient.start();
		    for (HttpGet request: requests) {
	        	resp = httpclient.execute(request, null);
	        }
		    if(blockLast)
		    	resp.get();
	     }catch(Exception e){
	    	 LOG.debug("Error in AsyncClientHttp execute method", e);
	     }
		
	}
	/*
	public void execute() throws InterruptedException, IOException{
		try{
			httpclient.start();
			final CountDownLatch latch = new CountDownLatch(requests.length);
	        for (final HttpGet request: requests) {
	        	lastReqResponse = httpclient.execute(request, new FutureCallback<HttpResponse>() {
	
	                public void completed(final HttpResponse response) {
	                    latch.countDown();
	                    LOG.debug(request.getRequestLine() + "->" + response.getStatusLine());
	                }
	
	                public void failed(final Exception ex) {
	                    latch.countDown();
	                    LOG.debug(request.getRequestLine() + "->" + ex);
	                }
	
	                public void cancelled() {
	                    latch.countDown();
	                    LOG.debug(request.getRequestLine() + " cancelled");
	                }
	
	            });
	        }
	        latch.await();
	        LOG.debug("Shutting down");
	     }catch(Exception e){
	    	 LOG.debug("Error in AsyncClientHttp execute method", e);
	     }finally{
	    	 
	     }
		
	}*/

	public void close(){
		try {
			LOG.debug("Shutting down");
			httpclient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
