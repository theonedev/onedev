gitplex.repocommits = {
	init: function(commits) {
		$(window).resize(function(e) {
			e.stopPropagation();
			gitplex.repocommits.drawCommitLane();
		});		
		gitplex.repocommits.renderCommitLane(commits);
	},

	/*
	 * commits is an array of ordered commit object, and the commit object is itself a 
	 * list of parent indexes
	 */
	renderCommitLane: function(commits) {
		var columnsLimit = 12;
		
		var colorsLimit = 12;

		/*
		 * rows store the map of line to row. A line represents a child->parent relationship. 
		 * For instance line(1,5) represents the line from commit at row 1 (the child ) to 
		 * commit at row 5 (the parent). In case child row index equals parent row index, 
		 * the line represents a commit. 
		 * 
		 */
		var rows = [];
		var parent2children = {};
		var colors = {};
		
		var colorStack = [];
		function getSortedLines(row, reverse) {
			var lines = [];
			var keys = Object.keys(row);
			for (var i in keys)
				lines.push(keys[i]);

			lines.sort(function(x, y) {
				if (reverse)
					return row[y] - row[x];
				else
					return row[x] - row[y];
			});
			return lines;
		}
		
		function fromKey(lineKey) {
			return lineKey.split(',').map(function(item) {
				return parseInt(item, 10);
			});
		}
		
		function toKey(line) {
			return line[0] + "," + line[1];
		}
		
		function assignColor(lineKey) {
			var color = colors[lineKey];
			if (color == undefined) {
				if (colorStack.length == 0) {
					for (var i=colorsLimit; i>=1; i--)
						colorStack.push(i);
				}
				color = colorStack.pop();
				colors[lineKey] = color;
			}
			return color;
		}
		
		for (var i in commits) {
			commits[i] = commits[i][1];
			var parents = commits[i];
			for (var j in parents) {
				var parent = parents[j];
				var children = parent2children[parent];
				if (children == undefined) {
					children = [];
					parent2children[parent] = children;
				}
				children.push(i);
			}
		}
		
		for (var rowIndex in commits) {
			var row = {};
			if (rowIndex == 0) {
				row["0,0"] = 0;
			} else {
				var parents = commits[rowIndex-1];
				// special line represents the commit at rowIndex itself
				var commitLineKey = toKey([rowIndex, rowIndex]);
				var lastRow = rows[rowIndex-1];
				var column = 0;
				var linesOfLastRow = getSortedLines(lastRow, false);
				for (var i in linesOfLastRow) {
					var lineOfLastRow = fromKey(linesOfLastRow[i]);
					if (lineOfLastRow[1] < 0) // line is cutted due to max columns limitation
						continue;
					if (lineOfLastRow[1] != lineOfLastRow[0]) { // not a commit point
						// line not started from last row, in this case, the line 
						// only occupies a column when it goes through current row 
						if (lineOfLastRow[1] == rowIndex) { 
							if (row[commitLineKey] == undefined)
								row[commitLineKey] = column++;
						} else { 
							row[toKey(lineOfLastRow)] = column++;
						}
					} else {
						for (var j in parents) {
							var parent = parents[j];
							if (parent == rowIndex) {
								if (row[commitLineKey] == undefined)
									row[commitLineKey] = column++;
							} else {
								row[toKey([rowIndex-1, parent])] = column++;
							}
						}
					}
				}
				
				if (row[commitLineKey] == undefined)
					row[commitLineKey] = column++;
				if (column > columnsLimit) {
					var reversedLines = getSortedLines(row, true);
					for (var i in reversedLines) {
						var line = fromKey(reversedLines[i]);
						if (line[0] == rowIndex-1) {
							var cuttedLine = [line[0], line[1]*-1];
							var lineKey = toKey(line);
							var lineColumn = row[lineKey];
							delete row[lineKey];
							row[toKey(cuttedLine)] = lineColumn;
							column--;
							if (column == columnsLimit)
								break;
						}
					}
					if (column != columnsLimit)
						throw "Error calculating commit lane at row " + rowIndex;
				}
				
				var cuttedLines = [];
				var children = parent2children[rowIndex];
				if (children) {
					for (var i in children) {
						var child = children[i];
						if (child != rowIndex-1) {
							var line = [child, rowIndex];
							var cuttedLine = [child, rowIndex*-1];
							var lineKey = toKey(line);
							var cuttedLineKey = toKey(cuttedLine);
							if (lastRow[lineKey] == undefined) {
								if (lastRow[cuttedLineKey] != undefined) {
									var cuttedLineColumn = lastRow[cuttedLineKey];
									delete lastRow[cuttedLineKey];
									lastRow[lineKey] = cuttedLineColumn;
								} else { 
									cuttedLines.push(cuttedLine);
								}
							}
						}
					}
				}
				if (cuttedLines.length != 0) {
					// for every disappeared line, we need to make them appear again in last row
					// so that end part of the line can be drawn from last row to this row. 
					// Below code find column in last row to insert these appeared lines, and 
					// we want to make sure that this column can result in minimum line crossovers. 
					var commitColumn = row[commitLineKey];
					var insertColumn = 0;
					for (var i=linesOfLastRow.length-1; i>=0; i--) {
						var lineOfLastRowKey = linesOfLastRow[i];
						var lineOfLastRow = fromKey(lineOfLastRowKey);
						if (lineOfLastRow[0] == lineOfLastRow[1]) {
							var found = false;
							for (var j in parents) {
								var parent = parents[j];
								var lineKey = toKey([rowIndex-1, parent]);
								var lineColumn = row[lineKey];
								if (lineColumn != undefined) {
									if (lineColumn<commitColumn) {
										found = true;
										break;
									}
								} else {
									lineKey = toKey([rowIndex-1, parent*-1]);
									lineColumn = row[lineKey];
									if (lineColumn != undefined && lineColumn<commitColumn) {
										found = true;
										break;
									}
								}
							}
							if (found) {
								insertColumn = i;
								break;
							}
						} else {
							var lineColumn = row[lineOfLastRowKey];
							if (lineColumn!=undefined && lineColumn<commitColumn) {
								insertColumn = i;
								break;
							}
						}
					}
					column = insertColumn+1;
					for (var i in cuttedLines) 
						lastRow[toKey(cuttedLines[i])] = column++;
					for (var i=insertColumn+1; i<linesOfLastRow.length; i++) 
						lastRow[linesOfLastRow[i]] = column++;
				}
			}
			rows.push(row);
		}
		for (var i=1; i<rows.length; i++) {
			var row = rows[i];
			var lastRow = rows[i-1];
			var lines = getSortedLines(row, false);
			var lastLines = getSortedLines(lastRow, true);
			
			var recycledColors = [];
			
			for (var j in lines) {
				var line = fromKey(lines[j]);
				if (line[0] == line[1]) {
					for (var k in lastLines) {
						var lastLine = fromKey(lastLines[k]);
						if (lastLine[0] == lastLine[1]) {
							if (commits[i-1].indexOf(i) != -1) 
								recycledColors.push(assignColor(toKey([i-1, i])));
						} else if (lastLine[1] == i) {
							recycledColors.push(assignColor(toKey(lastLine)));
						} else if (lastLine[1]*-1 == i) {
							recycledColors.push(assignColor(toKey([lastLine[0], i])));
						}
					}
				} else {
					if (line[1] < 0) 
						assignColor(toKey([line[0], line[1]*-1]));
					else
						assignColor(toKey(line));
				}
			}
			for (var j in recycledColors)
				colorStack.push(recycledColors[j]);
		}
		gitplex.repocommits.rows = rows;
		gitplex.repocommits.commits = commits; 
		gitplex.repocommits.parent2children = parent2children;
		gitplex.repocommits.colors = colors;
		
		gitplex.repocommits.drawCommitLane();
	},
	drawCommitLane: function() {
		var columnWidth = 12;
		var topOffset = 22;
		var rightOffset = 4;
		var commitSize = 4;
		
		var commitClass = "commit-lane-dot";
		var lineClass = "commit-lane-line";

		var rows = gitplex.repocommits.rows;
		var commits = gitplex.repocommits.commits; 
		var parent2children = gitplex.repocommits.parent2children;
		var colors = gitplex.repocommits.colors;

		var maxColumns = 0;
		for (var i in rows) {
			var columns = Object.keys(rows[i]).length;
			if (columns > maxColumns) 
				maxColumns = columns; 
		}
		
		function fromKey(lineKey) {
			return lineKey.split(',').map(function(item) {
				return parseInt(item, 10);
			});
		}
		
		function toKey(line) {
			return line[0] + "," + line[1];
		}
		
		var bodyTop = $("#repo-commits>.body").offset().top - topOffset;
		
		function getTop(row) {
			return $(".commitindex-"+row).offset().top - bodyTop;
		}
		
		function getLeft(column) {
			return column*columnWidth + columnWidth/2;
		}
		
		var $list = $("#repo-commits>.body>.list"); 
		$list.css("margin-left", maxColumns*columnWidth+rightOffset);
		
		var $lane = $("#repo-commits>.body>.lane"); 
		$lane.empty();
		$lane.height($list.outerHeight());
		$lane.width(maxColumns*columnWidth);
		var paper = Snap($lane[0]);
		
		for (var i=0; i<commits.length; i++) {
			var parents = commits[i];
			var row = rows[i];
			
			var column = row[toKey([i, i])];
			var left = getLeft(column);
			var top = getTop(i);
			var circle = paper.circle(left, top, commitSize);
			circle.addClass(commitClass);
			if (parents.length != 0) {
				circle.addClass(commitClass + colors[toKey([i, parents[0]])]);
			} else {
				var children = parent2children[i];
				if (children && children.length != 0)
					circle.addClass(commitClass + colors[toKey([children[0], i])]);
			}
			if (i != commits.length-1) {
				var nextRow = rows[i+1];
				var nextTop = getTop(i+1);
				for (var lineKey in row) {
					var line = fromKey(lineKey);
					column = row[lineKey];

					function drawLine(nextColumn, color, upArrow, downArrow) {
						if (nextColumn != undefined) {
							var arrowOffset = 10;
							var arrowWidth = 4;
							var arrowHeight = 8;
							var left = getLeft(column);
							var nextLeft = getLeft(nextColumn);
							var line = paper.line(left, top, nextLeft, nextTop);
							line.addClass(lineClass);
							line.addClass(lineClass + color);
							var arrow;
							if (upArrow) {
								arrow = paper.path("M" + left + " " + top + "l0 -" + arrowOffset 
										+ "l-" + arrowWidth + " " + arrowHeight + "m" + arrowWidth*2 
										+ " 0l-" + arrowWidth + " -" + arrowHeight);
							} else if (downArrow) {
								arrow = paper.path("M" + nextLeft + " " + nextTop + "l0 " + arrowOffset 
										+ "l-" + arrowWidth + " -" + arrowHeight + "m" + arrowWidth*2 
										+ " 0l-" + arrowWidth + " " + arrowHeight);
							}
							if (arrow) {
								arrow.addClass(lineClass);
								arrow.addClass(lineClass + color);
							}
						}
					}
					
					if (line[0] == line[1]) {
						for (var j in parents) {
							var parent = parents[j];
							if (parent == i+1) {
								drawLine(nextRow[toKey([i+1, i+1])], colors[toKey([i, i+1])]);
							} else {
								var nextLineKey = toKey([i, parent]);
								var nextColumn = nextRow[nextLineKey];
								if (nextColumn != undefined) { 
									drawLine(nextColumn, colors[nextLineKey], false, false);
								} else {
									var nextCuttedLineKey = toKey([i, -1*parent]);
									drawLine(nextRow[nextCuttedLineKey], colors[nextLineKey], false, true);
								}
							}
						}
					} else {
						if (line[1] < 0) {
							if (line[1]*-1 == i+1) {
								var nextColumn = nextRow[toKey([i+1, i+1])];
								drawLine(nextColumn, colors[toKey([line[0], i+1])], true, false);
							}
						} else if (line[1] == i+1){
							var nextColumn = nextRow[toKey([i+1, i+1])];
							drawLine(nextColumn, colors[lineKey], false, false);
						} else {
							drawLine(nextRow[lineKey], colors[lineKey], false, false);
						}
					}
				}
			}
		}
	}
	
}