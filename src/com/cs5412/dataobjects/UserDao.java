package com.cs5412.dataobjects;


public class UserDao{

	public static final String FULLNAME="fullName";
	public static final String EMAIL="email";
	public static final String USERNAME="username";
	public static final String PASSWORD="password";
	
	
	private String fullName;
	private String email;
	private String username;
	private String password;

	public UserDao() {}
	public UserDao( String fullName, String email, String username, String password )
    {
		this.fullName = fullName;
		this.email    =	 email;
		this.username = username;
		this.password = password;
    }
	/**
	 * @return the fullName
	 */
	public String getFullName() {
		return fullName;
	}
	/**
	 * @param fullName the fullName to set
	 */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
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
	
	  

        

 
}
