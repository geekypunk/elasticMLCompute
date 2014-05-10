package com.cs5412.ssh;

/**
 * <p><b>Represents a machine. To be used by SSHAdaptor</p></b>
 * @author kt466
 *
 */
public class Machine {
	
	private String userName;
	private String password;
	private String ipAddress;
	private int sshPort=22;
	
	public Machine(String _user,String _pwd,String _ip){
		this.userName = _user;
		this.password = _pwd;
		this.ipAddress = _ip;
		
	}
	
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return the ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}
	/**
	 * @param ipAddress the ipAddress to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	/**
	 * @return the sshPort
	 */
	public int getSshPort() {
		return sshPort;
	}
	/**
	 * @param sshPort the sshPort to set
	 */
	public void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}

	@Override
	public String toString(){
		
		return this.userName+"@"+this.ipAddress+":"+this.sshPort +" Password:"+this.password;
	}
}
