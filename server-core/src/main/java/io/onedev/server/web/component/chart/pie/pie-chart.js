onedev.server.pieChart = {
	onDomReady: function(containerId, pieSlices, selectionCallback, darkMode) {
		var $chart = $("#" + containerId + ">.pie-chart");
		if (pieSlices) {
			var chart = echarts.init($chart[0]);
			
			var chartColors = [];
			var chartData = [];
			var pieSelections = {};
			
			for (var i in pieSlices) {
				chartColors.push(pieSlices[i].color);
				chartData.push({
					name: pieSlices[i].name,
					displayName: pieSlices[i].displayName,
					value: pieSlices[i].value
				})
				pieSelections[pieSlices[i].name] = pieSlices[i].selected;
			}			
			
			chart.setOption({
				color: chartColors,
	            tooltip: {  
	                formatter: function(params) {
	                	return params.data.displayName;
	                },
					textStyle: {
						color: darkMode? 'white': '#535370'
					},
					backgroundColor: darkMode? '#36364F': 'white'
	            },
				legend: {
					show: true,
					selected: pieSelections,
					top: 0,
					x: "center",
					formatter: function(name) {
						var value = 0;
						var displayName = "";
			            for (var i = 0; i < chartData.length; i++) {
			              	if (chartData[i].name == name) {
			              		value = chartData[i].value;
			              		displayName = chartData[i].displayName;
			              		break;
			              	}
			            }
			            return `${displayName}  ${value}`;
			        },
					textStyle: {
						color: darkMode?'#cdcdde':'#3F4254'
					}
				},
				series: [{
					type: "pie",
					radius: "70%",
					center: ["50%", "55%"],
					label: {
						normal: {
							position: "inner",
							formatter: '{d}%'
						}
					},
					stillShowZeroSum: false, 
					animation: false,
					data: chartData	
				}]
			});	
			chart.on("legendselectchanged", function(obj) {
				selectionCallback(obj.name);
			});	
			$chart.on("resized", function() {
				chart.resize();
			});	
		} else {
			$chart.append("No Data").addClass("d-flex align-items-center h1 text-muted justify-content-center");
		}		
	}
}
