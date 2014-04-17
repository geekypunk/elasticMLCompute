package com.cs5412.ssh;

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
        LOG.info("Executing: " + command+" on "+machine.getIpAddress());
        channelExec.setCommand(command);
        channelExec.connect();
        LOG.info("execution continues");
    }

    public SSHAdaptor stop(){
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