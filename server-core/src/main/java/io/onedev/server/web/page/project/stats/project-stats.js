onedev.server.stats = {
	formatYAxisLabel: function(value, index, useKilo) {
		if (index == 0)
			useKilo = false;
		else if (index == 1 && value >= 1000)
			useKilo = true;
		return {
			useKilo: useKilo,
			value: useKilo? (value/1000).toFixed(1) + " k": value
		}
	},
	
	contribs: {
		onDomReady : function(overallContributions, topContributorsDataUrl, userCardCallback) {
			var $contribs = $(".project-contribs");
			var $overall = $contribs.find(".overall");
			if (Object.keys(overallContributions).length === 0) {
				$overall.html("<div class='no-data'>No data</div>");
				return;
			}
			
			var contribDays = [];

			for (var day in overallContributions) 
				contribDays.push(day);
			
			contribDays.sort(function(day1, day2) {
				return day1 - day2;
			});
			
			var days = [];
			
			var lastDay = contribDays[contribDays.length-1];
			var currentDay = contribDays[0];
			while (currentDay <= lastDay) {
				days.push(currentDay);
				var currentDayObj = onedev.server.day.fromValue(currentDay);
				currentDay = onedev.server.day.toValue(onedev.server.day.plus(currentDayObj, 1));
			}
			
			var fromDay = days[0];
			var toDay = days[days.length-1];
			
			function updateDateRange() {
				$contribs.find(".date-range").text(
						onedev.server.day.format(onedev.server.day.fromValue(fromDay)) 
						+ " ~ " 
						+ onedev.server.day.format(onedev.server.day.fromValue(toDay)));
			}

			var $contribType = $contribs.find(".contrib-type");
			var $topContributors = $contribs.find(".top-contributors");

			var overallChart = echarts.init($overall[0]);
			$overall.data("chart", overallChart);
			
			var overallXAxisData = [];
			for (var i=0; i<days.length; i++) {
				overallXAxisData.push(onedev.server.day.format(onedev.server.day.fromValue(days[i])));
			}
			
			function getOverallSeriesData() {
				var data = [];
				for (var i=0; i<days.length; i++) {
					var contribution = overallContributions[days[i]];
					if (contribution) 
						data.push(contribution[1][$contribType[0].selectedIndex]);
					else 
						data.push(0);
				}
				return data;
			}
			
			var grid = {
				left: '80px',
				right: '40px',
				top: '40px',
				bottom: '40px'
			};
			
			var useKiloForOverallChart;
			
			var overallChartOption = {
				grid: grid,
				xAxis: {
					type: 'category',
					data: overallXAxisData,
					boundaryGap: false,
				},
				yAxis: {
					minInterval: 1,
			    	axisLine: {
			    		show:false
			    	},	
			    	axisLabel: {
			    		formatter: function(value, index) {
			    			var result = onedev.server.stats.formatYAxisLabel(value, index, useKiloForOverallChart);
			    			useKiloForOverallChart = result.useKilo;
			    			return result.value;
			    		}
			    	},
				},
		        toolbox: {
		        	show: false
		        },
		        brush: {
		            xAxisIndex: 'all',
		            brushLink: 'all',
		            outOfBrush: {
		                colorAlpha: 0.1
		            },
					brushStyle: {
						color: 'rgba(225, 240, 255, 0.5)',
						borderColor: 'rgba(54, 153, 255, 0.8)'
					}
		        },
		        series: [ {
					type: 'line',
					symbol: 'none',
					smooth: true,
					color: "#1BC5BD",
					animation: false,
					data: getOverallSeriesData(),
					areaStyle: {
						color: "#1BC5BD"
					}
				} ]
			};
			overallChart.setOption(overallChartOption);
			overallChart.dispatchAction({
	            type: 'takeGlobalCursor',
	            key: 'brush',
	            brushOption: {
	                brushType: 'lineX',
	                brushMode: 'single'
	            }
	        });		
			overallChart.on("brush", function(params) {
				if (params.areas[0]) {
					var selection = params.areas[0].coordRange;
					function normalizeIndex(index) {
						if (index < 0)
							return 0;
						else if (index > days.length -1)
							return days.length-1;
						else
							return index;
					}
					fromDay = days[normalizeIndex(selection[0])];
					toDay = days[normalizeIndex(selection[1])];
				} else {
					fromDay = days[0];
					toDay = days[days.length-1];
				}
				updateDateRange();
				$topContributors.trigger("update");
			});
			
			function updateTopContributors() {
				$topContributors.empty().append("<div class='loading'><img src='/img/ajax-indicator-big.gif'></img></div>");
				setTimeout(function() {
					$.ajax({
						url: topContributorsDataUrl + "&type=" + $contribType.val().toUpperCase() + "&from=" + fromDay + "&to=" + toDay,
						cache: false, 
						beforeSend: function(xhr) {
							xhr.setRequestHeader('Wicket-Ajax', 'true');
							xhr.setRequestHeader('Wicket-Ajax-BaseURL', Wicket.Ajax.baseUrl || '.');
						},
						success: function(topContributors) {
							var xAxisData = [];
							for (var i=0; i<days.length; i++) {
								if (days[i] >= fromDay && days[i] <= toDay) 
									xAxisData.push(onedev.server.day.format(onedev.server.day.fromValue(days[i])));
							}
							
							var maxValue = 0;
							for (var i=0; i<topContributors.length; i++) {
								for (var day in topContributors[i].dailyContributions) {
									var value = topContributors[i].dailyContributions[day];
									if (maxValue < value)
										maxValue = value;
								}
							}
							
							function renderContributor($container, contributor, index) {
								var $contrib = $("<div class='card contrib mb-5'><div class='card-body'></div></div>");
								$container.append($contrib);
								var $head = $("<div class='head d-flex align-items-center'></div>");
								$contrib.children().append($head);
								var $left = $("<div class='mr-3 d-flex align-items-center'></div>");
								$head.append($left);
								$left.append("<a class='user mr-3'><img class='avatar' src='" + contributor.authorAvatarUrl + "'></img></a>");

								var alignment = {targetX: 0, targetY: 0, x: 0, y: 100, offset: 8};
								$left.find("a.user").hover(function() {
									var $card = $("<div id='user-card' class='floating'></div>");
									$card.hide();
									$card.data("trigger", this);
									$card.data("alignment", alignment);
									$("body").append($card);
									userCardCallback(contributor.authorName, contributor.authorEmailAddress);
									return $card;
								}, alignment);
								
								var $userInfo = $("<div></div>");
								$left.append($userInfo);
								
								$userInfo.append("<div class='font-weight-bold'>" + contributor.authorName + "</div>");
								var $totalContribution = $("<div class='total-contribution font-size-sm'></div>");
								$userInfo.append($totalContribution);
								$totalContribution.append("<span class='commits mr-2'>" + contributor.totalCommits + " commits</span>");
								$totalContribution.append("<span class='additions mr-2'>" + contributor.totalAdditions + " ++</span>");
								$totalContribution.append("<span class='deletions'>" + contributor.totalDeletions + " --</span>");
								$head.append("<div class='ml-auto font-size-h6 font-weight-bold'>#" + (index + 1) + "</div>");
								
								var $body = $("<div class='body chart'></div>");
								$contrib.children().append($body);
								
								var chart = echarts.init($body[0]);
								$body.data("chart", chart);
								
								var seriesData = [];
								var maxValueOfContributor = 0;
								for (var i=0; i<days.length; i++) {
									if (days[i] >= fromDay && days[i] <= toDay) {
										var contribution = contributor.dailyContributions[days[i]];
										if (contribution) {
											seriesData.push(contribution);
											if (maxValueOfContributor < contribution)
												maxValueOfContributor = contribution;
										} else {
											seriesData.push(0);
										}
									}
								}
								
								var useKilo;
								var option = {
									grid: grid,
									xAxis: {
										type: 'category',
										data: xAxisData,
										boundaryGap: false
									},
									yAxis: {
										minInterval: 1,
								    	axisLine: {
								    		show:false
								    	},	
								    	boundaryGap: [0, (maxValue-maxValueOfContributor)/maxValueOfContributor],
								    	axisLabel: {
								    		formatter: function(value, index) {
								    			var result = onedev.server.stats.formatYAxisLabel(value, index, useKilo);
								    			useKilo = result.useKilo;
								    			return result.value;
								    		}
								    	},
									},
							        series: [ {
										type: 'line',
										color: "#FFA800",
										symbol: 'none',
										smooth: true,
										sampling: 'average',
										animation: false,
										areaStyle: {
											color: "#FFA800"
										},
										data: seriesData,
									} ]
								};
								chart.setOption(option);							
							}
							
							$topContributors.empty();
							var rows = Math.trunc(topContributors.length/2);
							for (var i=0; i<rows; i++) {
								var $row = $("<div class='row'><div class='col-xl-6 left'></div><div class='col-xl-6 right'></div>");
								$topContributors.append($row);
								renderContributor($row.children(".left"), topContributors[i*2], i*2);
								renderContributor($row.children(".right"), topContributors[i*2+1], i*2+1);
							}
							if (rows*2 < topContributors.length) {
								var $row = $("<div class='row'><div class='col-md-6 left'></div></div");
								$topContributors.append($row);
								renderContributor($row.children(".left"), topContributors[rows*2], rows*2);
							}
						},
					});
				}, 10);
			}
			
			$contribType.change(function() {
				overallChartOption.series[0].data = getOverallSeriesData();
				overallChart.setOption(overallChartOption);
			    updateTopContributors();
			});

			$topContributors.doneEvents("update", function() {
				updateTopContributors();
			});
			
			updateDateRange();
			updateTopContributors();
			
			$(window).resize(function() {
				$contribs.find(".chart").each(function() {
					$(this).data("chart").resize();
				});
			});
		},
		onUserCardAvailable: function() {
			var $userCard = $("#user-card");
			$userCard.empty().append($(".user-card-content").children()).show();
			$userCard.align({placement: $userCard.data("alignment"), target: {element: $userCard.data("trigger")}});
		}
	},
	
	sourceLines: {
		onDomReady: function(lineIncrements, defaultBranch) {
			var numOfTopLanguages = 10;
			
			var $chart = $(".source-lines>div>.chart");
			if (lineIncrements.length == 0) {
				$chart.append("<div class='no-data'>No data</div>");
				return;
			}
			
			var languageLines = {};
			var incrementDays = [];
			for (var day in lineIncrements) {
				delete lineIncrements[day]["@class"];
				incrementDays.push(day);
				var incrementsOnDay = lineIncrements[day];
				for (var language in incrementsOnDay) {
					var increment = incrementsOnDay[language];
					var dailyLinesByLanguages = languageLines[language];
					if (dailyLinesByLanguages != undefined)
						dailyLinesByLanguages += increment;
					else
						dailyLinesByLanguages = increment;
					languageLines[language] = dailyLinesByLanguages;
				}
			}
			incrementDays.sort(function(day1, day2) {
				return day1 - day2;
			});
			
			var languages = Object.keys(languageLines);
			languages.sort(function(a, b) {
				return languageLines[b] - languageLines[a];
			});
			
			var topLanguages = languages.slice(0, numOfTopLanguages);
			
			var lastDay = incrementDays[incrementDays.length-1];
			var currentDay = incrementDays[0];
			var dailyLinesByLanguages = {};
			for (var i in topLanguages)
				dailyLinesByLanguages[topLanguages[i]] = [];
			var xAxisData = [];
			while (currentDay <= lastDay) {
				var incrementsOnCurrentDay = lineIncrements[currentDay];
				for (var language in dailyLinesByLanguages) {
					var dailyLines = dailyLinesByLanguages[language];
					var increments;
					if (incrementsOnCurrentDay)
						increments = incrementsOnCurrentDay[language];
					if (increments == undefined)
						increments = 0;
					
					if (dailyLines.length != 0)
						dailyLines.push(dailyLines[dailyLines.length-1] + increments);
					else
						dailyLines.push(increments);
				}
				var currentDayObj = onedev.server.day.fromValue(currentDay);
				xAxisData.push(onedev.server.day.format(currentDayObj));
				currentDay = onedev.server.day.toValue(onedev.server.day.plus(currentDayObj, 1));
			}
			
			var chart = echarts.init($chart[0]);
			
			var useKilo;
			var title;
			if (defaultBranch != null)
				title = "SLOC on " + defaultBranch;
			else
				title = "No default branch";
			var option = {
				title: {
					text: title,
					left: 'center',
					top: 10
				},
				grid: {
					left: '60px',
					right: '40px',
					top: '120px',
					bottom: '40px'
				},
				xAxis: {
					type: 'category',
					data: xAxisData,
					boundaryGap: false
				},
				yAxis: {
					minInterval: 1,
					min: 0,
			    	axisLine: {
			    		show:false
			    	},	
			    	boundaryGap: [0, 0],
			    	axisLabel: {
			    		formatter: function(value, index) {
			    			var result = onedev.server.stats.formatYAxisLabel(value, index, useKilo);
			    			useKilo = result.useKilo;
			    			return result.value;
			    		}
			    	},
				},
				tooltip: {
					trigger: 'axis'
				},
				legend: {
					top: 40,
					data: []
				},
		        series: []
			};

			var colors = ["#53a8fd", "#19A519", "#EEAD08", "#09c112", "#ff4242", "#b00000", "#af29f8", "#ee6c1e", "#ef56cd", "#630596", "#8e4509", "#9b0f7b"];
			var colorIndex = 0;
			for (language in dailyLinesByLanguages) {
				option.series.push({
					name: language,
					type: 'line',
					color: colors[colorIndex],
					symbol: 'none',
					sampling: 'average',
					smooth: true,
					animation: false,
					data: dailyLinesByLanguages[language],
				});
				option.legend.data.push({
					name: language,
				});
				
				if (++colorIndex == colors.length)
					colorIndex = 0;
			}
			chart.setOption(option);	
			
			$chart.on("resized", function() {
				chart.resize();
			});
		}
	}
}