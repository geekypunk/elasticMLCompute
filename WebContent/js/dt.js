
		$(function(){
			
			$.getJSON("http://localhost:8080/elasticMLCompute/ml/dTree/getTrainingDataSets",{id: $(this).val(), ajax: 'true'}, function(j){
		 		var $subType = $("#e1");
				$subType.empty();
				$.each(j, function () {
					$subType.append($('<option></option>').attr("value", this.optionValue).text(this.optionDisplay));
				});
			});
			$.getJSON("http://localhost:8080/elasticMLCompute/ml/dTree/getTestDataSets",{id: $(this).val(), ajax: 'true'}, function(j){
		 		var $subType = $("#e2");
				$subType.empty();
				$.each(j, function () {
					$subType.append($('<option></option>').attr("value", this.optionValue).text(this.optionDisplay));
				});
			});
			
		
		});
		
		$("#btn-chart").click(function() {
			$('#dtChart').show();
			var a=$(this);
			a.button("loading");
			setTimeout(function() { 
				$.ajax({
					type: "GET",
					dataType: "text",
					url: "http://localhost:8080/elasticMLCompute/ml/dTree/getReport/groups.train-groups.test.chart",
					success: function (d) {
						a.button("complete");
						SVMCharts.displayChart(d);
					}
				})
				},500)
		});
		
		
		$("#dtForm button").click(function(event) {

			  /* stop form from submitting normally */
			
			event.preventDefault();
			
			var a=$('#btn-load-complete');
			a.button("loading");
			var myUrl = "http://localhost:8080/elasticMLCompute/ml/dTree/runDistributedService"; 
			var trainingDataset = $('#e1').val();
			alert("Hello");
			if(trainingDataset.trim().length > 0 && trainingDataset.trim().indexOf(".train")!=-1){	
				$.ajax({
					type: "POST",
					data:{ 
						trainingDataset: trainingDataset, 
						testDataset: $('#e2').val() 
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