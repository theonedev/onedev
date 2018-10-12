onedev.server.stats = {
	contribs : {
		onDomReady : function(jsonList, orderBy, fromDay, toDay) {
			var date = [];
			var data = [];
			var base = +new Date(jsonList[0].day.dateTime);
			var oneDay = 24 * 3600 * 1000;
			var firstday = new Date(jsonList[0].day.dateTime);

			date.push([ firstday.getFullYear(), firstday.getMonth() + 1, firstday.getDate() ].join('/'));

			if (orderBy == "COMMITS") {
				data.push(jsonList[0].contribution.commits);
				for (var i = 1; i < jsonList.length; i++) {
					var day = new Date(jsonList[i].day.dateTime);
					var now = new Date(base += oneDay);
					while (day.getTime() != now.getTime()) {
						date.push([ now.getFullYear(), now.getMonth() + 1, now.getDate() ].join('/'));
						data.push(0);
						now = new Date(base += oneDay);

					}
					date.push([ now.getFullYear(), now.getMonth() + 1, now.getDate() ].join('/'));
					data.push(jsonList[i].contribution.commits);
				}
			} else if (orderBy == "ADDITIONS") {
				data.push(jsonList[0].contribution.additions / 1000);
				for (var i = 1; i < jsonList.length; i++) {
					var day = new Date(jsonList[i].day.dateTime);
					var now = new Date(base += oneDay);
					while (day.getTime() != now.getTime()) {
						date.push([ now.getFullYear(), now.getMonth() + 1, now.getDate() ].join('/'));
						data.push(0);
						now = new Date(base += oneDay);

					}
					date.push([ now.getFullYear(), now.getMonth() + 1, now.getDate() ].join('/'));
					data.push(jsonList[i].contribution.additions / 1000);
				}

			} else {
				data.push(jsonList[0].contribution.deletions / 1000);
				for (var i = 1; i < jsonList.length; i++) {
					var day = new Date(jsonList[i].day.dateTime);
					var now = new Date(base += oneDay);
					while (day.getTime() != now.getTime()) {
						date.push([ now.getFullYear(), now.getMonth() + 1, now.getDate() ].join('/'));
						data.push(0);
						now = new Date(base += oneDay);

					}
					date.push([ now.getFullYear(), now.getMonth() + 1, now.getDate() ].join('/'));
					data.push(jsonList[i].contribution.deletions / 1000);
				}
			}

			var showdate = [];
			var showdata = [];
			var firstday,
				lastday;
			if (fromDay == 'null' && toDay == 'null') {
				firstday = 0;
				lastday = date.length - 1;
			} else {
				fromDay = fromDay.replace(/-/g, "/");
				toDay = toDay.replace(/-/g, "/");
				for (var j = 0; j < date.length; j++) {
					if (date[j] == fromDay)
						firstday = j;
					if (date[j] == toDay) {
						lastday = j;
						break;
					}
				}

			}
			showdate = date.slice(firstday, lastday + 1);
			showdata = data.slice(firstday, lastday + 1);
			var windowWidth = $(window).width();
			var myChart = echarts.init(document.getElementById('project-contribs'));
			var optionCommits = {
				tooltip : {
					trigger : 'axis',
					position : function(pt) {
						return [ pt[0], '10%' ];
					}
				},
				title : {
					left : 'center',
					text : 'Overall Contribution',
				},
				grid : {
					left : "2%",
					right : "2%",
					top : 50,
					buttom : "1%"
				},
				toolbox : {
					feature : {
						dataZoom : {
							yAxisIndex : 'none',
							iconStyle : {
								opacity : 0
							}
						},
						restore : {
							title: 'Restore date range',
							iconStyle : {
								textPosition : 'left',
								opacity : 0
							}
						},
					},
					right: 40
				},
				xAxis : {
					type : 'category',
					boundaryGap : false,
					data : showdate,
					triggerEvent : true
				},
				yAxis : {
					axisLine : {
						show : false
					},
					minInterval : 1,
					type : 'value',
					boundaryGap : [ 0, '100%' ],
					triggerEvent : true
				},

				series : [
					{
						name : 'commits',
						type : 'line',
						smooth : true,
						symbol : 'none',
						sampling : 'average',
						itemStyle : {
							normal : {
								label : {
									formatter : function(params) {
										if (params.value > 0) {
											return params.value;
										} else {
											return '';
										}
									}
								},
								color : 'rgba(0,0,0,0)'
							}
						},
						areaStyle : {
							normal : {
								color : new echarts.graphic.LinearGradient(0, 0, 0, 1, [ {
									offset : 0,
									color : 'rgb(143,207,159)'
								}, {
									offset : 1,
									color : 'rgb(143,207,159)'
								} ])
							}
						},
						data : showdata
					}
				]
			};
			var optionAdditions = {
				tooltip : {
					trigger : 'axis',
					formatter : '{b} <br/>{a}: {c}k',
					position : function(pt) {
						return [ pt[0], '10%' ];
					}
				},
				title : {
					left : 'center',
					text : 'Overall Contribution',
				},
				grid : {
					left : "2%",
					right : "2%",
					top : 60,
					buttom : 60
				},
				toolbox : {
					feature : {
						dataZoom : {
							yAxisIndex : 'none',
							iconStyle : {
								opacity : 0
							}
						},
						restore : {
							title: 'Restore date range'
						},
					},
					right: 40
				},
				xAxis : {
					type : 'category',
					boundaryGap : false,
					data : showdate,
					triggerEvent : true
				},
				yAxis : {
					axisLabel : {
						formatter : function(value) {
							return value + 'k'
						}
					},

					axisLine : {
						show : false
					},
					type : 'value',
					max : function(value) {
						return value.max * 1;
					},
					boundaryGap : [ 0, '100%' ],
					triggerEvent : true
				},

				series : [
					{
						name : 'additions',
						type : 'line',
						smooth : true,
						symbol : 'none',
						sampling : 'average',
						itemStyle : {
							normal : {
								label : {
									formatter : function(params) {
										if (params.value > 0) {
											return params.value;
										} else {
											return '';
										}
									}
								},
								color : 'rgba(0,0,0,0)'
							}
						},
						areaStyle : {
							normal : {
								color : new echarts.graphic.LinearGradient(0, 0, 0, 1, [ {
									offset : 0,
									color : 'rgb(143,207,159)'
								}, {
									offset : 1,
									color : 'rgb(143,207,159)'
								} ])
							}
						},
						data : showdata
					}
				]
			};
			var optionDeletions = {
				tooltip : {
					trigger : 'axis',
					formatter : '{b} <br/>{a}: {c}k',
					position : function(pt) {
						return [ pt[0], '10%' ];
					}
				},
				title : {
					left : 'center',
					text : 'Overall Contribution',
				},
				grid : {
					left : "2%",
					right : "2%",
					top : 60,
					buttom : 60
				},
				toolbox : {
					feature : {
						dataZoom : {
							yAxisIndex : 'none',
							iconStyle : {
								opacity : 0
							}
						},
						restore : {
							title: 'Restore date range'
						},
					},
					right: 40
				},
				xAxis : {
					type : 'category',

					boundaryGap : false,
					data : showdate,
					triggerEvent : true
				},
				yAxis : {
					axisLine : {
						show : false
					},
					axisLabel : {
						formatter : function(value) {
							return value + 'k'
						}
					},
					type : 'value',
					max : function(value) {
						return value.max * 1;
					},
					boundaryGap : [ 0, '100%' ],
					triggerEvent : true
				},

				series : [
					{
						name : 'deletions',
						type : 'line',
						smooth : true,
						symbol : 'none',
						sampling : 'average',
						itemStyle : {
							normal : {
								label : {
									formatter : function(params) {
										if (params.value > 0) {
											return params.value;
										} else {
											return '';
										}
									}
								},
								color : 'rgba(0,0,0,0)'
							}
						},
						areaStyle : {
							normal : {
								color : new echarts.graphic.LinearGradient(0, 0, 0, 1, [ {
									offset : 0,
									color : 'rgb(143,207,159)'
								}, {
									offset : 1,
									color : 'rgb(143,207,159)'
								} ])
							}
						},
						data : showdata
					}
				]
			};

			if (orderBy == "COMMITS") myChart.setOption(optionCommits, true);
			else if (orderBy == "ADDITIONS") myChart.setOption(optionAdditions, true);
			else myChart.setOption(optionDeletions, true);

			myChart.on('click', function(params) {
			});
			myChart.dispatchAction({
				type : 'takeGlobalCursor',
				key : 'dataZoomSelect',
				dataZoomSelectActive : true // 允许缩放
			});
			myChart.on('restore', function(params) {
				myFunction(date[0], date[date.length - 1]);
				redraw(date[0], date[date.length - 1]);
			});
			$(window).resize(function() {
				myChart.resize();
			});
			myChart.on('datazoom', function(params) {
				myChart.setOption({
					toolbox : {
						feature : {
							restore : {
								title: 'Restore date range',
								iconStyle : {
									opacity : 1
								}
							}
						},
						right: 40
					},
				});
				var startValue = myChart.getModel().option.dataZoom[0].startValue;
				var endValue = myChart.getModel().option.dataZoom[0].endValue;
				myFunction(showdate[startValue], showdate[endValue]);
			});
		},


		ondrawLinesReady : function(flag, jsonUserDailyContribution, orderBy, gap) {
			function getMonthData(list) {
				var monthdate = [];
				var monthdata = [];
				var MONTH = new Array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
				var lastDay = new Date(list[0].day.dateTime);
				if (orderBy == "COMMITS") {
					var monthCommits = list[0].contribution.commits;
					if (list.length == 1) {
						monthdate.push([ lastDay.getFullYear(), MONTH[lastDay.getMonth()] ].join('/'));
						monthdata.push(monthCommits);
					}
					for (var i = 1; i < list.length; i++) {
						var monthNo = lastDay.getMonth();
						var currentDay = new Date(list[i].day.dateTime);
						var current_monthNo = currentDay.getMonth();
						if (lastDay.getFullYear() == currentDay.getFullYear() && monthNo == current_monthNo) {
							monthCommits += list[i].contribution.commits;
							if (i == list.length - 1) {
								monthdate.push([ lastDay.getFullYear(), MONTH[lastDay.getMonth()] ].join('/'));
								monthdata.push(monthCommits);
								break;
							}
						} else {
							monthdate.push([ lastDay.getFullYear(), MONTH[lastDay.getMonth()] ].join('/'));
							monthdata.push(monthCommits);

							var monthgap = (currentDay.getFullYear() * 12 + currentDay.getMonth()) - (lastDay.getFullYear() * 12 + lastDay.getMonth());
							if (monthgap >= 2) {
								var curr = lastDay;
								curr.setMonth(curr.getMonth() + 1);
								while (curr <= currentDay) {
									if (curr.getFullYear() == currentDay.getFullYear() && curr.getMonth() == currentDay.getMonth()) break;
									var month = curr.getMonth();
									monthdate.push([ curr.getFullYear(), MONTH[month] ].join('/'));
									monthdata.push(0);
									curr.setMonth(month + 1);
								}
							}

							monthCommits = list[i].contribution.commits;
							lastDay = new Date(list[i].day.dateTime);
							if (i == list.length - 1) {
								monthdate.push([ currentDay.getFullYear(), MONTH[currentDay.getMonth()] ].join('/'));
								monthdata.push(monthCommits);
								break;
							}
						}

					}
				} else if (orderBy == "ADDITIONS") {
					var monthAdditions = list[0].contribution.additions;
					if (list.length == 1) {
						monthdate.push([ lastDay.getFullYear(), MONTH[lastDay.getMonth()] ].join('/'));
						monthdata.push(monthAdditions / 1000);
					}
					for (var i = 1; i < list.length; i++) {
						var monthNo = lastDay.getMonth();
						var currentDay = new Date(list[i].day.dateTime);
						var current_monthNo = currentDay.getMonth();
						if (lastDay.getFullYear() == currentDay.getFullYear() && monthNo == current_monthNo) {
							monthAdditions += list[i].contribution.additions;
							if (i == list.length - 1) {
								monthdate.push([ lastDay.getFullYear(), MONTH[lastDay.getMonth()] ].join('/'));
								monthdata.push(monthAdditions / 1000);
								break;
							}
						} else {
							monthdate.push([ lastDay.getFullYear(), MONTH[lastDay.getMonth()] ].join('/'));
							monthdata.push(monthAdditions / 1000);
							var monthgap = (currentDay.getFullYear() * 12 + currentDay.getMonth()) - (lastDay.getFullYear() * 12 + lastDay.getMonth());
							if (monthgap >= 2) {
								var curr = lastDay;
								curr.setMonth(curr.getMonth() + 1);
								while (curr <= currentDay) {
									if (curr.getFullYear() == currentDay.getFullYear() && curr.getMonth() == currentDay.getMonth()) break;
									var month = curr.getMonth();
									monthdate.push([ curr.getFullYear(), MONTH[month] ].join('/'));
									monthdata.push(0);
									curr.setMonth(month + 1);
								}
							}

							monthAdditions = list[i].contribution.additions;
							lastDay = new Date(list[i].day.dateTime);
							if (i == list.length - 1) {
								monthdate.push([ currentDay.getFullYear(), MONTH[currentDay.getMonth()] ].join('/'));
								monthdata.push(monthAdditions / 1000);
								break;
							}
						}

					}
				} else {
					var monthDeletions = list[0].contribution.deletions;
					if (list.length == 1) {
						monthdate.push([ lastDay.getFullYear(), MONTH[lastDay.getMonth()] ].join('/'));
						monthdata.push(monthDeletions / 1000);
					}
					for (var i = 1; i < list.length; i++) {
						var monthNo = lastDay.getMonth();
						var currentDay = new Date(list[i].day.dateTime);
						var current_monthNo = currentDay.getMonth();
						if (lastDay.getFullYear() == currentDay.getFullYear() && monthNo == current_monthNo) {
							monthDeletions += list[i].contribution.deletions;
							if (i == list.length - 1) {
								monthdate.push([ lastDay.getFullYear(), MONTH[lastDay.getMonth()] ].join('/'));
								monthdata.push(monthDeletions / 1000);
								break;
							}
						} else {
							monthdate.push([ lastDay.getFullYear(), MONTH[lastDay.getMonth()] ].join('/'));
							monthdata.push(monthDeletions / 1000);

							var monthgap = (currentDay.getFullYear() * 12 + currentDay.getMonth()) - (lastDay.getFullYear() * 12 + lastDay.getMonth());
							if (monthgap >= 2) {
								var curr = lastDay;
								curr.setMonth(curr.getMonth() + 1);
								while (curr <= currentDay) {
									if (curr.getFullYear() == currentDay.getFullYear() && curr.getMonth() == currentDay.getMonth()) break;
									var month = curr.getMonth();
									monthdate.push([ curr.getFullYear(), MONTH[month] ].join('/'));
									monthdata.push(0);
									curr.setMonth(month + 1);
								}
							}

							monthDeletions = list[i].contribution.deletions;
							lastDay = new Date(list[i].day.dateTime);
							if (i == list.length - 1) {
								monthdate.push([ currentDay.getFullYear(), MONTH[currentDay.getMonth()] ].join('/'));
								monthdata.push(monthDeletions / 1000);
								break;
							}
						}

					}
				}

				monthdata = monthdata.concat(monthdate);
				return monthdata;
			}

			function getDailyData(list) {
				var date = [];
				var data = [];
				var base = +new Date(list[0].day.dateTime);
				var oneDay = 24 * 3600 * 1000;
				var firstday = new Date(list[0].day.dateTime);

				date.push([ firstday.getFullYear(), firstday.getMonth() + 1, firstday.getDate() ].join('/'));
				if (orderBy == "COMMITS") {
					data.push(list[0].contribution.commits);
					for (var i = 1; i < list.length; i++) {
						var day = new Date(list[i].day.dateTime);
						var now = new Date(base += oneDay);
						while (day.getTime() != now.getTime()) {
							date.push([ now.getFullYear(), now.getMonth() + 1, now.getDate() ].join('/'));
							data.push(0);
							now = new Date(base += oneDay);

						}
						date.push([ now.getFullYear(), now.getMonth() + 1, now.getDate() ].join('/'));
						data.push(list[i].contribution.commits);

					}
				} else if (orderBy == "ADDITIONS") {
					data.push(list[0].contribution.additions / 1000);
					for (var i = 1; i < list.length; i++) {
						var day = new Date(list[i].day.dateTime);
						var now = new Date(base += oneDay);
						while (day.getTime() != now.getTime()) {
							date.push([ now.getFullYear(), now.getMonth() + 1, now.getDate() ].join('/'));
							data.push(0);
							now = new Date(base += oneDay);
						}
						date.push([ now.getFullYear(), now.getMonth() + 1, now.getDate() ].join('/'));
						data.push(list[i].contribution.additions / 1000);
					}
				} else {
					data.push(list[0].contribution.deletions / 1000);
					for (var i = 1; i < list.length; i++) {
						var day = new Date(list[i].day.dateTime);
						var now = new Date(base += oneDay);
						while (day.getTime() != now.getTime()) {
							date.push([ now.getFullYear(), now.getMonth() + 1, now.getDate() ].join('/'));
							data.push(0);
							now = new Date(base += oneDay);
						}
						date.push([ now.getFullYear(), now.getMonth() + 1, now.getDate() ].join('/'));
						data.push(list[i].contribution.deletions / 1000);
					}
				}
				data = data.concat(date);
				return data;
			}
			//Get Monthly Data
			var byMonth = getMonthData(jsonUserDailyContribution);
			var monthdate = [];
			var monthdata = [];
			for (var i = 0; i < byMonth.length / 2; i++) {
				monthdata.push(byMonth[i]);
			}
			for (var j = byMonth.length / 2; j < byMonth.length; j++) {
				monthdate.push(byMonth[j]);
			}
			//Get Daily Data
			var byDay = getDailyData(jsonUserDailyContribution);
			var data = [];
			var date = [];
			for (var i = 0; i < byDay.length / 2; i++) {
				data.push(byDay[i]);
			}
			for (var j = byDay.length / 2; j < byDay.length; j++) {
				date.push(byDay[j]);
			}
			var elem = document.getElementsByClassName("project-userline");
			var myChart = echarts.init(elem[flag]);
			var optionCommits = {
				tooltip : {
					trigger : 'axis',
					position : function(pt) {
						return [ pt[0], '10%' ];
					}
				},
				title : {
					left : 'center',
					text : '',
				},
				grid : {
					left : "5%",
					right : "1%",
					top : 30,
					buttom : 0
				},
				toolbox : {
					feature : {
						dataZoom : {
							yAxisIndex : 'none',
							iconStyle : {
								opacity : 0
							}
						},
						restore : {
							title: 'Restore date range',
							iconStyle : {
								opacity : 0
							}
						},
					},
					right: 40
				},
				xAxis : {
					type : 'category',
					boundaryGap : false,
					data : monthdate,
					axisLabel : {
						clickable : true
					}
				},
				yAxis : {
					axisLine : {
						show : false
					},
					type : 'value',
					minInterval : 1,
					max : function(value) {
						return value.max * 1;
					},
					boundaryGap : [ 0, '100%' ]
				},

				series : [
					{
						name : 'commits',
						type : 'line',
						smooth : true,
						symbol : 'none',
						sampling : 'average',
						itemStyle : {
							normal : {
								color : 'rgba(0, 0, 0,0)'
							}
						},
						areaStyle : {
							normal : {
								color : new echarts.graphic.LinearGradient(0, 0, 0, 1, [ {
									offset : 0,
									color : 'rgb(251, 133, 50)'
								}, {
									offset : 1,
									color : 'rgb(251, 133, 50)'
								} ])
							}
						},
						data : monthdata
					}
				]
			};
			var optionCommitsDaily = {
				tooltip : {
					trigger : 'axis',
					position : function(pt) {
						return [ pt[0], '10%' ];
					}
				},
				title : {
					left : 'center',
					text : '',
				},
				grid : {
					left : "5%",
					right : "1%",
					top : 30,
					buttom : 0
				},
				toolbox : {
					feature : {
						dataZoom : {
							yAxisIndex : 'none',
							iconStyle : {
								opacity : 0
							}
						},
						restore : {
							title: 'Restore date range',
							iconStyle : {
								opacity : 0
							}
						},
					},
					right: 40
				},
				xAxis : {
					type : 'category',
					boundaryGap : false,
					data : date,
					axisLabel : {
						clickable : true
					}
				},
				yAxis : {
					axisLine : {
						show : false
					},
					type : 'value',
					minInterval : 1,
					max : function(value) {
						return value.max * 1;
					},
					boundaryGap : [ 0, '100%' ]
				},

				series : [
					{
						name : 'commits',
						type : 'line',
						smooth : true,
						symbol : 'none',
						sampling : 'average',
						itemStyle : {
							normal : {
								color : 'rgba(0, 0, 0,0)'
							}
						},
						areaStyle : {
							normal : {
								color : new echarts.graphic.LinearGradient(0, 0, 0, 1, [ {
									offset : 0,
									color : 'rgb(251, 133, 50)'
								}, {
									offset : 1,
									color : 'rgb(251, 133, 50)'
								} ])
							}
						},
						data : data
					}
				]
			};
			var optionAdditions = {
				tooltip : {
					trigger : 'axis',
					formatter : '{b} <br/>{a}: {c}k',
					position : function(pt) {
						return [ pt[0], '10%' ];
					}
				},
				title : {
					left : 'center',
					text : '',
				},
				grid : {
					left : "5%",
					right : "1%",
					top : 30,
					buttom : 0
				},
				toolbox : {
					feature : {
						dataZoom : {
							yAxisIndex : 'none',
							iconStyle : {
								opacity : 0
							}
						},
						restore : {
							title: 'Restore date range',
							iconStyle : {
								opacity : 0
							}
						},
					},
					right: 40
				},
				xAxis : {
					type : 'category',
					boundaryGap : false,
					data : monthdate,
					axisLabel : {
						clickable : true
					}
				},
				yAxis : {
					axisLabel : {
						formatter : function(value) {
							return value + 'k'
						}
					},
					axisLine : {
						show : false
					},
					type : 'value',
					max : function(value) {
						return value.max * 1;
					},
					boundaryGap : [ 0, '100%' ]
				},

				series : [
					{
						name : 'additions',
						type : 'line',
						smooth : true,
						symbol : 'none',
						sampling : 'average',
						itemStyle : {
							normal : {
								color : 'rgba(0, 0, 0,0)'
							}
						},
						areaStyle : {
							normal : {
								color : new echarts.graphic.LinearGradient(0, 0, 0, 1, [ {
									offset : 0,
									color : 'rgb(251, 133, 50)'
								}, {
									offset : 1,
									color : 'rgb(251, 133, 50)'
								} ])
							}
						},
						data : monthdata
					}
				]
			};
			var optionAdditionsDaily = {
				tooltip : {
					trigger : 'axis',
					formatter : '{b} <br/>{a}: {c}k',
					position : function(pt) {
						return [ pt[0], '10%' ];
					}
				},
				title : {
					left : 'center',
					text : '',
				},
				grid : {
					left : "5%",
					right : "1%",
					top : 30,
					buttom : 0
				},
				toolbox : {
					feature : {
						dataZoom : {
							yAxisIndex : 'none',
							iconStyle : {
								opacity : 0
							}
						},
						restore : {
							title: 'Restore date range',
							iconStyle : {
								opacity : 0
							}
						},
					},
					right: 40
				},
				xAxis : {
					type : 'category',
					boundaryGap : false,
					data : date,
					axisLabel : {
						clickable : true
					}
				},
				yAxis : {
					axisLabel : {
						formatter : function(value) {
							return value + 'k'
						}
					},
					axisLine : {
						show : false
					},
					type : 'value',
					max : function(value) {
						return value.max * 1;
					},
					boundaryGap : [ 0, '100%' ]
				},

				series : [
					{
						name : 'additions',
						type : 'line',
						smooth : true,
						symbol : 'none',
						sampling : 'average',
						itemStyle : {
							normal : {
								color : 'rgba(0, 0, 0,0)'
							}
						},
						areaStyle : {
							normal : {
								color : new echarts.graphic.LinearGradient(0, 0, 0, 1, [ {
									offset : 0,
									color : 'rgb(251, 133, 50)'
								}, {
									offset : 1,
									color : 'rgb(251, 133, 50)'
								} ])
							}
						},
						data : data
					}
				]
			};


			var optionDeletions = {
				tooltip : {
					trigger : 'axis',
					formatter : '{b} <br/>{a}: {c}k',
					position : function(pt) {
						return [ pt[0], '10%' ];
					}
				},
				title : {
					left : 'center',
					text : '',
				},
				grid : {
					left : "5%",
					right : "1%",
					top : 30,
					buttom : 0
				},
				toolbox : {
					feature : {
						dataZoom : {
							yAxisIndex : 'none',
							iconStyle : {
								opacity : 0
							}
						},
						restore : {
							title: 'Restore date range',
							iconStyle : {
								opacity : 0
							}
						},
					},
					right: 40
				},
				xAxis : {
					type : 'category',
					boundaryGap : false,
					data : monthdate,
					axisLabel : {
						clickable : true
					}
				},
				yAxis : {
					axisLabel : {
						formatter : function(value) {
							return value + 'k'
						}
					},
					axisLine : {
						show : false
					},
					type : 'value',
					max : function(value) {
						return value.max * 1;
					},
					boundaryGap : [ 0, '100%' ]
				},

				series : [
					{
						name : 'deletions',
						type : 'line',
						smooth : true,
						symbol : 'none',
						sampling : 'average',
						itemStyle : {
							normal : {
								color : 'rgba(0, 0, 0,0)'
							}
						},
						areaStyle : {
							normal : {
								color : new echarts.graphic.LinearGradient(0, 0, 0, 1, [ {
									offset : 0,
									color : 'rgb(251, 133, 50)'
								}, {
									offset : 1,
									color : 'rgb(251, 133, 50)'
								} ])
							}
						},
						data : monthdata
					}
				]
			};
			var optionDeletionsDaily = {
				tooltip : {
					trigger : 'axis',
					formatter : '{b} <br/>{a}: {c}k',
					position : function(pt) {
						return [ pt[0], '10%' ];
					}
				},
				title : {
					left : 'center',
					text : '',
				},
				grid : {
					left : "5%",
					right : "1%",
					top : 30,
					buttom : 0
				},
				toolbox : {
					feature : {
						dataZoom : {
							yAxisIndex : 'none',
							iconStyle : {
								opacity : 0
							}
						},
						restore : {
							title: 'Restore date range',
							iconStyle : {
								opacity : 0
							}
						},
					},
					right: 40
				},
				xAxis : {
					type : 'category',
					boundaryGap : false,
					data : date,
					axisLabel : {
						clickable : true
					}
				},
				yAxis : {
					axisLabel : {
						formatter : function(value) {
							return value + 'k'
						}
					},
					axisLine : {
						show : false
					},
					type : 'value',
					max : function(value) {
						return value.max * 1;
					},
					boundaryGap : [ 0, '100%' ]
				},

				series : [
					{
						name : 'deletions',
						type : 'line',
						smooth : true,
						symbol : 'none',
						sampling : 'average',
						itemStyle : {
							normal : {
								color : 'rgba(0, 0, 0,0)'
							}
						},
						areaStyle : {
							normal : {
								color : new echarts.graphic.LinearGradient(0, 0, 0, 1, [ {
									offset : 0,
									color : 'rgb(251, 133, 50)'
								}, {
									offset : 1,
									color : 'rgb(251, 133, 50)'
								} ])
							}
						},
						data : data
					}
				]
			};

			if (orderBy == "COMMITS") {
				if (gap > 365) myChart.setOption(optionCommits);
				else myChart.setOption(optionCommitsDaily);
			} else if (orderBy == "ADDITIONS") {
				if (gap > 365) myChart.setOption(optionAdditions);
				else myChart.setOption(optionAdditionsDaily);
			} else {
				if (gap > 365) myChart.setOption(optionDeletions);
				else myChart.setOption(optionDeletionsDaily);
			}
			$(window).resize(function() {
				myChart.resize();
			});
			myChart.dispatchAction({
				type : 'takeGlobalCursor',
				key : 'dataZoomSelect',
				dataZoomSelectActive : true // 允许缩放
			});

			myChart.on('restore', function(params) {
				myChart.clear();
				if (gap > 365) {
					if (orderBy == "COMMITS") {
						myChart.setOption(optionCommits);
					} else if (orderBy == "ADDITIONS") {
						myChart.setOption(optionAdditions);
					} else {
						myChart.setOption(optionDeletions);
					}
					myChart.dispatchAction({
						type : 'takeGlobalCursor',
						key : 'dataZoomSelect',
						dataZoomSelectActive : true
					});
				} else {
					if (orderBy == "COMMITS") {
						myChart.setOption(optionCommitsDaily);
					} else if (orderBy == "ADDITIONS") {
						myChart.setOption(optionAdditionsDaily);
					} else {
						myChart.setOption(optionDeletionsDaily);
					}
					myChart.dispatchAction({
						type : 'takeGlobalCursor',
						key : 'dataZoomSelect',
						dataZoomSelectActive : true
					});
				}
			});

			myChart.on('datazoom', function(params) {
				myChart.setOption({
					toolbox : {
						feature : {
							restore : {
								title: 'Restore date range',
								iconStyle : {
									opacity : 1
								}
							}
						},
						right: 40
					},
				});
				var startValue = myChart.getModel().option.dataZoom[0].startValue;
				var endValue = myChart.getModel().option.dataZoom[0].endValue;

				if (monthdata[startValue] == 0) {
					for (var i = startValue + 1; i < endValue; i++) {
						if (monthdata[i] != 0) {
							startValue = i;
							break;
						}
					}
				}
				if (monthdata[endValue] == 0) {
					for (var i = endValue - 1; i >= startValue; i--) {
						if (monthdata[i] != 0) {
							endValue = i;
							break;
						}
					}
				}

				var startdate = Date.parse(monthdate[startValue]);
				var enddate = Date.parse(monthdate[endValue]);

				var startday = new Date(startdate);
				var endday = new Date(enddate);
				var gapday = (enddate - startdate) / (24 * 3600 * 1000);
				if (gapday < 365 && monthdata[startValue] != 0 && monthdata[endValue] != 0) {
					var start = 0;
					var end = jsonUserDailyContribution.length - 1;
					for (var i = 0; i < jsonUserDailyContribution.length; i++) {
						var day = new Date(jsonUserDailyContribution[i].day.dateTime);
						if (start == 0) {
							if (day.getFullYear() == startday.getFullYear() && day.getMonth() == startday.getMonth()) {
								start = i;
							}
						}
						if (day.getFullYear() == endday.getFullYear() && day.getMonth() == endday.getMonth()) {
							end = i;
							break;
						}
					}

					var userDailyContribution = jsonUserDailyContribution.slice(start, end + 1);
					var newdata = [];
					var newdate = [];
					var byDay = getDailyData(userDailyContribution);
					for (var i = 0; i < byDay.length / 2; i++) {
						newdata.push(byDay[i]);
					}
					for (var j = byDay.length / 2; j < byDay.length; j++) {
						newdate.push(byDay[j]);
					}

					myChart.clear();
					myChart.setOption({
						tooltip : {
							trigger : 'axis',
							position : function(pt) {
								return [ pt[0], '10%' ];
							}
						},
						title : {
							left : 'center',
							text : '',
						},
						grid : {
							left : "5%",
							right : "1%",
							top : 30,
							buttom : 0
						},
						toolbox : {
							feature : {
								dataZoom : {
									yAxisIndex : 'none',
									iconStyle : {
										opacity : 0
									}
								},
								restore : {
									title: 'Restore date range'
								},
							},
							right: 40
						},
						xAxis : {
							type : 'category',
							boundaryGap : false,
							data : newdate,
							axisLabel : {
								clickable : true
							}
						},
						yAxis : {
							axisLine : {
								show : false
							},
							type : 'value',
							minInterval : 1,
							max : function(value) {
								return value.max * 1;
							},
							boundaryGap : [ 0, '100%' ]
						},

						series : [
							{
								//name:'commits',
								type : 'line',
								smooth : true,
								symbol : 'none',
								sampling : 'average',
								itemStyle : {
									normal : {
										color : 'rgba(0, 0, 0,0)'
									}
								},
								areaStyle : {
									normal : {
										color : new echarts.graphic.LinearGradient(0, 0, 0, 1, [ {
											offset : 0,
											color : 'rgb(251, 133, 50)'
										}, {
											offset : 1,
											color : 'rgb(251, 133, 50)'
										} ])
									}
								},
								data : newdata
							}
						]
					}, true);
					if (orderBy == "COMMITS") {
						myChart.setOption({
							series : [
								{
									name : 'commits',
								}
							]
						});
					} else if (orderBy == "ADDITIONS") {
						myChart.setOption({
							yAxis : {
								axisLabel : {
									formatter : function(value) {
										return value + 'k'
									}
								},
							},
							series : [
								{
									name : 'additions',
								}
							]
						});
					} else {
						myChart.setOption({
							yAxis : {
								axisLabel : {
									formatter : function(value) {
										return value + 'k'
									}
								},
							},
							series : [
								{
									name : 'deletions',
								}
							]
						});
					}
					myChart.dispatchAction({
						type : 'takeGlobalCursor',
						key : 'dataZoomSelect',
						dataZoomSelectActive : true
					});
				}
			});
		}
	},

	sourceLines : {
		onDomReady : function() {}
	}
}