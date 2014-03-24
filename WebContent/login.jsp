<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html lang="en">
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
	<meta charset="utf-8">
	<title>Elastic ML Cloud Admin | Login</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1, user-scalable=no">
	<meta name="description" content="">
	<meta name="author" content="">
	<!-- STYLESHEETS --><!--[if lt IE 9]><script src="js/flot/excanvas.min.js"></script><script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script><script src="http://css3-mediaqueries-js.googlecode.com/svn/trunk/css3-mediaqueries.js"></script><![endif]-->
	<link rel="stylesheet" type="text/css" href="css/cloud-admin.css" >
	
	<link href="font-awesome/css/font-awesome.min.css" rel="stylesheet">
	<!-- DATE RANGE PICKER -->
	<link rel="stylesheet" type="text/css" href="js/bootstrap-daterangepicker/daterangepicker-bs3.css" />
	<!-- UNIFORM -->
	<link rel="stylesheet" type="text/css" href="js/uniform/css/uniform.default.min.css" />
	<!-- ANIMATE -->
	<link rel="stylesheet" type="text/css" href="css/animatecss/animate.min.css" />
	<!-- FONTS -->
	<link href='http://fonts.googleapis.com/css?family=Open+Sans:300,400,600,700' rel='stylesheet' type='text/css'>
	<link rel="shortcut icon" type="image/x-icon" href="img/favicon.ico">
</head>
<body class="login">
<%
//allow access only if session exists
if(session.getAttribute("user") != null){
    response.sendRedirect("index.jsp");
}
String userName = null;
String sessionID = null;
Cookie[] cookies = request.getCookies();
if(cookies !=null){
for(Cookie cookie : cookies){
    if(cookie.getName().equals("user")) userName = cookie.getValue();
}
}
%>
<!-- PAGE -->
	<section id="page">
			<!-- HEADER -->
			<header>
				<!-- NAV-BAR -->
				<div class="container">
					<div class="row">
						<div class="col-md-4 col-md-offset-4">
							<div id="logo">
								<a href="index.html"><img src="img/logo/logo.gif" alt="logo name" /></a>
							</div>
						</div>
					</div>
				</div>
				<!--/NAV-BAR -->
			</header>
			<!--/HEADER -->
			<!-- LOGIN -->
			<section id="login" class="visible">
				<div class="container">
					<div class="row">
						<div class="col-md-4 col-md-offset-4">
							<div class="login-box-plain">
								<h2 class="bigintro">Sign In</h2>
								<div class="divide-40"></div>
								<form id="loginForm" role="form" >
								  <div class="form-group">
									<label for="loginUsername">Username</label>
									<i class="fa fa-user"></i>
									<input name="user" type="text" class="form-control" id="loginUsername" >
								  </div>
								  <div class="form-group"> 
									<label for="loginPassword">Password</label>
									<i class="fa fa-lock"></i>
									<input name="pwd" type="password" class="form-control" id="loginPassword" >
								  </div>
								  <div class="form-actions">
									<label class="checkbox"> <input type="checkbox" class="uniform" value=""> Remember me</label>
									<button type="submit" class="btn btn-danger">Submit</button>
								  </div>
								</form>
								<div class="login-helpers">
									<a href="#" onclick="swapScreen('forgot');return false;">Forgot Password?</a> <br>
									Don't have an account with us? <a href="#" onclick="swapScreen('register');return false;">Register
										now!</a>
								</div>
							</div>
						</div>
					</div>
				</div>
			</section>
			<!--/LOGIN -->
			<!-- REGISTER -->
			<section id="register">
				<div class="container">
					<div class="row">
						<div class="col-md-4 col-md-offset-4">
							<div class="login-box-plain">
								<h2 class="bigintro">Register</h2>
								<div class="divide-40"></div>
								<form id="registerForm" role="form" action="user/auth/register" method="POST">
								  <div class="form-group">
									<label for="registerFullName">Full Name</label>
									<i class="fa fa-font"></i>
									<input type="text" name="fullName" class="form-control" id="registerFullName" >
								  </div>
								  <div class="form-group">
									<label for="registerUsername">Username</label>
									<i class="fa fa-user"></i>
									<input type="text" name="username" class="form-control" id="registerUsername" >
								  </div>
								  <div class="form-group">
									<label for="registerEmail">Email address</label>
									<i class="fa fa-envelope"></i>
									<input type="email" name="email" class="form-control" id="registerEmail" >
								  </div>
								  <div class="form-group"> 
									<label for="registerPassword">Password</label>
									<i class="fa fa-lock"></i>
									<input type="password" name="password" class="form-control" id="registerPassword" >
								  </div>
								  <!--
								  <div class="form-group"> 
									<label for="password2">Repeat Password</label>
									<i class="fa fa-check-square-o"></i>
									<input type="password" name="password" class="form-control" id="password2" >
								  </div>
								  -->
								  <div class="form-actions">
									<label class="checkbox"> <input type="checkbox" class="uniform" value=""> I agree to the <a href="#">Terms of Service</a> and <a href="#">Privacy Policy</a></label>
									<button type="submit" class="btn btn-success">Sign Up</button>
								  </div>
								</form>
								<div class="login-helpers">
									<a href="#" onclick="swapScreen('login');return false;"> Back to Login</a> <br>
								</div>
							</div>
						</div>
					</div>
				</div>
			</section>
			<!--/REGISTER -->
			<!-- FORGOT PASSWORD -->
			<section id="forgot">
				<div class="container">
					<div class="row">
						<div class="col-md-4 col-md-offset-4">
							<div class="login-box-plain">
								<h2 class="bigintro">Reset Password</h2>
								<div class="divide-40"></div>
								<form role="form">
								  <div class="form-group">
									<label for="exampleInputEmail1">Enter your Email address</label>
									<i class="fa fa-envelope"></i>
									<input type="email" class="form-control" id="exampleInputEmail1" >
								  </div>
								  <div class="form-actions">
									<button type="submit" class="btn btn-info">Send Me Reset Instructions</button>
								  </div>
								</form>
								<div class="login-helpers">
									<a href="#" onclick="swapScreen('login');return false;">Back to Login</a> <br>
								</div>
							</div>
						</div>
					</div>
				</div>
			</section>
			<!-- FORGOT PASSWORD -->
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
	
	
	<!-- UNIFORM -->
	<script type="text/javascript" src="js/uniform/jquery.uniform.min.js"></script>
	<!-- CUSTOM SCRIPT -->
	<script src="js/script.js"></script>
	<script>
		jQuery(document).ready(function() {		
			App.setPage("login");  //Set current page
			App.init(); //Initialise plugins and elements
		});
	</script>
	<script type="text/javascript">
		function swapScreen(id) {
			jQuery('.visible').removeClass('visible animated fadeInUp');
			jQuery('#'+id).addClass('visible animated fadeInUp');
		}
	</script>

	<script type="text/javascript">
	$("#loginForm").submit(function(event) {
		event.preventDefault();
		$.ajax({
		    url : "user/auth/login",
		    type: "POST",
		    dataType : "text",
		    data : {
		    	username : $('#loginUsername').val().trim(),
		    	password : $('#loginPassword').val().trim()

		    },
		    success: function(data, textStatus, jqXHR)
		    {
		        if(data==="success")
		        	window.location.replace("index.jsp");
		        else
		        	alert("Incorrect username/password");
		    },
		    error: function (jqXHR, textStatus, errorThrown)
		    {
		 		alert(errorThrown);
		    }
		});
	});

	$("#registerForm").submit(function(event) {
		event.preventDefault();
		$.ajax({
		    url : "user/auth/register",
		    type: "POST",
		    dataType : "text",
		    data : {
		    	fullName : $('#registerFullName').val().trim(),
		    	email : $('#registerEmail').val().trim(),
		    	username : $('#registerUsername').val().trim(),
		    	password : $('#registerPassword').val().trim()

		    },
		    success: function(data, textStatus, jqXHR)
		    {
		        window.location.replace("index.jsp");
		    },
		    error: function (jqXHR, textStatus, errorThrown)
		    {
		 		alert(errorThrown);
		    }
		});
	});
	</script>
	<!-- /JAVASCRIPTS -->
</body>
</html>