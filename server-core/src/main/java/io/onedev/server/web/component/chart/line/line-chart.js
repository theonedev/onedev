onedev.server.lineChart = {
	onDomReady: function(containerId, lineSeries, valueFormatter) {
		var $chart = $("#" + containerId + ">.line-chart");
		
		console.log(lineSeries);
		
		if (lineSeries) {
			var chart = echarts.init($chart[0]);
			option = {
				xAxis: {
					type: 'category',
					data: []
				},
			    yAxis: {
			        type: 'value',
					minInterval: 1,
			    	axisLine: {
			    		show:false
			    	},	
			    },
				series: []
			};
			if (lineSeries.lineNames.length > 1) {
				option.legend = {
					show: true, 
					x: "center",
					data: []
				}
				for (const i in lineSeries.lineNames) 
					option.legend.data.push(lineSeries.lineNames[i]);
				if (lineSeries.seriesName) {
					option.grid = {
						top: 80
					}
					option.title = {
						text: lineSeries.seriesName,
						left: 'center'
					}
					option.legend.top = 30;
				} 
			} else {
				let title = lineSeries.lineNames[0];
				if (lineSeries.seriesName) 
					title = lineSeries.seriesName + " / " + title;
				option.title = {
					text: title,
					left: 'center'
				}
			} 
			
			for (const i in lineSeries.lineNames) {
				option.series.push({
					name: lineSeries.lineNames[i],
					data: [],
					type: 'line',
					smooth: true, 
					animation: false,
					lineStyle: {
						color: lineSeries.lineColors[i]
					},
					itemStyle: {
						color: lineSeries.lineColors[i]
					}
				});
			}
			for (const key in lineSeries.lineValues) {
				option.xAxis.data.push(key);
				for (const i in lineSeries.lineNames) 
					option.series[i].data.push(lineSeries.lineValues[key][i]);
			};
			if (valueFormatter) {
				option.yAxis.axisLabel = {
	        		formatter: valueFormatter
	    		}
			}
			if (lineSeries.minValue) 
				option.yAxis.min = lineSeries.minValue;
			if (lineSeries.maxValue) 
				option.yAxis.max = lineSeries.maxValue;

			chart.setOption(option);
			
			console.log(option);
			
			$chart.on("resized", function() {
				chart.resize();
			});	
		} else {
			$chart.append("No Data").addClass("d-flex align-items-center h1 text-muted justify-content-center");
		}		
	}
}
