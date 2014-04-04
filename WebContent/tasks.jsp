<!DOCTYPE html>
<html lang="en">
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
	<meta charset="utf-8">
	<title>Cloud Admin | Tasks</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1, user-scalable=no">
	<meta name="description" content="">
	<meta name="author" content="">
	<!-- STYLESHEETS --><!--[if lt IE 9]><script src="js/flot/excanvas.min.js"></script><script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script><script src="http://css3-mediaqueries-js.googlecode.com/svn/trunk/css3-mediaqueries.js"></script><![endif]-->
	<link rel="stylesheet" type="text/css" href="css/cloud-admin.css" >
	<link rel="stylesheet" type="text/css"  href="css/themes/default.css" id="skin-switcher" >
	<link rel="stylesheet" type="text/css"  href="css/responsive.css" >
	
	<link href="font-awesome/css/font-awesome.min.css" rel="stylesheet">
	<!-- JQUERY UI-->
	<link rel="stylesheet" type="text/css" href="js/jquery-ui-1.10.3.custom/css/custom-theme/jquery-ui-1.10.3.custom.min.css" />
	<!-- DATE RANGE PICKER -->
	<link rel="stylesheet" type="text/css" href="js/bootstrap-daterangepicker/daterangepicker-bs3.css" />
	<!-- DATA TABLES -->
	<link rel="stylesheet" type="text/css" href="js/datatables/media/css/jquery.dataTables.min.css" />
	<link rel="stylesheet" type="text/css" href="js/datatables/media/assets/css/datatables.min.css" />
	<link rel="stylesheet" type="text/css" href="js/datatables/extras/TableTools/media/css/TableTools.min.css" />
	<link href='css/fonts.css' rel='stylesheet' type='text/css'>
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
			<!-- SAMPLE BOX CONFIGURATION MODAL FORM-->
			<div class="modal fade" id="box-config" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
				<div class="modal-dialog">
				  <div class="modal-content">
					<div class="modal-header">
					  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					  <h4 class="modal-title">Box Settings</h4>
					</div>
					<div class="modal-body">
					  Here goes box setting content.
					</div>
					<div class="modal-footer">
					  <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
					  <button type="button" class="btn btn-primary">Save changes</button>
					</div>
				  </div>
				</div>
			  </div>
			<!-- /SAMPLE BOX CONFIGURATION MODAL FORM-->
			<div class="container">
				<div class="row">
					<div id="content" class="col-lg-12">
						<!-- DATA TABLES -->
						<div class="row">
							<div class="col-md-12">
								<!-- BOX -->
								<div class="box border green">
									<div class="box-title">
										<h4><i class="fa fa-table"></i>Tasks</h4>
										<div class="tools hidden-xs">
											
											<a href="javascript:;" class="reload">
												<i class="fa fa-refresh"></i>
											</a>
											
										</div>
									</div>
									<div class="box-body">
										<table id="datatable1" cellpadding="0" cellspacing="0" border="0" class="datatable table table-striped table-bordered table-hover">
											<thead>
												<tr>
													<th>Task Type</th>
													<th>Task Description</th>
													<th>Status</th>
												</tr>
											</thead>
											<tbody>
											</tbody>
											<tfoot>
												<tr>
													<th>Task Type</th>
													<th>Task Description</th>
													<th>Status</th>
												</tr>
											</tfoot>
										</table>
									</div>
								</div>
								<!-- /BOX -->
							</div>
						</div>
												
					</div><!-- /CONTENT-->
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
	
		
	<!-- DATE RANGE PICKER -->
	<script src="js/bootstrap-daterangepicker/moment.min.js"></script>
	
	<script src="js/bootstrap-daterangepicker/daterangepicker.min.js"></script>
	<!-- SLIMSCROLL -->
	<script type="text/javascript" src="js/jQuery-slimScroll-1.3.0/jquery.slimscroll.min.js"></script><script type="text/javascript" src="js/jQuery-slimScroll-1.3.0/slimScrollHorizontal.min.js"></script>
	<!-- BLOCK UI -->
	<script type="text/javascript" src="js/jQuery-BlockUI/jquery.blockUI.min.js"></script>
	
	<!-- COOKIE -->
	<script type="text/javascript" src="js/jQuery-Cookie/jquery.cookie.min.js"></script>
	<!-- CUSTOM SCRIPT -->
	<script src="js/script.js"></script>
	<script src="js/notifications.js"></script>
	<!-- DATA TABLES -->
	<script type="text/javascript" src="js/datatables/media/js/jquery.dataTables.min.js"></script>
	<script type="text/javascript" src="js/datatables/media/assets/js/datatables.min.js"></script>
	<script type="text/javascript" src="js/datatables/extras/TableTools/media/js/TableTools.min.js"></script>
	<script type="text/javascript" src="js/datatables/extras/TableTools/media/js/ZeroClipboard.min.js"></script>
	<script>
		jQuery(document).ready(function() {		
			App.setPage("dynamic_table");  //Set current page
			App.init(); //Initialise plugins and elements
		});
	</script>
	<script>
	function getTaskTypeIcon(type){

		if(type==="Dataset Upload")
			return '<i class="fa fa-cloud-upload"></i>';
		else if(type==="Algorithm Execution")
			return '<i class="fa fa-tasks"></i>';
		else
			return '';
	}	
	function getTaskStatusIcon(status){

		if(status==="SUCCESS")
			return '<span class="label label-info">SUCCESS</span>';
		else if(status==="RUNNING")
			return '<span class="label label-warning">RUNNING</span>';
		else if(status==="FAILURE")
			return '<span class="label label-danger">FAILURE</span>';
	}

	$.ajax({
	    url : "taskui/tasks/getAllTasks",
	    type: "GET",
	    dataType : "json",
	    data : {
	    	
	    },
	    success: function(data, textStatus, jqXHR)
	    {
	    	var r = new Array(), j = -1;
			var row;
			for (var key=0, size=data.length; key<size; key++){
				row = new Array();
			    row.push(getTaskTypeIcon(data[key].taskType)+data[key].taskType);
			    row.push(data[key].taskDescription);
			    row.push(getTaskStatusIcon(data[key].status));
			    r.push(row);
			   
			}
			$("#datatable1").dataTable().fnDestroy();
       		$('#datatable1').dataTable({
                "sPaginationType": "bs_full"
            }).fnAddData(r);
    
           
	        $('.datatable').each(function(){
	            var datatable = $(this);
	            // SEARCH - Add the placeholder for Search and Turn this into in-line form control
	            var search_input = datatable.closest('.dataTables_wrapper').find('div[id$=_filter] input');
	            search_input.attr('placeholder', 'Search');
	            search_input.addClass('form-control input-sm');
	            // LENGTH - Inline-Form control
	            var length_sel = datatable.closest('.dataTables_wrapper').find('div[id$=_length] select');
	            length_sel.addClass('form-control input-sm');
	        });
 			
	    },
	    error: function (jqXHR, textStatus, errorThrown)
	    {
	 			console.log(errorThrown);
	    }
	});

	$(".reload").click(function(){
  		$.ajax({
		    url : "taskui/tasks/getAllTasks",
		    type: "GET",
		    dataType : "json",
		    data : {
		    	
		    },
		    success: function(data, textStatus, jqXHR)
		    {
		    	var r = new Array(), j = -1;
				var row;
				for (var key=0, size=data.length; key<size; key++){
					row = new Array();
				    row.push(getTaskTypeIcon(data[key].taskType)+data[key].taskType);
				    row.push(data[key].taskDescription);
				    row.push(getTaskStatusIcon(data[key].status));
				    r.push(row);
				   
				}
		
				$("#datatable1").dataTable().fnClearTable();
	       		$('#datatable1').dataTable().fnAddData(r);
	     
	 			
		    },
		    error: function (jqXHR, textStatus, errorThrown)
		    {
		 			console.log(errorThrown);
		    }
		});
	});
	</script>
	
	<!-- /JAVASCRIPTS -->
</body>
</html>