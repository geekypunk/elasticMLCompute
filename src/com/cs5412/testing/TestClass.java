package com.cs5412.testing;

import java.lang.reflect.Type;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.couchbase.client.CouchbaseClient;
import com.cs5412.taskmanager.TaskDao;
import com.cs5412.taskmanager.TaskDaoAdaptor;
import com.cs5412.taskmanager.TaskManager;
import com.cs5412.taskmanager.TaskStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class TestClass {
	public static void main(String[] args) throws Exception{
		System.out.println(System.getProperty("java.library.path"));
		//PropertiesConfiguration config = new PropertiesConfiguration("../WEB-INF/config.properties");
	
		List<URI> hosts = Arrays.asList(
			      new URI("http://192.168.56.101:8091/pools")
			    );
	    CouchbaseClient couchbaseClient = new CouchbaseClient(
	    		hosts, "default", "");
	    TaskManager taskManager = new TaskManager(couchbaseClient);
		final GsonBuilder gsonBuilder = new GsonBuilder();
	    gsonBuilder.registerTypeAdapter(TaskDao.class, new TaskDaoAdaptor());
	    gsonBuilder.setPrettyPrinting();
	    Gson gson = gsonBuilder.create();
	    Type type = new TypeToken<ArrayList<String>>(){}.getType();
	    ArrayList<String> tasks = gson.fromJson((String) couchbaseClient.get("AllUser"+"TaskIds"), type);
	    if(tasks != null){
		    for(String str : tasks){
//		    	System.out.println(str);
		    	TaskDao td = taskManager.getTaskById(str);
		    	System.out.println(td.getWsURL() + " : " + td.getTaskId() + " : " + td.getParentTaskId() + " : " + td.getStatus());
		    }
	    }
//	    Gson gson = new Gson();
//	    Type collectionType = new TypeToken<ArrayList<ArrayList<Double>>>(){}.getType();
//		ArrayList<ArrayList<Double>> allAccuracies = gson.fromJson((String) couchbaseClient.get("om" + "DTAcc"), collectionType);	
//		System.out.println(allAccuracies);
//		couchbaseClient.flush();
	 /*   ArrayList<ArrayList<Integer>> myList = new ArrayList<ArrayList<Integer>>();
	    ArrayList<Integer> list1 = new ArrayList<Integer>();
	    ArrayList<Integer> list2 = new ArrayList<Integer>();
	    list1.add(1);list1.add(2);
	    list2.add(1);list2.add(2);
	    myList.add(list1);myList.add(list2);
	    String json = gson.toJson(myList);
	    System.out.println(json);
	    
	    Type collectionType = new TypeToken<ArrayList<ArrayList<Integer>>>(){}.getType();
	    ArrayList<ArrayList<Integer>> ints2 = gson.fromJson(json, collectionType);
	    System.out.println(ints2);
	    final GsonBuilder gsonBuilder = new GsonBuilder();
	    gsonBuilder.registerTypeAdapter(TaskDao.class, new TaskDaoAdaptor());
	    gsonBuilder.setPrettyPrinting();
	    gson = gsonBuilder.create();
	    HashMap<Integer,TaskDao> tasks = new HashMap<Integer, TaskDao>();
	    Type type = new TypeToken<HashMap<Integer,TaskDao>>(){}.getType();
	    tasks = gson.fromJson((String) couchbaseClient.get("AllUser"+"Tasks"), type);
	    for(Entry<Integer, TaskDao> ent : tasks.entrySet()){
	    	TaskDao tda = ent.getValue();
	    	ArrayList<Integer> map = tda.getParentTaskId();
	    	System.out.println(map.size());
	    }*/
//		couchbaseClient.shutdown();
	}
}
