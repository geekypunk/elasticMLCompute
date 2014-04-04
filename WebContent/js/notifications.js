
function handleNewNotifications(newNotificationsFromBackEnd){
	
	if(newNotificationsFromBackEnd.length>0){
		$('#header-notification').find('.dropdown-menu.notification').empty();
		var notifDiv = $('#header-notification').find(".dropdown-menu.notification").find(".dropdown-title");
		notifDiv.append('<span><i class="fa fa-bell"></i></span>');
		notifDiv.find("span").text(newNotificationsFromBackEnd.length+" Notifications");
		//$('#header-notification').find(".badge").text(newNotificationsFromBackEnd);
		addRedPopup(newNotificationsFromBackEnd.length);
		$.each(newNotificationsFromBackEnd, function( index, value ) {
			if(value.reportUri === "upload"){
				functionAddNotif(1,value.taskName,value.reportUri,"Uploaded!");
			}
			else{
				functionAddNotif(1,value.taskName,value.reportUri,"Done!");
			}
		});
	

	}/*else{
		$('#header-notification').find(".badge").empty();
		$('#header-notification').find('.dropdown-menu.notification').empty();
	}*/
	/*If user clicks on bell icon, show new notifications, remove red popup number*/
	$( "#header-notification" ).click(function() {
		$('#header-notification').find(".badge").empty();
		
		/*
		Sent a GET request to notificationHandler to set the "seen" flag on the notifications
		*/

	});

	function functionAddNotif(status,message,url,desc){
		var notifListDiv =  $('#header-notification').find('.dropdown-menu.notification');
		var labelHTML;
		if(status ===1){
			labelHTML = '<li><a href="'+url+'"><span class="label label-success"><i class="fa fa-check-square-o"></i></span>';
		}else{
			labelHTML = '<span class="label label-success"><i class="fa fa-exclamation-triangle"></i></span>';	
		}
		var messageHTML='<span class="body"><span class="message">'+message+'.</span>';
		messageHTML+='<span class="time"><i class="fa fa-cloud-upload"></i><span> '+desc+'</span></span></span></a></li>';
		notifListDiv.append(labelHTML+messageHTML);
	}
	function addRedPopup(count){
		var div =  $('#header-notification').find('.fa.fa-bell');
		div.after('<span class="badge">'+count+'</span>');
	}

}


	function pollForTasks(){
		$.ajax({
		    url : "ui/notifications/getFinishedTasks",
		    type: "GET",
		    dataType : "json",
		    data : {
		    	
		    },
		    success: function(data, textStatus, jqXHR)
		    {
		    	handleNewNotifications(data);
		    	setTimeout(
	                pollForTasks, /* Request next message */
	                5000 /* ..after 5 seconds */
	            );
		    },
		    error: function (jqXHR, textStatus, errorThrown)
		    {
		 			console.log(errorThrown);
		    }
		});
	}
	$(document).ready(function(){
        pollForTasks(); /* Start the inital request */
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
