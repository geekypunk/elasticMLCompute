var SVMCharts = function () {

    return {
        displayChart: function (data) {

            if (!jQuery.plot) {
                return;
            }
			/*
			function multiply100(data){
				var masterArray = [];
				var dataPoint;
				
				$.each(data, function( index, value ) {
					dataPoint = [];
					$.each(value, function( index, value2 ) {
						dataPoint.push()
					}
				}
			
			}*/
			data = JSON.parse(data);
			var allSeries=[];
			var seriesObj;
			$.each(data, function( index, value ) {
				seriesObj={}
				seriesObj.label = "C"+index;
				seriesObj.data = value;
				allSeries.push(seriesObj);
			});
           
            	
            /* Interactive Chart */
            function chart2() {
                
                var plot = $.plot($("#chart_2"), allSeries, {
                        series: {
                            lines: {
                                show: true,
                                lineWidth: 1,
                                fill: true,
                                fillColor: {
                                    colors: [{
                                            opacity: 0.05
                                        }, {
                                            opacity: 0.01
                                        }
                                    ]
                                }
                            },
                            points: {
                                show: true
                            },
                            shadowSize: 2
                        },
                        grid: {
                            hoverable: true,
                            clickable: true,
                            tickColor: "#eee",
                            borderWidth: 0
                        },
                        colors: ["#DB5E8C", "#F0AD4E", "#5E87B0"],
                        xaxis: {
							ticks:7,
                            min: 0.01,
                            max: 10
                        },
                        yaxis: {
                            ticks:7,
							min: 0.2,
                            max: 1.0
                            
                        }
                    });


                function showTooltip(x, y, contents) {
                    $('<div id="tooltip">' + contents + '</div>').css({
                            position: 'absolute',
                            display: 'none',
                            top: y + 5,
                            left: x + 15,
                            border: '1px solid #333',
                            padding: '4px',
                            color: '#fff',
                            'border-radius': '3px',
                            'background-color': '#333',
                            opacity: 0.80
                        }).appendTo("body").fadeIn(200);
                }

                var previousPoint = null;
                $("#chart_2").bind("plothover", function (event, pos, item) {
                    $("#x").text(pos.x.toFixed(2));
                    $("#y").text(pos.y.toFixed(2));

                    if (item) {
                        if (previousPoint != item.dataIndex) {
                            previousPoint = item.dataIndex;

                            $("#tooltip").remove();
                            var x = (item.datapoint[0]/100).toFixed(5),
                                y = item.datapoint[1].toFixed(5);

                            showTooltip(item.pageX, item.pageY, item.series.label + " of " + x + " = " + y);
                        }
                    } else {
                        $("#tooltip").remove();
                        previousPoint = null;
                    }
                });
            }
   
           
            chart2();
			
        },
    };

}();