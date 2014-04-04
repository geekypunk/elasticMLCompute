<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
	<meta charset="utf-8">
	<title>Elastic ML Cloud Admin | DashBoard</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1, user-scalable=no">
	<meta name="description" content="">
	<meta name="author" content="">
	<!-- STYLESHEETS --><!--[if lt IE 9]><script src="js/flot/excanvas.min.js"></script><script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script><script src="http://css3-mediaqueries-js.googlecode.com/svn/trunk/css3-mediaqueries.js"></script><![endif]-->
	<link rel="stylesheet" type="text/css" href="css/cloud-admin.css" >
	<link rel="stylesheet" type="text/css"  href="css/themes/default.css" id="skin-switcher" >
	<link rel="stylesheet" type="text/css"  href="css/responsive.css" >
	
	<link href="font-awesome/css/font-awesome.min.css" rel="stylesheet">
	<!-- DATE RANGE PICKER -->
	<link rel="stylesheet" type="text/css" href="js/bootstrap-daterangepicker/daterangepicker-bs3.css" />
	<!-- TYPEAHEAD -->
	<link rel="stylesheet" type="text/css" href="js/typeahead/typeahead.css" />
	<!-- SELECT2 -->
	<link rel="stylesheet" type="text/css" href="js/select2/select2.min.css" />
	<!-- UNIFORM -->
	<link rel="stylesheet" type="text/css" href="js/uniform/css/uniform.default.min.css" />
	<link href='css/fonts.css' rel='stylesheet' type='text/css'>
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
							<span class="username">John Doe</span>
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
									<li><a class="" href="kernel.jsp"><span class="sub-menu-text">KERNEL</span></a></li>
									<li><a class="" href="decisiontree.jsp"><span class="sub-menu-text">DECISION TREE</span></a></li>
									<li><a class="" href="wsd.jsp"><span class="sub-menu-text">WSD</span></a></li>
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
						<!-- ADVANCED -->
						<div class="row">
							<div class="col-md-12">
								<!-- BOX -->
								<div class="box border green">
									<div class="box-title">
										<h4><i class="fa fa-bars"></i>Parameters</h4>
										
									</div>
									<div class="box-body">
										<form id="kernelForm" class="form-horizontal">
										  <div class="form-group">
											 <label class="col-md-2 control-label" for="e1">Training Data<span class="required"></span></label> 
											 <div class="col-md-10">
												<select id="e1" name="trainingDataset" class="col-md-12">
																				
												</select>												
											 </div>
										  </div>
										  <div class="form-group">
											 <label class="col-md-2 control-label" for="e2">Test Data<span class="required"></span></label> 
											 <div class="col-md-10">
												<select id="e2" name="testDataset" class="col-md-12">
												   								   
												</select>												
											 </div>
										  </div>
										  <div class="form-group">
											 <label class="col-md-2 control-label" for="e3">Kernel Type<span class="required"></span></label> 
											 <div class="col-md-10">
												<select id="e3" name="kernelType" class="col-md-12">
													<option value="polyKernel">Polynomial Kernel</option>		
													<option value="rbFunction">Radial Basis Function</option>
													<option value="sigmoidFunction">Sigmoid Function</option>	
													<option value="blank" selected></option>			   
												</select>												
											 </div>
										  </div>
										  <div class="form-group" id="kernelParam">
										  
										  </div>
										  <button type="submit" id="btn-load-complete" class="btn btn-success" data-complete-text="Run again!" data-loading-text="Running...">Run!</button>
									   </form>
									   <br/>
									   <button id="btn-chart" class="btn btn-success" data-complete-text="Refresh" data-loading-text="Refresh...">Display Chart</button>
									   <br/>
									 
									  
									  
										
									</div>
								</div>
								<!-- /BOX -->
							</div>
							<!-- KERNEL CHART -->
							<div class="col-md-12" id="kernelChart" style="display:none;">
								<!-- BOX -->
								<div class="box border blue" style="width:90%">
									<div class="box-title">
										<h4><i class="fa fa-signal"></i>Interactive Chart</h4>
										<div class="tools">
											<a href="#box-config" data-toggle="modal" class="config">
												<i class="fa fa-cog"></i>
											</a>
											<a href="javascript:;" class="reload">
												<i class="fa fa-refresh"></i>
											</a>
											<a href="javascript:;" class="collapse">
												<i class="fa fa-chevron-up"></i>
											</a>
											<a href="javascript:;" class="remove">
												<i class="fa fa-times"></i>
											</a>
										</div>
									</div>
									<div class="box-body">
										<div id="chart_2" class="chart"></div>
									</div>
								</div>
								<!-- /BOX -->
							</div>
							<!-- /KERNEL CHART -->
						</div>
						<!-- /ADVANCED -->
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
	
	<!-- SLIMSCROLL -->
	<script type="text/javascript" src="js/jQuery-slimScroll-1.3.0/jquery.slimscroll.min.js"></script><script type="text/javascript" src="js/jQuery-slimScroll-1.3.0/slimScrollHorizontal.min.js"></script>
	<!-- BLOCK UI -->
	<script type="text/javascript" src="js/jQuery-BlockUI/jquery.blockUI.min.js"></script>
	<!-- TYPEHEAD -->
	<script type="text/javascript" src="js/typeahead/typeahead.min.js"></script>
	<!-- AUTOSIZE -->
	<script type="text/javascript" src="js/autosize/jquery.autosize.min.js"></script>
	<!-- COUNTABLE -->
	<script type="text/javascript" src="js/countable/jquery.simplyCountable.min.js"></script>
	<!-- INPUT MASK -->
	<script type="text/javascript" src="js/bootstrap-inputmask/bootstrap-inputmask.min.js"></script>
	<!-- SELECT2 -->
	<script type="text/javascript" src="js/select2/select2.min.js"></script>
	<!-- UNIFORM -->
	<script type="text/javascript" src="js/uniform/jquery.uniform.min.js"></script>
	<!-- FLOT CHARTS -->
	<script src="js/flot/jquery.flot.min.js"></script>
	<script src="js/flot/jquery.flot.time.min.js"></script>
    <script src="js/flot/jquery.flot.selection.min.js"></script>
	<script src="js/flot/jquery.flot.resize.min.js"></script>
    <script src="js/flot/jquery.flot.pie.min.js"></script>
    <script src="js/flot/jquery.flot.stack.min.js"></script>
    <script src="js/flot/jquery.flot.crosshair.min.js"></script>
	<!-- COOKIE -->
	<script type="text/javascript" src="js/jQuery-Cookie/jquery.cookie.min.js"></script>
	<!-- WIZARD -->
	<script src="js/bootstrap-wizard/jquery.bootstrap.wizard.min.js"></script>
	<!-- WIZARD -->
	<script src="js/jquery-validate/jquery.validate.min.js"></script>
	<script src="js/jquery-validate/additional-methods.min.js"></script>
	<!-- CUSTOM SCRIPT -->
	<!-- Notification Script-->
	<script src="js/notifications.js"></script>
	<script src="js/script.js"></script>
	<script src="js/svm.charts.js"></script>
	<script src="js/kernel.js"></script>
	<script>
		jQuery(document).ready(function() {		
			App.setPage("forms");  //Set current page
			App.init(); //Initialise plugins and elements
			
		});
	</script>
	<!-- /JAVASCRIPTS -->
</body>
</html>