package com.cs5412.webservices.parameters;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cs5412.filesystem.impl.FileSystemImpl;
import com.cs5412.utils.HTTPConstants;
import com.google.common.collect.Maps;

/**
 * Servlet implementation class KMeansParameters
 * @author kt466
 */
@WebServlet("/KMeansParameters")
public class KMeansParameters extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public KMeansParameters() {
        super();
        // TODO Auto-generated constructor stub
    }

    FileSystemImpl fs = new FileSystemImpl();
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter responseWriter = response.getWriter();
		
		Map<String,String[]> params = Maps.newHashMap(request.getParameterMap());
	
		if(params.containsKey(HTTPConstants.GET_DATASETS)){
		
			Collection<File> files = fs.getUploadedDatasets();
			JSONArray filesJson = new JSONArray();
			JSONObject jsonFile;
			for(File file : files){
				try {
					jsonFile = new JSONObject();
					jsonFile.put("optionValue", file.getName());
					jsonFile.put("optionDisplay", file.getName());
					filesJson.put(jsonFile);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			responseWriter.write(filesJson.toString());
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String dataset = request.getParameter(HTTPConstants.DS_UI_PARAM_NAME);
		response.getWriter().write(dataset);
	}
	

}
