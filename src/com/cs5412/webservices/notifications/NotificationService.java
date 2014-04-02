package com.cs5412.webservices.notifications;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.cs5412.taskmanager.TaskDao;
import com.cs5412.taskmanager.TaskManager;
@Path("/notifications")
public class NotificationService {
	
	static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);
	@Context ServletContext context;
	TaskManager taskManager;
	
	@PostConstruct
    void initialize() {
		taskManager = new TaskManager((CouchbaseClient)context.getAttribute("couchbaseClient"));

    }
	
	@Path("/poller")
	@GET
	public Response isAlive(){
		return Response.status(200).entity("Hello").build();
	}
	
	/**
	 * Get finished and unseen tasks
	 */
	@Path("/getFinishedTasks")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFinishedTasks(
			@Context HttpServletRequest request,
			@Context HttpServletResponse response
			) throws Exception {
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		List<TaskDao> finishedAndUnseenTasks = taskManager.getFinishedAndUnseenByUserId(username); 
		JSONArray result = new JSONArray();
		JSONObject taskObj;
		for(TaskDao task : finishedAndUnseenTasks){
			taskObj = new JSONObject();
			taskObj.put("taskId", task.getTaskId());
			taskObj.put("taskName", task.getTaskName());
			taskObj.put("reportUri", task.getReportUrl());
			result.put(taskObj);
			
		}
		return Response.status(200).entity(result.toString()).build();
		
	}
	
	/**
	 * Get finished and unseen tasks
	 */
	@Path("/markAllAsSeen")
	@GET
	@Consumes("application/x-www-form-urlencoded")
	public void setTaskAsSeen(
			@Context HttpServletRequest request,
			@Context HttpServletResponse response
			) throws Exception {
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		taskManager.markAllAsSeen(username);
		
	}

}
