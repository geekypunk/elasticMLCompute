		$(function(){
			
			$.getJSON("ml/kernel/getTrainingDataSets",{id: $(this).val(), ajax: 'true'}, function(j){
		 		var $subType = $("#e1");
				$subType.empty();
				$.each(j, function () {
					$subType.append($('<option></option>').attr("value", this.optionValue).text(this.optionDisplay));
				});
			});
			$.getJSON("ml/kernel/getTestDataSets",{id: $(this).val(), ajax: 'true'}, function(j){
		 		var $subType = $("#e2");
				$subType.empty();
				$.each(j, function () {
					$subType.append($('<option></option>').attr("value", this.optionValue).text(this.optionDisplay));
				});
			});
			
		
		});
		
		$("#btn-chart").click(function() {
			$('#kernelChart').show();
			var a=$(this);
			a.button("loading");
			setTimeout(function() { 
				$.ajax({
					type: "GET",
					dataType: "text",
					url: "ml/kernel/getReport/SVM.train-SVM.test.chart",
					success: function (d) {
						a.button("complete");
						SVMCharts.displayChart(d);
					}
				})
				},500)
		});
		
		$("#e3").change(function(){
			var str = "";
		    $( "#e3 option:selected" ).each(function() {
		      str += $( this ).text();
		    });
		    var label;
		    if(str == "Polynomial Kernel"){
			    label = "<label class=\"col-md-2 control-label\" for=\"e3\">Parameter d<span class=\"required\"></span></label>";
			    label += "<div class=\"col-md-10\"><input type=\"text\" id=\"e4\" class=\"col-md-12\"><div>";
			}else if(str == "Radial Basis Function"){
				label = "<label class=\"col-md-2 control-label\" for=\"e3\">Parameter gamma<span class=\"required\"></span></label>";
			    label += "<div class=\"col-md-10\"><input type=\"text\" id=\"e4\" class=\"col-md-12\"><div>";
			}else if(str == "Sigmoid Function"){
				label = "<label class=\"col-md-2 control-label\" for=\"e3\">Parameter s<span class=\"required\"></span></label>";
			    label += "<div class=\"col-md-10\"><input type=\"text\" id=\"e4\" class=\"col-md-12\"><div>";
			}else{
				label = "";
			}
//		    alert(label);
		    
		    $("#kernelParam").html(label);
		});
		
		
		$("#kernelForm button").click(function(event) {

			  /* stop form from submitting normally */
			
			event.preventDefault();
			
			var a=$('#btn-load-complete');
			a.button("loading");
			var kernelType = $('#e3').val();
			var kernelValue = $('#e4').val();
			var myUrl = "ml/kernel/runDistributedService"; 
			var trainingDataset = $('#e1').val();
			if(trainingDataset.trim().length > 0 && trainingDataset.trim().indexOf(".train")!=-1){	
				$.ajax({
					type: "POST",
					data:{ 
						trainingDataset: trainingDataset, 
						testDataset: $('#e2').val(),
						kernelType : kernelType,
						kernelValue : kernelValue
					},
					dataType: "text",
					url: myUrl,
					success: function (d) {
						a.button("complete");
					},
					error: function (d) {
		
					},
					complete: function (d) {
						$('#btn-load-complete').button("complete");
					}
				});
			}else{
				alert("Choose a training dataset!");
				$('#btn-load-complete').button("reset");
			}
		
		});
		
		
		