function handleNewNotifications(newNotificationsFromBackEnd){

		if(newNotificationsFromBackEnd.length>0){
		$('#header-notification').find(".dropdown-menu.notification").find(".dropdown-title").append('<span><i class="fa fa-bell"></i></span>');
		$('#header-notification').find(".dropdown-menu.notification").find(".dropdown-title").find("span").text(newNotificationsFromBackEnd.length+" Notifications");
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
		

		}else{
			$('#header-notification').find(".badge").empty();
			$('#header-notification').find('.dropdown-menu.notification:not(:last)').empty();
		}
		/*If user clicks on bell icon, show new notifications, remove red popup number*/
		$( "#header-notification" ).click(function() {
			$('#header-notification').find(".badge").empty();
			/*
			Sent a GET request to notificationHandler to set the "seen" flag on the notifications
			*/

		});

		function functionAddNotif(status,message,url,desc){
			var notifListDiv =  $('#header-notification').find('.dropdown-menu.notification').find('.footer');
			var labelHTML;
			if(status ===1){
				labelHTML = '<li><a href="'+url+'"><span class="label label-success"><i class="fa fa-check-square-o"></i></span>';
			}else{
				labelHTML = '<span class="label label-success"><i class="fa fa-exclamation-triangle"></i></span>';	
			}
			var messageHTML='<span class="body"><span class="message">'+message+'.</span>';
			messageHTML+='<span class="time"><i class="fa fa-cloud-upload"></i><span> '+desc+'</span></span></span></a></li>';
			notifListDiv.before(labelHTML+messageHTML);
		}
		function addRedPopup(count){
			var div =  $('#header-notification').find('.fa.fa-bell');
			div.after('<span class="badge">'+count+'</span>');
		}

	}