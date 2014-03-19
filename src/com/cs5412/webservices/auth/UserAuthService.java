package com.cs5412.webservices.auth;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.cs5412.user.UserManager;
import com.cs5412.user.UserManagerImpl;
import com.google.common.collect.Maps;

@Path("/auth")
public class UserAuthService {

	static final Logger LOG = LoggerFactory.getLogger(UserAuthService.class);
	
	@Path("/register")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response register(
			@FormParam("fullName") String name,
			@FormParam("email") String email,
			@FormParam("username") String username,
			@FormParam("password") String password,
			@Context ServletContext context,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response
			) throws Exception {
		
		CouchbaseClient couchBaseClient = (CouchbaseClient) context.getAttribute("couchbaseClient");
		Map<String,String> params = Maps.newHashMap();
		params.put("fullName", name);
		params.put("email", email);
		params.put("username", username);
		params.put("password", password);
		UserManager um = new UserManagerImpl(couchBaseClient);
		um.createUser(params);
	
		//Create session and login the user
		HttpSession session = request.getSession();
	    session.setAttribute("user", username);
	    //setting session to expiry in 30 mins
	    //session.setMaxInactiveInterval(30*60);
	    Cookie loginCookie = new Cookie("user",username);
	    //setting cookie to expiry in 30 mins
	    //loginCookie.setMaxAge(30*60);
	    response.addCookie(loginCookie);
	    //response.sendRedirect("/elasticMLCompute/index.jsp");
		return Response.status(200).entity("success").build();
				
				
	
	}
	
	@Path("/login")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response login(
			@FormParam("username") String username,
			@FormParam("password") String password,
			@Context HttpServletRequest request,
			@Context HttpServletResponse response,
			@Context ServletContext context
			) throws Exception {
				
		CouchbaseClient couchbaseClient = (CouchbaseClient) context.getAttribute("couchbaseClient"); 
		
		UserManager um = new UserManagerImpl(couchbaseClient);
		if(um.authenticateUser(username, password)){
			 HttpSession session = request.getSession();
		     session.setAttribute("user", username);
		     //setting session to expiry in 30 mins
		     //session.setMaxInactiveInterval(30*60);
		     Cookie loginCookie = new Cookie("user",username);
		    //setting cookie to expiry in 30 mins
		    //loginCookie.setMaxAge(30*60);
		     response.addCookie(loginCookie);
		     //response.sendRedirect("/elasticMLCompute/index.jsp");
		     return Response.status(200).entity("success").build();
		}else{
			return Response.status(200).entity("failure").build();
		}
			
				
	
	}
	@Path("/logout")
	@GET
	@Consumes("application/x-www-form-urlencoded")
	public void logout(
			@Context HttpServletRequest request,
			@Context HttpServletResponse response,
			@Context ServletContext context
			) throws Exception {
				
		response.setContentType("text/html");
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
        for(Cookie cookie : cookies){
            if(cookie.getName().equals("JSESSIONID")){
                System.out.println("JSESSIONID="+cookie.getValue());
            }
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
        }
        //invalidate the session if exists
        HttpSession session = request.getSession(false);
        System.out.println("User="+session.getAttribute("user"));
        if(session != null){
            session.invalidate();
        }
        //no encoding because we have invalidated the session
        response.sendRedirect("/elasticMLCompute/login.jsp");
	
	}
	
	
	
}

