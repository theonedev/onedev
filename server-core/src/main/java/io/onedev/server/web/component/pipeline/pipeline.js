onedev.server.pipeline = {
	lineWidth: 2,
	lineColor: onedev.server.isDarkMode()?"#535370":"#D1D3E0",
	onWindowLoad: function(containerId, dependencies, activeJobIndex) {
		function getJobIndex(jobIndexString) {
			var splitted = jobIndexString.split("-");	
			return {
				column: parseInt(splitted[0]),
				row: parseInt(splitted[1])
			}
		}

		var $pipeline = $("#" + containerId + ">.pipeline");
		
		for (var jobIndexString in dependencies) {
			var jobIndex = getJobIndex(jobIndexString); 
			for (var i in dependencies[jobIndexString]) {
				var dependencyJobIndexString = dependencies[jobIndexString][i];
				var dependencyJobIndex = getJobIndex(dependencyJobIndexString);								
				onedev.server.pipeline.drawDependencyLine($pipeline, dependencyJobIndex, jobIndex);
			}
		}
		
		if (activeJobIndex)
			onedev.server.pipeline.markJobActive($pipeline, activeJobIndex);
	},
	drawDependencyLine: function($pipeline, dependencyJobIndex, jobIndex) {
		var dependencyJobIndexString = dependencyJobIndex.column + "-" + dependencyJobIndex.row;
		var jobIndexString = jobIndex.column + "-" + jobIndex.row;
		var $paper = $pipeline.children(".dependencies");
		var paper = Snap($paper[0]);
		var $dependencyJob = onedev.server.pipeline.getJob($pipeline, dependencyJobIndex);
		var $job = onedev.server.pipeline.getJob($pipeline, jobIndex);
		var columnSpacing = parseInt($job.parent().css("margin-left"));
		var rowSpacing = parseInt($job.css("margin-bottom"));
		var dependencyStart = {
			left: $dependencyJob.offset().left + $dependencyJob.outerWidth() - $paper.offset().left,
			top: $dependencyJob.offset().top + $dependencyJob.outerHeight()/2 - $paper.offset().top
		}
		var dependencyStop = {
			left: $job.offset().left - $paper.offset().left,
			top: $job.offset().top + $job.outerHeight()/2 - $paper.offset().top
		}
		
		function hasIntermediateJobs(row) {
			for (var column = dependencyJobIndex.column+1; column < jobIndex.column; column++) {
				var $rows = $pipeline.children().eq(column).children();
				if ($rows.length > row)
					return true; 
			}
			return false;						
		}
		
		if (dependencyJobIndex.row == jobIndex.row) {
			if (hasIntermediateJobs(jobIndex.row)) {
				var leftCurveStop = {
					left: dependencyStart.left + columnSpacing,
					top: $dependencyJob.offset().top + $dependencyJob.outerHeight() + rowSpacing/2 - $paper.offset().top
				}
				onedev.server.pipeline.drawCurve(paper, dependencyStart, leftCurveStop, dependencyJobIndexString, jobIndexString);

				var rightCurveStart = {
					left: dependencyStop.left - columnSpacing,
					top: leftCurveStop.top
				}				
				onedev.server.pipeline.drawCurve(paper, rightCurveStart, dependencyStop, dependencyJobIndexString, jobIndexString);
				
				paper.line(leftCurveStop.left, leftCurveStop.top, rightCurveStart.left, rightCurveStart.top).attr({
					stroke: onedev.server.pipeline.lineColor,
					strokeWidth: onedev.server.pipeline.lineWidth,
					from: dependencyJobIndexString,
					to: jobIndexString
				});
			} else {
				paper.line(dependencyStart.left, dependencyStart.top, dependencyStop.left, dependencyStop.top).attr({
					stroke: onedev.server.pipeline.lineColor,
					strokeWidth: onedev.server.pipeline.lineWidth,
					from: dependencyJobIndexString,
					to: jobIndexString
				});
			}
		} else if (dependencyJobIndex.row > jobIndex.row) {
			if (hasIntermediateJobs(dependencyJobIndex.row)) {
				var leftCurveStop = {
					left: dependencyStart.left + columnSpacing,
					top: $dependencyJob.offset().top - rowSpacing/2 - $paper.offset().top
				}
				onedev.server.pipeline.drawCurve(paper, dependencyStart, leftCurveStop, dependencyJobIndexString, jobIndexString);
				
				var rightCurveStart = {
					left: dependencyStop.left - columnSpacing,
					top: leftCurveStop.top
				}				
				onedev.server.pipeline.drawCurve(paper, rightCurveStart, dependencyStop, dependencyJobIndexString, jobIndexString);
				
				paper.line(leftCurveStop.left, leftCurveStop.top, rightCurveStart.left, rightCurveStart.top).attr({
					stroke: onedev.server.pipeline.lineColor,
					strokeWidth: onedev.server.pipeline.lineWidth,
					from: dependencyJobIndexString,
					to: jobIndexString
				});
			} else {
				curveStart = {
					left: dependencyStop.left - columnSpacing,
					top: dependencyStart.top
				}
				onedev.server.pipeline.drawCurve(paper, curveStart, dependencyStop, dependencyJobIndexString, jobIndexString);
				
				paper.line(dependencyStart.left, dependencyStart.top, curveStart.left, curveStart.top).attr({
					stroke: onedev.server.pipeline.lineColor,
					strokeWidth: onedev.server.pipeline.lineWidth,
					from: dependencyJobIndexString,
					to: jobIndexString
				});
			}		
		} else {
			if (hasIntermediateJobs(jobIndex.row)) {
				var leftCurveStop = {
					left: dependencyStart.left + columnSpacing,
					top: $dependencyJob.offset().top + $dependencyJob.outerHeight() + rowSpacing/2 - $paper.offset().top
				}
				onedev.server.pipeline.drawCurve(paper, dependencyStart, leftCurveStop, dependencyJobIndexString, jobIndexString);
				
				var rightCurveStart = {
					left: dependencyStop.left - columnSpacing,
					top: leftCurveStop.top
				}				
				onedev.server.pipeline.drawCurve(paper, rightCurveStart, dependencyStop, dependencyJobIndexString, jobIndexString);
				
				paper.line(leftCurveStop.left, leftCurveStop.top, rightCurveStart.left, rightCurveStart.top).attr({
					stroke: onedev.server.pipeline.lineColor,
					strokeWidth: onedev.server.pipeline.lineWidth,
					from: dependencyJobIndexString,
					to: jobIndexString
				});
			} else {
				curveStop = {
					left: dependencyStart.left + columnSpacing,
					top: dependencyStop.top
				}
				onedev.server.pipeline.drawCurve(paper, dependencyStart, curveStop, dependencyJobIndexString, jobIndexString);
				
				paper.line(curveStop.left, curveStop.top, dependencyStop.left, dependencyStop.top).attr({
					stroke: onedev.server.pipeline.lineColor,
					strokeWidth: onedev.server.pipeline.lineWidth,
					from: dependencyJobIndexString,
					to: jobIndexString
				});
			}		
		}
		/*
		var arrowHead = {
			left: $job.offset().left - $paper.offset().left,
			top: $job.offset().top + $job.outerHeight()/2 - $paper.offset().top
		}
		var arrowWidth = 8;
		var arrowHeight = 8;
		paper.path(
			"M" + 
			arrowHead.left + "," + arrowHead.top + " " +
			"l" + -1*arrowHeight + "," + -1*arrowWidth/2 + " " +
			"l" + "0," + arrowWidth + " " + 
			"Z"
		).attr({
			fill: onedev.server.pipeline.lineColor,
			stroke: onedev.server.pipeline.lineColor,
			strokeWidth: onedev.server.pipeline.lineWidth,
			from: dependencyJobIndexString,
			to: jobIndexString
		});
		*/
	},
	drawCurve: function(paper, from, to, dependencyJobIndexString, jobIndexString) {
		var curve = paper.path(
				"M" + 
				from.left + "," + from.top + " " +
				"C" + 
				(from.left + to.left)/2 + "," + from.top + " " + 
				(from.left + to.left)/2 + "," + to.top + " " + 
				to.left + "," + to.top);
		curve.attr({
			fill: "none",
			stroke: onedev.server.pipeline.lineColor,
			strokeWidth: onedev.server.pipeline.lineWidth,
			from: dependencyJobIndexString, 
			to: jobIndexString
		});
	},
	getJob: function($pipeline, jobIndex) {
		return $pipeline.children().eq(jobIndex.column).children().eq(jobIndex.row);	
	},
	markJobActive: function($pipeline, jobIndex) {
		$pipeline.find(".pipeline-row").removeClass("active");
		var paper = Snap($pipeline.children(".dependencies")[0]);
		paper.selectAll("path, line").forEach(function(e) {
			e.attr({
				stroke: onedev.server.pipeline.lineColor
			});
		});
		onedev.server.pipeline.getJob($pipeline, jobIndex).addClass("active");
		var jobIndexString = jobIndex.column + "-" + jobIndex.row;
		paper.selectAll("[from='" + jobIndexString + "'], [to='" + jobIndexString + "']").forEach(function(e) {
			e.attr({
				stroke: "#3699FF"
			});
			paper.append(e);
		});
		$pipeline.find(".active")[0].scrollIntoViewIfNeeded(false);
	},
	onSortStart: function($uiItem) {
		var $pipeline = $uiItem.closest(".pipeline");
		var paper = Snap($pipeline.children(".dependencies")[0]);
		paper.selectAll("path, line").forEach(function(e) {
			e.attr({
				strokeWidth: 0
			});
		});
		$(".ui-sortable-placeholder").width($uiItem.width());
		if (onedev.server.isDarkMode())
			$uiItem.parent().css("background", "#2b2b40");
		else
			$uiItem.parent().css("background", "#FFFFE8");
	},
	onSortStop: function($uiItem) {
		var $pipeline = $uiItem.closest(".pipeline");
		var paper = Snap($pipeline.children(".dependencies")[0]);
		paper.selectAll("path, line").forEach(function(e) {
			e.attr({
				strokeWidth: onedev.server.pipeline.lineWidth
			});
		});
		$uiItem.parent().css("background", "inherit");
	}
}