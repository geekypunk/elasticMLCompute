<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
	<meta charset="utf-8">
	<title>Elastic ML Cloud Admin | Manage your datasets</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1, user-scalable=no">
	<meta name="description" content="">
	<meta name="author" content="">
	<!-- STYLESHEETS --><!--[if lt IE 9]><script src="js/flot/excanvas.min.js"></script><script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script><script src="http://css3-mediaqueries-js.googlecode.com/svn/trunk/css3-mediaqueries.js"></script><![endif]-->
	<link rel="stylesheet" type="text/css" href="css/cloud-admin.css" >
	<link rel="stylesheet" type="text/css"  href="css/themes/default.css" id="skin-switcher" >
	<link rel="stylesheet" type="text/css"  href="css/responsive.css" >
	
	<link href="font-awesome/css/font-awesome.min.css" rel="stylesheet">
	<!-- JQUERY UPLOAD -->
	<!-- CSS to style the file input field as button and adjust the Bootstrap progress bars -->
	<link rel="stylesheet" href="js/jquery-upload/css/jquery.fileupload.css">
	<link rel="stylesheet" href="js/jquery-upload/css/jquery.fileupload-ui.css">
	<!-- FONTS -->
	<link href='http://fonts.googleapis.com/css?family=Open+Sans:300,400,600,700' rel='stylesheet' type='text/css'>
	<link rel="shortcut icon" type="image/x-icon" href="img/favicon.ico">
</head>
<body>
<%
//allow access only if session exists
String user = null;
if(session.getAttribute("user") == null){
    response.sendRedirect("login.jsp");
}else user = (String) session.getAttribute("user");
String userName = null;
String sessionID = null;
Cookie[] cookies = request.getCookies();
if(cookies !=null){
for(Cookie cookie : cookies){
    if(cookie.getName().equals("user")) userName = cookie.getValue();
    if(cookie.getName().equals("JSESSIONID")) sessionID = cookie.getValue();
}
}
%>
	<!-- HEADER -->
	<header class="navbar clearfix" id="header">
		<div class="container">
				<div class="navbar-brand">
					<!-- COMPANY LOGO -->
					<a href="index.jsp">
						<img src="img/logo/logo.png" alt="Cloud Admin Logo" class="img-responsive" height="30" width="120">
					</a>
					<!-- /COMPANY LOGO -->
					<div id="sidebar-collapse" class="sidebar-collapse btn">
						<i class="fa fa-bars" 
							data-icon1="fa fa-bars" 
							data-icon2="fa fa-bars" ></i>
					</div>
					<!-- /SIDEBAR COLLAPSE -->
				</div>
				<!-- NAVBAR LEFT -->
				
				<!-- /NAVBAR LEFT -->
				<!-- BEGIN TOP NAVIGATION MENU -->					
				<ul class="nav navbar-nav pull-right">
					<!-- BEGIN NOTIFICATION DROPDOWN -->	
					<li class="dropdown" id="header-notification">
						<a href="#" class="dropdown-toggle" data-toggle="dropdown">
							<i class="fa fa-bell"></i>
						</a>
						<ul class="dropdown-menu notification">
							<li class="dropdown-title">
								
							</li>
							
							
							<li class="footer">
								<a href="#">See all notifications <i class="fa fa-arrow-circle-right"></i></a>
							</li>
						</ul>
					</li>
					
					<!-- BEGIN USER LOGIN DROPDOWN -->
					<li class="dropdown user" id="header-user">
						<a href="#" class="dropdown-toggle" data-toggle="dropdown">
							<img alt="" src="img/avatars/avatar3.jpg" />
							<span class="username"><%=session.getAttribute("user")%></span>
							<i class="fa fa-angle-down"></i>
						</a>
						<ul class="dropdown-menu">
							<li><a href="#"><i class="fa fa-user"></i> My Profile</a></li>
							<li><a href="#"><i class="fa fa-cog"></i> Account Settings</a></li>
							<li><a href="#"><i class="fa fa-eye"></i> Privacy Settings</a></li>
							<li><a href="user/auth/logout"><i class="fa fa-power-off"></i> Log Out</a></li>
						</ul>
					</li>
					<!-- END USER LOGIN DROPDOWN -->
				</ul>
				<!-- END TOP NAVIGATION MENU -->
		</div>
		
		
	</header>
	<!--/HEADER -->
	
	<!-- PAGE -->
	<section id="page">
				<!-- SIDEBAR -->
				<div id="sidebar" class="sidebar">
					<div class="sidebar-menu nav-collapse">
						<div class="divide-20"></div>
						<!-- SEARCH BAR -->
						<div id="search-bar">
							<input class="search" type="text" placeholder="Search"><i class="fa fa-search search-icon"></i>
						</div>
						<!-- /SEARCH BAR -->
						
						<!-- SIDEBAR MENU -->
						<ul>
						
							<!-- DATASETS -->
							<li><a class="" href="upload.jsp"><i class="fa fa-pencil-square-o fa-fw"></i> <span class="menu-text">DataSets</span></a></li>
							<!-- /DATASETS -->
							
							<!-- ML ALGORITHMS MENU -->
							<li class="has-sub">
								<a href="javascript:;" class="">
								<i class="fa fa-briefcase fa-fw"></i> <span class="menu-text">Algorithms</span>
								<span class="arrow"></span>
								</a>
								<ul class="sub">
									<li><a class="" href="knn.jsp"><span class="sub-menu-text">KNN</span></a></li>
									<li><a class="" href="svm.jsp"><span class="sub-menu-text">SVM</span></a></li>
								</ul>
							</li>
							<!-- /ML ALGORITHM MENU -->
							
							<!-- REPORTS MENU -->
							<li><a class="" href="reports.jsp"><i class="fa fa-bar-chart-o fa-fw"></i> <span class="menu-text">Reports</span></a></li>
							<!-- /REPORTS MENU -->
							
							<!-- TASKS -->
							<li><a class="" href="tasks.jsp"><i class="fa fa-tasks fa-fw"></i> <span class="menu-text">Tasks</span></a></li>
							<!-- /TASKS -->
						</ul>
						<!-- /SIDEBAR MENU -->
					</div>
				</div>
				<!-- /SIDEBAR -->
		<div id="main-content">
			<div class="container">
				<div class="row">
					<div id="content" class="col-lg-12">
						<!-- PAGE HEADER-->
						<div class="row">
							
							<div class="col-md-12">
								<!-- BOX -->
								<div class="box border inverse">
									<div class="box-title">
										<h4><i class="fa fa-bars"></i>Manage your datasets</h4>
										
									</div>
									<div class="box-body">
										<form id="fileupload" action="http://localhost:8080/elasticMLCompute/FileUpload" method="POST" enctype="multipart/form-data">
										<!-- Redirect browsers with JavaScript disabled to the origin page -->
										<noscript><input type="hidden" name="redirect" value="http://localhost:8080/elasticMLCompute/FileUpload"></noscript>
										<!-- The fileupload-buttonbar contains buttons to add/delete files and start/cancel the upload -->
										<div class="divide-20"></div>
										<div class="row fileupload-buttonbar">
											<div class="col-lg-12">
												<!-- The fileinput-button span is used to style the file input field as button -->
												<span class="btn btn-success fileinput-button">
													<i class="fa fa-plus"></i>
													<span>Add files...</span>
													<input type="file" name="files[]" multiple>
												</span> &nbsp;&nbsp;
												<button type="submit" class="btn btn-primary start">
													<i class="fa fa-arrow-circle-o-up"></i>
													<span>Start upload</span>
												</button> &nbsp;&nbsp;
												<button type="reset" class="btn btn-warning cancel">
													<i class="fa fa-ban"></i>
													<span>Cancel upload</span>
												</button> &nbsp;&nbsp;
												<button type="button" class="btn btn-danger delete">
													<i class="fa fa-trash-o"></i>
													<span>Delete</span>
												</button>
												<input type="checkbox" class="toggle">
												<!-- The loading indicator is shown during file processing -->
												<span class="fileupload-loading"></span>
											</div>
											<!-- The global progress information -->
											<div class="col-lg-5 fileupload-progress fade">
												<!-- The global progress bar -->
												<div class="progress progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100">
													<div class="progress-bar progress-bar-success" style="width:0%;"></div>
												</div>
												<!-- The extended global progress information -->
												<div class="progress-extended">&nbsp;</div>
											</div>
										</div>
										<!-- The table listing the files available for upload/download -->
										<table role="presentation" class="table table-striped"><tbody class="files"></tbody></table>
									</form>
									<div class="panel panel-default">
									<div class="panel-heading">
										<h3 class="panel-title">Notes</h3>
									</div>
									<div class="panel-body">
										<ul>
											<li>Maximum file size is 2GB</li>
											<li>You can also <strong>drag &amp; drop</strong> files from your desktop.</li>
										</ul>
									</div>
								</div>
								
								<script id="template-upload" type="text/x-tmpl">
								{% for (var i=0, file; file=o.files[i]; i++) { %}
									<tr class="template-upload fade">
										<td>
											<span class="preview"></span>
										</td>
										<td>
											<p class="name">{%=file.name%}</p>
											{% if (file.error) { %}
												<div><span class="label label-danger">Error</span> {%=file.error%}</div>
											{% } %}
										</td>
										<td>
											<p class="size">{%=o.formatFileSize(file.size)%}</p>
											{% if (!o.files.error) { %}
												<div class="progress progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0"><div class="progress-bar progress-bar-success" style="width:0%;"></div></div>
											{% } %}
										</td>
										<td>
											{% if (!o.files.error && !i && !o.options.autoUpload) { %}
												<button class="btn btn-primary start">
													<i class="fa fa-arrow-circle-o-up"></i>
													<span>Start</span>
												</button>
											{% } %}
											{% if (!i) { %}
												<button class="btn btn-warning cancel">
													<i class="fa fa-ban"></i>
													<span>Cancel</span>
												</button>
											{% } %}
										</td>
									</tr>
								{% } %}
								</script>
								<!-- The template to display files available for download -->
								<script id="template-download" type="text/x-tmpl">
								{% for (var i=0, file; file=o.files[i]; i++) { %}
									<tr class="template-download fade">
										<td>
											<span class="preview">
												{% if (file.thumbnailUrl) { %}
													<a href="{%=file.url%}" title="{%=file.name%}" download="{%=file.name%}" data-gallery><img src="{%=file.thumbnailUrl%}"></a>
												{% } %}
											</span>
										</td>
										<td>
											<p class="name">
												{% if (file.url) { %}
													<a href="{%=file.url%}" title="{%=file.name%}" download="{%=file.name%}" {%=file.thumbnailUrl?'data-gallery':''%}>{%=file.name%}</a>
												{% } else { %}
													<span>{%=file.name%}</span>
												{% } %}
											</p>
											{% if (file.error) { %}
												<div><span class="label label-danger">Error</span> {%=file.error%}</div>
											{% } %}
										</td>
										<td>
											<span class="size">{%=o.formatFileSize(file.size)%}</span>
										</td>
										<td>
											{% if (file.deleteUrl) { %}
												<button class="btn btn-danger delete" data-type="{%=file.deleteType%}" data-url="{%=file.deleteUrl%}"{% if (file.deleteWithCredentials) { %} data-xhr-fields='{"withCredentials":true}'{% } %}>
													<i class="fa fa-trash-o"></i>
													<span>Delete</span>
												</button>
												<input type="checkbox" name="delete" value="1" class="toggle">
											{% } else { %}
												<button class="btn btn-warning cancel">
													<i class="fa fa-ban"></i>
													<span>Cancel</span>
												</button>
											{% } %}
										</td>
									</tr>
								{% } %}
								</script>
									</div>
								</div>
								<!-- /BOX -->
							</div>
						</div>
						<!-- /SAMPLE -->						
						<div class="footer-tools">
							<span class="go-top">
								<i class="fa fa-chevron-up"></i> Top
							</span>
						</div>
					</div><!-- /CONTENT-->
				
						</div>
						<!-- /PAGE HEADER -->
					</div>
				</div>
			</div>
		</div>
	</section>
	<!--/PAGE -->
	<!-- JAVASCRIPTS -->
	<!-- Placed at the end of the document so the pages load faster -->
	<!-- JQUERY -->
	<script src="js/jquery/jquery-2.0.3.min.js"></script>
	<!-- JQUERY UI-->
	<script src="js/jquery-ui-1.10.3.custom/js/jquery-ui-1.10.3.custom.min.js"></script>
	<!-- BOOTSTRAP -->
	<script src="bootstrap-dist/js/bootstrap.min.js"></script>
	
	<!-- JQUERY UPLOAD -->
	<!-- The Templates plugin is included to render the upload/download listings -->
	<script src="js/blueimp/javascript-template/tmpl.min.js"></script>
	<!-- The Load Image plugin is included for the preview images and image resizing functionality -->
	<script src="js/blueimp/javascript-loadimg/load-image.min.js"></script>
	<!-- The Canvas to Blob plugin is included for image resizing functionality -->
	<script src="js/blueimp/javascript-canvas-to-blob/canvas-to-blob.min.js"></script>
	<!-- blueimp Gallery script -->
	<script src="js/blueimp/gallery/jquery.blueimp-gallery.min.js"></script>
	
	<!-- The basic File Upload plugin -->
	<script src="js/jquery-upload/js/jquery.fileupload.js"></script>
	<!-- The File Upload processing plugin -->
	<script src="js/jquery-upload/js/jquery.fileupload-process.js"></script>
	<!-- The File Upload validation plugin -->
	<script src="js/jquery-upload/js/jquery.fileupload-validate.js"></script>
	<!-- The File Upload user interface plugin -->
	<script src="js/jquery-upload/js/jquery.fileupload-ui.js"></script>
	<!-- The main application script -->
	<script src="js/jquery-upload/js/main.js"></script>
	
	<!-- COOKIE -->
	<script type="text/javascript" src="js/jQuery-Cookie/jquery.cookie.min.js"></script>
	<!-- CUSTOM SCRIPT -->
	<script src="js/script.js"></script>
	<script>
		jQuery(document).ready(function() {		
			App.setPage("upload");  //Set current page
			App.init(); //Initialise plugins and elements
		});
	</script>
	<!-- Notification Script-->
	<script src="js/notifications.js"></script>
	<script>
	$.ajax({
	    url : "ui/notifications/getFinishedTasks",
	    type: "GET",
	    dataType : "json",
	    data : {
	    	
	    },
	    success: function(data, textStatus, jqXHR)
	    {
	    	handleNewNotifications(data);
	    },
	    error: function (jqXHR, textStatus, errorThrown)
	    {
	 			console.log(errorThrown);
	    }
	});
	$( "#header-notification" ).click(function() {
  		$.ajax({
		    url : "ui/notifications/markAllAsSeen",
		    type: "GET",
		    data : {
		    	
		    },
		    success: function(data, textStatus, jqXHR)
		    {
		    	
		    },
		    error: function (jqXHR, textStatus, errorThrown)
		    {
		 			console.log(errorThrown);
		    }
		});
	});
	</script>
	<!-- END Notification Script-->
	<!-- /JAVASCRIPTS -->
</body>
</html>