onedev.server.stats = {
	contribs : {
		onDomReady : function(jsonList, orderBy, fromDay, toDay, gapday) {

			//console.log(">>>>>>>>>>gapday:",gapday,"fromDay:",fromDay,"toDay",toDay);
			var flag = false;
			function getDailyData(list) {
				//var dailyData=[];
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

			/*get Whole Monthly Data from jsonList*/
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
			/*get Sliced DailyData from the Whole DailyData*/
			function getSlicedDailyData(list, fromday, today) {
				var slicedDate = [];
				var slicedData = [];
				for (var i = 0; i < list.length / 2; i++) {
					slicedData.push(list[i]);
				}
				for (var j = list.length / 2; j < list.length; j++) {
					slicedDate.push(list[j]);
				}

				var firstday,
					lastday;
				if (fromday == 'null' && today == 'null') {
					firstday = 0;
					lastday = slicedDate.length - 1;
				} else {
					fromday = fromday.replace(/-/g, "/");
					today = today.replace(/-/g, "/");
					for (var j = 0; j < slicedDate.length; j++) {
						if (slicedDate[j] == fromday)
							firstday = j;
						if (slicedDate[j] == today) {
							lastday = j;
							break;
						}
					}

				}
				slicedDate = slicedDate.slice(firstday, lastday + 1);
				slicedData = slicedData.slice(firstday, lastday + 1);
				slicedData = slicedData.concat(slicedDate);
				return slicedData;
			}
			/*get Sliced MonthData from the Whole MonthData*/
			function getSlicedMonthData(list, fromday, today) {
				//console.log("FUNCTION GETSLICED MONTHDATA",fromday,today)
				var slicedDate = [];
				var slicedData = [];
				var MONTH = new Array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
				for (var i = 0; i < list.length / 2; i++) {
					slicedData.push(list[i]);
				}
				for (var j = list.length / 2; j < list.length; j++) {
					slicedDate.push(list[j]);
				}

				var firstday,
					lastday;
				if (fromday == 'null' && today == 'null') {
					firstday = 0;
					lastday = slicedDate.length - 1;
				} else {
					fromday = fromday.replace(/-/g, "/");
					today = today.replace(/-/g, "/");
					var year_fromday = fromday.split("/")[0];
					var month_fromday = fromday.split("/")[1];
					var year_today = today.split("/")[0];
					var month_today = today.split("/")[1];
					//console.log("##########year:",year_fromday," month",month_fromday);
					for (var j = 0; j < slicedDate.length; j++) {
						var year = slicedDate[j].split("/")[0];
						var month = slicedDate[j].split("/")[1];
						if (year == year_fromday && month == MONTH[month_fromday - 1]) {
							firstday = j;
							break;
						}

					}
					for (var j = firstday; j < slicedDate.length; j++) {
						var year = slicedDate[j].split("/")[0];
						var month = slicedDate[j].split("/")[1];
						if (year == year_today && month == MONTH[month_today - 1]) {
							lastday = j;
							break;
						}
					}

				}
				slicedDate = slicedDate.slice(firstday, lastday + 1);
				slicedData = slicedData.slice(firstday, lastday + 1);
				slicedData = slicedData.concat(slicedDate);
				return slicedData;
			}
			//				console.log("fromDay and toDay",fromDay,"###",toDay);

			var showdata = [];
			var showdate = [];

			/*get showdata for monthContribution*/
			if (gapday > 365) {
				var wholeMonthData = getMonthData(jsonList);
				var monthData = getSlicedMonthData(wholeMonthData, fromDay, toDay);
				//console.log("month",monthData);
				for (var i = 0; i < monthData.length / 2; i++) {
					showdata.push(monthData[i]);
				}
				for (var j = monthData.length / 2; j < monthData.length; j++) {
					showdate.push(monthData[j]);
				}
			}

			/*get showdata for dailyContribution*/
			else {
				var wholeDailyData = getDailyData(jsonList);
				var dailyData = getSlicedDailyData(wholeDailyData, fromDay, toDay);
				for (var i = 0; i < dailyData.length / 2; i++) {
					showdata.push(dailyData[i]);
				}
				for (var j = dailyData.length / 2; j < dailyData.length; j++) {
					showdate.push(dailyData[j]);
				}
			}
			//console.log("showdata",showdata);
			var myChart = echarts.init(document.getElementById('project-contribs'));

			var optionCommits = {
				tooltip : {
					trigger : 'axis',
					// formatter: '{b} <br/>{a}:{c}s',
					position : function(pt) {
						return [ pt[0], '10%' ];
					}
				},
				title : {
					left : 'center',
					text : 'OverallContribution',
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
					/*restore: {
						//icon:'path://M30.9,53.2C16.8,53.2,5.3,41.7,5.3,27.6S16.8,2,30.9,2C45,2,56.4,13.5,56.4,27.6S45,53.2,30.9,53.2z M30.9,3.5C17.6,3.5,6.8,14.4,6.8,27.6c0,13.3,10.8,24.1,24.101,24.1C44.2,51.7,55,40.9,55,27.6C54.9,14.4,44.1,3.5,30.9,3.5z M36.9,35.8c0,0.601-0.4,1-0.9,1h-1.3c-0.5,0-0.9-0.399-0.9-1V19.5c0-0.6,0.4-1,0.9-1H36c0.5,0,0.9,0.4,0.9,1V35.8z M27.8,35.8 c0,0.601-0.4,1-0.9,1h-1.3c-0.5,0-0.9-0.399-0.9-1V19.5c0-0.6,0.4-1,0.9-1H27c0.5,0,0.9,0.4,0.9,1L27.8,35.8L27.8,35.8z',
						iconStyle:{
							textPosition:'left',
							opacity:1
						}
					},*/
					//saveAsImage: {}
					}
				//show:false
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
					/*max: function(value) {
					    return value.max ;
					},*/
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
					formatter : '{b} <br/>{a}:{c}k',
					position : function(pt) {
						return [ pt[0], '10%' ];
					}
				},
				title : {
					left : 'center',
					text : 'OverallContribution',
				},
				grid : {
					left : "4%",
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
					/*restore: {
						iconStyle:{
							opacity:1
						}
					},*/
					//saveAsImage: {}
					}
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
					formatter : '{b} <br/>{a}:{c}k',
					position : function(pt) {
						return [ pt[0], '10%' ];
					}
				},
				title : {
					left : 'center',
					text : 'OverallContribution',
				},
				grid : {
					left : "4%",
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
					/*restore: {
						iconStyle:{
					    	opacity:1
					    }
					},*/
					//saveAsImage: {}
					}
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

			if (orderBy == "COMMITS") {
				myChart.setOption(optionCommits, true);
			} else if (orderBy == "ADDITIONS") {
				myChart.setOption(optionAdditions, true);
			} else {
				myChart.setOption(optionDeletions, true);
			}
			var firstday = new Date(jsonList[0].day.dateTime);
			var firstDay = [ firstday.getFullYear(), firstday.getMonth() + 1, firstday.getDate() ].join('-');
			var lastday = new Date(jsonList[jsonList.length - 1].day.dateTime);
			var lastDay = [ lastday.getFullYear(), lastday.getMonth() + 1, lastday.getDate() ].join('-');

			//				 console.log("firstday and lastday",firstDay,"##",lastDay);
			if ((fromDay == 'null' && toDay == 'null') || (fromDay == firstDay && toDay == lastDay)) {
				myChart.setOption({
					toolbox : {
						feature : {
							restore : {
								title : 'Restore date range',
								iconStyle : {
									opacity : 0
								}
							}
						},
						right: '40px'
					},
				});
			} else {
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
						right: '40px'
					},
				});
			}

			myChart.dispatchAction({
				type : 'takeGlobalCursor',
				key : 'dataZoomSelect',
				dataZoomSelectActive : true // 允许缩放
			});

			myChart.on('restore', function(params) {
				var firstday = new Date(jsonList[0].day.dateTime);
				var lastday = new Date(jsonList[jsonList.length - 1].day.dateTime)
				myFunction([ firstday.getFullYear(), firstday.getMonth() + 1, firstday.getDate() ].join('/'), [ lastday.getFullYear(), lastday.getMonth() + 1, lastday.getDate() ].join('/'));
				myChart.setOption({
					toolbox : {
						feature : {
							restore : {
                				title: 'Restore date range',
								iconStyle : {
									opacity : 0
								}
							}
						},
						right: '40px'
					},
				});
				redraw([ firstday.getFullYear(), firstday.getMonth() + 1, firstday.getDate() ].join('/'), [ lastday.getFullYear(), lastday.getMonth() + 1, lastday.getDate() ].join('/'));
			});
			$(window).resize(function() {myChart.resize()});

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
						right: '40px'
					},
				});
				var startIndex = myChart.getModel().option.dataZoom[0].startValue;
				var endIndex = myChart.getModel().option.dataZoom[0].endValue;
				var startdate = Date.parse(showdate[startIndex]);
				var enddate = Date.parse(showdate[endIndex]);
				var startday = new Date(startdate);
				var endday = new Date(enddate);
				var currGap = (enddate - startdate) / (24 * 3600 * 1000);
				//console.log("<<<<<<currGap+from+tp:",currGap,[startday.getFullYear(),startday.getMonth() + 1, startday.getDate()].join('/'),[endday.getFullYear(),endday.getMonth() + 1, endday.getDate()].join('/'))
				if (gapday > 365) {
					if (currGap < 365) {
						redraw([ startday.getFullYear(), startday.getMonth() + 1, startday.getDate() ].join('/'), [ endday.getFullYear(), endday.getMonth() + 1, endday.getDate() ].join('/'));
						myFunction([ startday.getFullYear(), startday.getMonth() + 1, startday.getDate() ].join('/'), [ endday.getFullYear(), endday.getMonth() + 1, endday.getDate() ].join('/'));
					} else {
						myFunction([ startday.getFullYear(), startday.getMonth() + 1, startday.getDate() ].join('/'), [ endday.getFullYear(), endday.getMonth() + 1, endday.getDate() ].join('/'));
					}
				} else {
					myFunction([ startday.getFullYear(), startday.getMonth() + 1, startday.getDate() ].join('/'), [ endday.getFullYear(), endday.getMonth() + 1, endday.getDate() ].join('/'));
				}
			});
		},


		ondrawLinesReady : function(flag, jsonUserDailyContribution, orderBy, gap) {
			//console.log("flag:"+flag+"  "+gap);
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
						//saveAsImage: {show:false},
						/*mytool:{//自定义按钮 danielinbiti,这里增加，selfbuttons可以随便取名字  
						       show:true,//是否显示  
						       title:'自定义', //鼠标移动上去显示的文字  
						       icon: 'image://images/timg.jpg',
						       option:{},  
						       onclick:function() {//点击事件,这里的option1是chart的option信息  
						             alert('1');//这里可以加入自己的处理代码，切换不同的图形  
						             }  
						},*/

					},
					right: '40px'
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
					//saveAsImage: {}
					},
					right: '40px'
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
					formatter : '{b} <br/>{a}:{c}k',
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
					//saveAsImage: {}
					},
					right: '40px'
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
					/*max: function(value) {
					    return value.max *1;
					},*/
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
					formatter : '{b} <br/>{a}:{c}k',
					position : function(pt) {
						return [ pt[0], '10%' ];
					}
				},
				title : {
					left : 'center',
					text : '',
				},
				grid : {
					left : "7%",
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
					//saveAsImage: {}
					},
					right: '40px'
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
					formatter : '{b} <br/>{a}:{c}k',
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
					//saveAsImage: {}
					},
					right: '40px'
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
					formatter : '{b} <br/>{a}:{c}k',
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
					//saveAsImage: {}
					},
					right: '40px'
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
			//myChart.showLoading();
			$(window).resize(function() {myChart.resize();});
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
						right: '40px'
					},
				});
				var startValue = myChart.getModel().option.dataZoom[0].startValue;
				var endValue = myChart.getModel().option.dataZoom[0].endValue;

				//console.log("start"+monthdate[startValue]+"/1");
				//console.log("end"+monthdate[endValue]+"/1");

				//console.log(monthdata[startValue]+" + "+monthdata[endValue]);
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
				//console.log("start----------",[startday.getFullYear(),startday.getMonth() + 1, startday.getDate()].join('/'),[endday.getFullYear(),endday.getMonth() + 1, endday.getDate()].join('/'));
				//console.log("gapday in user:"+gapday);
				if (gapday < 365 && monthdata[startValue] != 0 && monthdata[endValue] != 0) {
					//console.log("here gapday",gapday);
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
					//console.log("start:"+start+"   end:"+end);

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
					//console.log("date:"+newdate);
					//console.log("data:"+newdata);

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
	                				title: 'Restore date range',
								},
							//saveAsImage: {}
							},
							right: '40px'
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