package com.cs5412.autoscaling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.cs5412.ssh.Machine;
import com.cs5412.ssh.SSHAdaptor;

public class AutoScale {
	private static String HAPROXYCTL = "/opt/haproxyctl/haproxyctl";
	private static String STATSCMD = " show health | grep servers | awk  '{ print $2,$3 }'";
	private static String SERVER_POOL_NAME="servers";
	private static String HASOCKET="/var/run/haproxy.stat";
	private static String CMD_DISABLE;
	private static String CMD_ENABLE;
	private static Random randomGenerator;
	private static Machine LOAD_BALANCER;
	private static SSHAdaptor lbShell;
	private String NODE_NAME;
	public AutoScale(PropertiesConfiguration cfg){
		this.NODE_NAME = SERVER_POOL_NAME+"/"+cfg.getString("NODE_NAME");
		randomGenerator = new Random();
		LOAD_BALANCER = new Machine(cfg.getString("LOAD_BALANCER_USER"), 
				cfg.getString("LOAD_BALANCER_PWD"), 
				cfg.getString("LOAD_BALANCER_IP"));
		lbShell = new SSHAdaptor(LOAD_BALANCER);
		
	}
	
	public void  scaleUp() throws Exception{
		
		lbShell = lbShell.connect();
		lbShell.execute(HAPROXYCTL+STATSCMD);
		String output = lbShell.getShellOutput();
		String[] result = output.split("\n");
		String[] line;
		List<String> availableServers = new ArrayList<String>();
		for(String s : result){
			line = s.split(" ");
			if(line[1].equalsIgnoreCase(SRVSTATUS.MAINT.toString())){
				System.out.println(line[0]);
				availableServers.add(line[0]);
			}
		}
		String availableServer = availableServers.get(randomGenerator.nextInt(availableServers.size()));
		String cmd = "echo \"enable server "+SERVER_POOL_NAME+"/"+availableServer+"\" | socat stdio "+HASOCKET;
		lbShell.execute(cmd);
		lbShell.disconnect();
		
	}
	
	public static void main(String args[]) throws Exception{
		CMD_DISABLE  = "echo \"disable server "+"servers/node2"+"\" | socat stdio "+HASOCKET;
		CMD_ENABLE   = "echo \"enable server "+"servers/node1"+"\" | socat stdio "+HASOCKET;
		lbShell = new SSHAdaptor(new Machine("kt466", "l", "128.84.216.68"));
		String output;
		
		lbShell = lbShell.connect();
		lbShell.execute(HAPROXYCTL+STATSCMD);
		output = lbShell.getShellOutput();
		System.out.println(output);
		lbShell.disconnect();
		
		
		lbShell = lbShell.connect();
		lbShell.execute(CMD_ENABLE);
		lbShell.disconnect();
		
		
		output = lbShell.getShellOutput();
		System.out.println(output);
		lbShell.disconnect();
		
		
	}
	
	
	
}
