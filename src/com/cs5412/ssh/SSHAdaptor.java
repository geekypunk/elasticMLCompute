package com.cs5412.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SSHAdaptor {
    private JSch jsch;
    private ChannelExec channelExec;
    private Session session;
    private Machine machine;
    private static final Logger LOG = LoggerFactory.getLogger(SSHAdaptor.class);
    private static int SESSION_TIMEOUT = 3000;
    private InputStream inputStream;
    private boolean flag;
    public SSHAdaptor(Machine machine) {
        this.machine = machine;
        this.jsch = new JSch();
    }

    public SSHAdaptor connect() throws Exception {
        LOG.info("connecting to " + machine);
        session = jsch.getSession(machine.getUserName(), machine.getIpAddress(), machine.getSshPort());
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(machine.getPassword());
        session.connect(SESSION_TIMEOUT);

        channelExec = (ChannelExec) session.openChannel("exec");
        LOG.info("connected to " + machine);
        return this;
    }

    public void execute(String command) throws Exception {
    	flag = true;
        LOG.info("Executing: " + command+" on "+machine.getIpAddress());
        this.inputStream = channelExec.getInputStream();
        channelExec.setCommand(command);
        channelExec.connect();
        LOG.info("execution continues");
    }

    public String getShellOutput() throws IOException{
    	StringBuilder sb = new StringBuilder();
    	String line;
    	BufferedReader reader = new BufferedReader(new InputStreamReader(this.inputStream));
    	while ((line = reader.readLine()) != null){
    		
    		sb.append(line).append("\n");
    	 
    	}
    	reader.close();
    	return sb.toString();
    		 
    	
    }
    public SSHAdaptor stop(){
    	flag = false;
        LOG.info("execution stopped");
        return this;
    }

    public SSHAdaptor disconnect() throws Exception {
        int exitCode = channelExec.getExitStatus();
        LOG.info("Exitcode is "+exitCode);
        if (channelExec != null) channelExec.disconnect();
        if (session != null) session.disconnect();
        LOG.info("disconnected from " + machine);
        return this;
    }

    
}