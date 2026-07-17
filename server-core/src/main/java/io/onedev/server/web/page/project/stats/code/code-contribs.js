onedev.server.codeContribs = {
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
	formatDay: function(day) {
		var localDate = JSJoda.LocalDate.ofEpochDay(day);
		return localDate.year()%100 + "-" + localDate.monthValue() + "-" + localDate.dayOfMonth();
	},
	onDomReady : function(overallContributions, topContributorsDataUrl, userCardCallback, darkMode, translations, fromDay, toDay) {
		var $contribs = $(".code-contribs");
		var $overall = $contribs.find(".overall");
		if (Object.keys(overallContributions).length === 0) {
			$overall.html("<div class='no-data'>" + translations.noData + "</div>");
			return;
		}
		
		var contribDays = [];

		for (var day in overallContributions) 
			contribDays.push(Number(day));
		
		contribDays.sort(function(day1, day2) {
			return day1 - day2;
		});
		
		var days = [];
		
		var lastDay = toDay != null ? toDay : contribDays[contribDays.length-1];
		var currentDay = fromDay != null ? fromDay : contribDays[0];
		while (currentDay <= lastDay) {
			days.push(currentDay);
			currentDay++;
		}
		
		var fromDay = days[0];
		var toDay = days[days.length-1];

		var $topContributors = $contribs.find(".top-contributors");

		var overallChart = echarts.init($overall[0]);
		$overall.data("chart", overallChart);
		
		var overallXAxisData = [];
		for (var i=0; i<days.length; i++) {
			overallXAxisData.push(onedev.server.codeContribs.formatDay(days[i]));
		}
		
		function getOverallSeriesData() {
			var data = [];
			for (var i=0; i<days.length; i++) {
				var contribution = overallContributions[days[i]];
				if (contribution) 
					data.push(contribution);
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
				axisLabel: {
					color: darkMode?'#cdcdde':'#3F4254'
				}
			},
			yAxis: {
				minInterval: 1,
				splitLine: {
					lineStyle: {
						color: darkMode?'#535370':'#E4E6EF'
					}
				},			    	
				axisLine: {
					show: false
				},	
				axisLabel: {
					formatter: function(value, index) {
						var result = onedev.server.codeContribs.formatYAxisLabel(value, index, useKiloForOverallChart);
						useKiloForOverallChart = result.useKilo;
						return result.value;
					},
					color: darkMode?'#cdcdde':'#3F4254'
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
					color: darkMode?'rgba(55, 60, 63, 0.5)':'rgba(225, 240, 255, 0.5)',
					borderColor: darkMode?'rgba(54, 153, 255, 0.8)':'rgba(54, 153, 255, 0.8)'
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
			$topContributors.trigger("update");
		});
		
		function updateTopContributors() {
			$topContributors.empty().append("<div class='loading'></div>");
			setTimeout(function() {
				$.ajax({
					url: topContributorsDataUrl + "&from=" + fromDay + "&to=" + toDay,
					cache: false, 
					beforeSend: function(xhr) {
						xhr.setRequestHeader('Wicket-Ajax', 'true');
						xhr.setRequestHeader('Wicket-Ajax-BaseURL', Wicket.Ajax.baseUrl || '.');
					},
					success: function(topContributors) {
						var xAxisData = [];
						for (var i=0; i<days.length; i++) {
							if (days[i] >= fromDay && days[i] <= toDay) 
								xAxisData.push(onedev.server.codeContribs.formatDay(days[i]));
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
							function createAuthorLink() {
								var $link = $("<a class='user'></a>");
								if (contributor.authorProfileUrl)
									$link.attr("href", contributor.authorProfileUrl);
								else
									$link.css("cursor", "default");	
								return $link;
							}
							var $contrib = $("<div class='contrib border rounded p-3 mb-5'></div>");
							$container.append($contrib);
							var $head = $("<div class='head d-flex align-items-center'></div>");
							$contrib.append($head);
							var $left = $("<div class='mr-3 d-flex align-items-center'></div>");
							$head.append($left);
							var $avatarLink = createAuthorLink();
							$avatarLink.addClass("mr-3");
							$avatarLink.append("<img class='avatar' src='" + contributor.authorAvatarUrl + "'></img>");
							$left.append($avatarLink);
							
							var $userInfo = $("<div></div>");
							$left.append($userInfo);
							var $nameDiv = $("<div class='font-weight-bold'></div>");
							$nameLink = createAuthorLink();
							$nameLink.append(contributor.authorName);
							$nameDiv.append($nameLink);
							$userInfo.append($nameDiv);

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

							var $totalContribution = $("<div class='total-contribution font-size-sm'></div>");
							$userInfo.append($totalContribution);
							$totalContribution.append("<a href='" + contributor.commitsUrl + "' class='commits'>" + contributor.totalCommits + " " + translations.commits + "</a>");
							$head.append("<div class='ml-auto font-size-h6 font-weight-bold'>#" + (index + 1) + "</div>");
							
							var $body = $("<div class='body chart'></div>");
							$contrib.append($body);
							
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
									boundaryGap: false,
									axisLabel: {
										color: darkMode?'#cdcdde':'#3F4254'
									}
								},
								yAxis: {
									minInterval: 1,
									axisLine: {
										show: false
									},	
									splitLine: {
										lineStyle: {
											color: darkMode?'#535370':'#E4E6EF'
										}
									},			    	
									boundaryGap: [0, (maxValue-maxValueOfContributor)/maxValueOfContributor],
									axisLabel: {
										formatter: function(value, index) {
											var result = onedev.server.codeContribs.formatYAxisLabel(value, index, useKilo);
											useKilo = result.useKilo;
											return result.value;
										},
										color: darkMode?'#cdcdde':'#3F4254'
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
		
		$topContributors.doneEvents("update", function() {
			updateTopContributors();
		});
		
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
}
