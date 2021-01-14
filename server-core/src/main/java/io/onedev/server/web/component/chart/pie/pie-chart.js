onedev.server.pieChart = {
	onDomReady: function(containerId, pieSlices, selectionCallback) {
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
					value: pieSlices[i].value
				})
				pieSelections[pieSlices[i].name] = pieSlices[i].selected;
			}			
			
			chart.setOption({
				color: chartColors,
	            tooltip: {  
	                formatter: "{b}"  
	            }, 
				legend: {
					show: true,
					selected: pieSelections,
					top: 0,
					x: "center",
					formatter: function(name) {
						var value = 0;
			            for (var i = 0; i < chartData.length; i++) {
			              	if (chartData[i].name == name) 
			              		value = chartData[i].value;
			            }
			            return `${name}  ${value}`;
			        },
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
