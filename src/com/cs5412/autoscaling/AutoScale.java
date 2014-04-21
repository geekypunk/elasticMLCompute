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
	
	private static Random randomGenerator;
	private static Machine LOAD_BALANCER;
	private static SSHAdaptor lbShell;
	public AutoScale(PropertiesConfiguration cfg){
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
		String cmd = "echo \"disable server "+SERVER_POOL_NAME+"/"+availableServer+"\" | socat stdio "+HASOCKET;
		lbShell.execute(cmd);
		lbShell.disconnect();
		
	}
	
	
	
	
}
