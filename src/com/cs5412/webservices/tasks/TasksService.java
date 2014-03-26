package com.cs5412.webservices.tasks;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
import com.cs5412.dataobjects.TaskDao;
import com.cs5412.taskmanager.TaskManager;
import com.cs5412.webservices.notifications.NotificationService;

/**
 * Class for viewing and managing tasks
 */
@Path("/tasks")
public class TasksService {
	
	static final Logger LOG = LoggerFactory.getLogger(TasksService.class);
	@Context ServletContext context;
	TaskManager taskManager;
	
	@PostConstruct
    void initialize() {
		taskManager = new TaskManager((CouchbaseClient)context.getAttribute("couchbaseClient"));

    }
	@Path("/getAllTasks")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllTasks(
			@Context HttpServletRequest request,
			@Context HttpServletResponse response
			) throws Exception {
				
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		List<TaskDao> tasks = taskManager.getAllTasksForUser(username);
		JSONArray result = new JSONArray();
		JSONObject taskObj;
		for(TaskDao task : tasks){
			taskObj = new JSONObject();
			taskObj.put("taskId", task.getTaskId());
			taskObj.put("taskType", task.getTaskType());
			taskObj.put("taskDescription", task.getTaskDescription());
			taskObj.put("status", task.getStatus());
			result.put(taskObj);
		}
		return Response.status(200).entity(result.toString()).build();
		
		
	}

}
