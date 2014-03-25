package com.cs5412.webservices.ml.dt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import org.slf4j.Logger;

public class ParallelDTJob implements Runnable{
    private Logger LOG;
	private String username;
	private int i;
	private String loadBalancerAddress;
	public ArrayList<ArrayList<Double>> allAccuracies;
	
	public ParallelDTJob(String username, String loadBalancerAddress, int i, Logger LOG, ArrayList<ArrayList<Double>> allAccuracies){
		this.username = username;
		this.loadBalancerAddress = loadBalancerAddress;
		this.i = i;
		this.LOG = LOG;
		this.allAccuracies = allAccuracies;
	}
	
	public void run() {
		try{
			String taskUrl = loadBalancerAddress + "/elasticMLCompute/ml/dTree/generateEachService" + "/" + username + "/" + i;
	    	URL url = new URL(taskUrl);
	    	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(1000000);
	        conn.setConnectTimeout(1000000);
	        conn.setRequestMethod("GET");
	        conn.setUseCaches(false);
	        conn.setDoInput(true);
	        conn.setDoOutput(true);
	        conn.connect();
	        LOG.debug(conn.getResponseCode() + "");
	        
	        String acc = "";
	        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
	            acc = readStream(conn.getInputStream());
	        }
	        ArrayList<Double> accuracy = new ArrayList<Double>();
	        String[] strAcc = acc.split(" ");
	        for(String str : strAcc) accuracy.add(Double.parseDouble(str));
	        allAccuracies.add(accuracy);
		}catch(Exception e){
			LOG.error("Error: " + e);
		}
	}
	
	private static String readStream(InputStream in) {
	    BufferedReader reader = null;
	    StringBuilder builder = new StringBuilder();
	    try {
	        reader = new BufferedReader(new InputStreamReader(in));
	        String line = "";
	        while ((line = reader.readLine()) != null) {
	            builder.append(line);
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        if (reader != null) {
	            try {
	                reader.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    return builder.toString();
	}
}
