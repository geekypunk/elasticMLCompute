package com.cs5412.webservices.reports;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.CouchbaseClient;
import com.cs5412.filesystem.IFileSystem;
import com.cs5412.taskmanager.TaskManager;

/**
 * <p><b>This class supports the Reports page on the client</b></p>
 * @author kt466
 *
 */
@Path("/reports")
public class ReportsService {
	static final Logger LOG = LoggerFactory.getLogger(ReportsService.class);
	@Context ServletContext context;
	TaskManager taskManager;
	IFileSystem fs;
	
	@PostConstruct
    void initialize() {
		taskManager = new TaskManager((CouchbaseClient)context.getAttribute("couchbaseClient"));
		fs = (IFileSystem) context.getAttribute("fileSystem");

    }
	@Path("/getAllReports")
	@GET
	public Response getAllReports(
			@Context HttpServletRequest request,
			@Context HttpServletResponse response
			) throws Exception {
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		String path = fs.getUserPath(username)+File.separator+"reports";
		List<LocatedFileStatus> dir = (List<LocatedFileStatus>) fs.getFilesInPath(new org.apache.hadoop.fs.Path(path));
		JSONArray reportsJSONArray = new JSONArray();
		JSONObject report;
		for(FileStatus file:dir){
			report = new JSONObject();
			report.put("name", file.getPath().getName());
			report.put("size", file.getLen());
			report.put("createdAt", new Date(file.getModificationTime()));
			reportsJSONArray.put(report);
			
		}
		return Response.status(200).entity(reportsJSONArray.toString()).build();
	}

	@Path("/getReport/{reportId}")
	@GET
	public Response getReportData(
			@PathParam("reportId") String reportId,
			@Context HttpServletRequest request
			) throws Exception {
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("user");
		LOG.debug("Reading report "+reportId);
		String path = fs.getUserPath(username)+File.separator+"reports"+File.separator+reportId;
		return Response.status(200).entity(fs.readFileToString(path)).build();
	}
}
