package com.cs5412.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;


public class Utils {
	
	public static final String SERVER_URL = "http://10.32.32.7:8181/elasticMLCompute/";
	
	public static final String linuxSeparator = "/";
	
	//For uniqueness across nodes, appending with IP address
	public static String getUUID(){
		String uid = UUID.randomUUID().toString();
		try{
			return uid+getIP();
		}catch(Exception e){
			return uid;
		}
		
	}
	
	public static String getIP() throws SocketException{
		String ip = null;
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()){
		    NetworkInterface current = interfaces.nextElement();
		    if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
		    Enumeration<InetAddress> addresses = current.getInetAddresses();
		    while (addresses.hasMoreElements()){
		        InetAddress current_addr = addresses.nextElement();
		        if (current_addr.isLoopbackAddress()) continue;
		        if (current_addr instanceof Inet4Address)
		        	  ip =  (current_addr.getHostAddress());
		        
		    }
		}
		return ip;
	}

	public static void main(String[] args)throws Exception{
		System.out.println(Utils.getIP());
	}
	
	public static void sendHTTPAsyncReqs(){
		
		
	}
	
}
