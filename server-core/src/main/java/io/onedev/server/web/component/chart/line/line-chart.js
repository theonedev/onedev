onedev.server.lineChart = {
	onDomReady: function(containerId, lineSeries, yAxisValueFormatter, darkMode) {
		var $chart = $("#" + containerId + ">.line-chart");
		
		if (lineSeries) {
			var chart = echarts.init($chart[0]);
			option = {
				xAxis: {
					type: 'category',
					data: [],
					axisLabel: {
						color: darkMode?'#cdcdde':'#3F4254'
					}
				},
			    yAxis: {
			        type: 'value',
					minInterval: 1,
			        splitLine: {
			            lineStyle: {
			                color: darkMode?'#535370':'#E4E6EF'
			            }
			        },			    	
			    	axisLine: {
			    		show:false
			    	},	
			    	axisLabel: {
						color: darkMode?'#cdcdde':'#3F4254'
			    	},
			    },
				tooltip: {
					trigger: 'axis',
					textStyle: {
						color: darkMode? 'white': '#535370'
					},
					borderColor: darkMode? '#36364F': 'white',
					backgroundColor: darkMode? '#36364F': 'white'
				},
				series: []
			};
			if (lineSeries.lines.length > 1) {
				var lineSelections = {};

				for (var i in lineSeries.lines) 
					lineSelections[lineSeries.lines[i].name] = lineSeries.lines[i].selected;

				option.legend = {
					show: true, 
					selected: lineSelections,
					x: "center",
					data: [],
					textStyle: {
						color: darkMode?'#cdcdde':'#3F4254'
					}
				}
				for (const i in lineSeries.lines) 
					option.legend.data.push(lineSeries.lines[i].name);
				if (lineSeries.seriesName) {
					option.grid = {
						top: 80
					}
					option.title = {
						text: lineSeries.seriesName,
						left: 'center',
						textStyle: {
							color: darkMode?'#cdcdde':'#3F4254'
						}
					}
					option.legend.top = 30;
				} 
			} else if (lineSeries.seriesName) {
				let title = lineSeries.seriesName;
				option.title = {
					text: title,
					left: 'center',
					textStyle: {
						color: darkMode?'#cdcdde':'#3F4254'
					}
				}
			} 
			
			for (const i in lineSeries.lines) {
				option.series.push({
					name: lineSeries.lines[i].name,
					data: [],
					type: 'line',
					smooth: true, 
					animation: false,
					connectNulls: true,
					lineStyle: {
						color: lineSeries.lines[i].color,
						type: lineSeries.lines[i].style?lineSeries.lines[i].style:"solid"
					},
					itemStyle: {
						color: lineSeries.lines[i].color
					}
				});
			}
			for (const i in lineSeries.lines) {
				var stack = lineSeries.lines[i].stack;
				if (stack) {
					option.series[i].stack = stack;
					option.series[i].stackStrategy = "all";
					option.series[i].areaStyle = {};
				}
			}
			for (const i in lineSeries.xAxisValues) {
				let xAxisValue = lineSeries.xAxisValues[i];
				option.xAxis.data.push(xAxisValue);
				for (const j in lineSeries.lines) {
					option.series[j].data.push(lineSeries.lines[j].yAxisValues[i]);
				}
			};
			if (yAxisValueFormatter) {
				option.yAxis.axisLabel.formatter = yAxisValueFormatter;
				option.tooltip.formatter = function(params) {
					if (params.length != 0) {
						let tooltip = params[0].axisValueLabel;
						for(var i in params) {
							var circle = `<span style='margin-right: 8px; border-radius: 50%; display: inline-block; width: 10px; height: 10px; background: ${params[i].color}'></span>`;
							tooltip += "<br>" + circle + params[i].seriesName + ": " + yAxisValueFormatter(params[i].data);
						}
						return tooltip;												
					} 
				}
			}
			if (lineSeries.minYAxisValue) 
				option.yAxis.min = lineSeries.minYAxisValue;
			if (lineSeries.maxYAxisValue) 
				option.yAxis.max = lineSeries.maxYAxisValue;

			chart.setOption(option);
			
			$chart.on("resized", function() {
				setTimeout(function() {
					chart.resize();
				});
			});	
		} else {
			$chart.append("No Data").addClass("d-flex align-items-center h1 text-muted justify-content-center");
		}		
	}
}
