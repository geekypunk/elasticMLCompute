Elastic Machine Learning Compute Engine
================
This project was done as a part of CS5412, cloud computing course at Cornell University

Why?
----------------
To enable users to analyze their data using standard Machine Learning algorithms out of the box. So, instead of a user 
configuring  an  Amazon  EC2  instance  with  his  application  code  and  running  his  algorithm,  we  will  provide  this 
solution through a simple and configurable dashboard. The user has to simply select the ML algorithm he wants to run 
through the dashboard, the data on which he wants to run the algorithm and the parameters (if any). The user does not 
have to write application code from the scratch.

What?
--------------------
Construct  an  Elastic  Cloud  model  which  will  provide  “Compute  as  a  Service”  (CaaS)  as  a  web-  service.  For  the 
purpose of this project, we intend to provide some standard machine learning algorithms as the compute services. The 
user can upload data to our servers or give the location (AWS S3 url). The users will also be able to pipe the output of 
one  algorithm  to  another.  This  “cloud  model”  built  will  ensure  scalability,  availability,  reliability,  fault-tolerance, 
elasticity and security
###Features
1. A variety of machine learning algorithms to choose from via a Dashboard.
2. Custom datasources like AWS S3 or the user can upload his data to our servers.
3. User can generate reports and store them. User will be able to export his results as CSV or JSON or even 
JPEG images of charts.
4. The user interface delivered both as a web application and as an Android app.
5. The system  auto scales based on load .


Support
------------------
For more details on the project, look into the support folder
