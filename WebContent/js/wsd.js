
		$(function(){
			
			$.getJSON("http://localhost:8080/elasticMLCompute/ml/wsd/getTrainingDataSets",{id: $(this).val(), ajax: 'true'}, function(j){
		 		var $subType = $("#e1");
				$subType.empty();
				$.each(j, function () {
					$subType.append($('<option></option>').attr("value", this.optionValue).text(this.optionDisplay));
				});
			});
			$.getJSON("http://localhost:8080/elasticMLCompute/ml/wsd/getTrainingDataSets",{id: $(this).val(), ajax: 'true'}, function(j){
		 		var $subType = $("#e2");
				$subType.empty();
				$.each(j, function () {
					$subType.append($('<option></option>').attr("value", this.optionValue).text(this.optionDisplay));
				});
			});
			$.getJSON("http://localhost:8080/elasticMLCompute/ml/wsd/getTestDataSets",{id: $(this).val(), ajax: 'true'}, function(j){
		 		var $subType = $("#e3");
				$subType.empty();
				$.each(j, function () {
					$subType.append($('<option></option>').attr("value", this.optionValue).text(this.optionDisplay));
				});
			});
			
		
		})
	
		$("#wsdForm").submit(function(event) {

			  /* stop form from submitting normally */
			event.preventDefault();

			var a=$('#btn-load-complete');
			a.button("loading");
			var trainingDataset = $('#e1').val();
			if(trainingDataset.trim().length > 0 && trainingDataset.trim().indexOf(".train")!=-1){	
				$.ajax({
					type: "POST",
					data:{ 
						trainingDataset: trainingDataset, 
						validationDataset: $('#e2').val() ,
						testDataset: $('#e3').val() 
					},
					dataType: "text",
					url: "ml/wsd/runDistributedService",
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
			
		$("#btn-chart-co").on("click",function(){
			$('#wsdChart').show();
			var a=$(this);
			a.button("loading");
			setTimeout(function() { 
				$.ajax({
					type: "GET",
					dataType: "text",
					url: "http://localhost:8080/elasticMLCompute/ml/wsd/getReport/WSD.train-WSD.test_CO.chart",
					success: function (d) {
						a.button("complete");
						SVMCharts.displayChart(d);
					}
				})
				},500)
		});
		
		$("#btn-chart-cl").on("click",function(){
			$('#wsdChart').show();
			var a=$(this);
			a.button("loading");
			setTimeout(function() { 
				$.ajax({
					type: "GET",
					dataType: "text",
					url: "http://localhost:8080/elasticMLCompute/ml/wsd/getReport/WSD.train-WSD.test_CL.chart",
					success: function (d) {
						a.button("complete");
						SVMCharts.displayChart(d);
					}
				})
				},500)
		});
