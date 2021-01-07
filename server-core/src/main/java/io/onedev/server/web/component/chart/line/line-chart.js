onedev.server.lineChart = {
	onDomReady: function(containerId, lineSeries, valueFormatter) {
		var $chart = $("#" + containerId + ">.line-chart");
		
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
			    series: [{
			        data: [],
			        type: 'line',
					smooth: true,
					lineStyle: {
						color: '#3699FF'
					},
					itemStyle: {
						color: '#3699FF'
					}
			    }]
			};
			if (lineSeries.name) {
				option.title = {
					text: lineSeries.name,
					left: 'center'
				}
			}
			if (valueFormatter) {
				option.yAxis.axisLabel = {
	        		formatter: valueFormatter
	    		}
			}
			if (lineSeries.minValue) 
				option.yAxis.min = lineSeries.minValue;
			if (lineSeries.maxValue) 
				option.yAxis.max = lineSeries.maxValue;

			for (const key in lineSeries.values) {
				option.xAxis.data.push(key);
				option.series[0].data.push(lineSeries.values[key]);
			};
			chart.setOption(option);
			
			$chart.on("resized", function() {
				chart.resize();
			});	
		} else {
			$chart.append("No Data").addClass("d-flex align-items-center h1 text-muted justify-content-center");
		}		
	}
}
