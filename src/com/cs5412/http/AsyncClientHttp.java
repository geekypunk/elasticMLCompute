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
	private Future<HttpResponse> lastReqResponse;
	public AsyncClientHttp(){
	    requestConfig = RequestConfig.custom()
	        .setSocketTimeout(3000000)
	        .setConnectTimeout(3000000).build();
	    httpclient = HttpAsyncClients.custom()
	        .setDefaultRequestConfig(requestConfig)
	        .build();
	}
	
	public void setRequests(String urlPrefix,String[] urlList){
		requests = new HttpGet[urlList.length];
		for(int i=0;i<urlList.length;i++){
			requests[i] = new HttpGet(urlPrefix+urlList[i]);
		}
	}
	
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
		
	}

	public void blockOnLastReq() throws InterruptedException, ExecutionException{
		lastReqResponse.get();
	}
	public void close(){
		try {
			httpclient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
