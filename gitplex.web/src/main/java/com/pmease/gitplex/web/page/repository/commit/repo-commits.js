gitplex.repocommits = {		
	/*
	 * commits is an array of ordered commit object, and the commit object is itself a 
	 * list of parent indexes
	 */
	renderCommitLane: function(commits) {
		var columnsLimit = 20;
		
		var colors = ["#0000ff", "#00ff00", "#ff0000",
		              "#ffff00", "#ff00ff", "#00ffff",
		              "#000080", "#008000", "#800000", 
		              "#808000", "#800080", "#008080"];

		/*
		 * rows store the map of line to row. A line represents a child->parent relationship. 
		 * For instance line(1,5) represents the line from commit at row 1 (the child ) to 
		 * commit at row 5 (the parent). In case child row index equals parent row index, 
		 * the line represents a commit. 
		 * 
		 */
		var rows = [];
		var parent2children = {};
		var colorAssignments = {};
		
		var colorStack = [];
		function getSortedLines(row, reverse) {
			var lines = [];
			var keys = Object.keys(row);
			for (var i=0; i<keys.length; i++)
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
			var color = colorAssignments[lineKey];
			if (color == undefined) {
				if (colorStack.length == 0) {
					for (var i=colors.length-1; i>=0; i--)
						colorStack.push(colors[i]);
				}
				color = colorStack.pop();
				colorAssignments[lineKey] = color;
			}
			return color;
		}
		
		for (var i=0; i<commits.length; i++) {
			commits[i] = commits[i][1];
			var parents = commits[i];
			for (var j=0; j<parents.length; j++) {
				var parent = parents[j];
				var children = parent2children[parent];
				if (children == undefined) {
					children = [];
					parent2children[parent] = children;
				}
				children.push(i);
			}
		}
		
		var maxColumns = 1;
		for (var rowIndex=0; rowIndex<commits.length; rowIndex++) {
			var row = {};
			if (rowIndex == 0) {
				row["0,0"] = 0;
			} else {
				var commit = commits[rowIndex-1];
				// special line represents the commit at rowIndex itself
				var commitLineKey = toKey([rowIndex, rowIndex]);
				var lastRow = rows[rowIndex-1];
				var column = 0;
				var linesOfLastRow = getSortedLines(lastRow, false);
				for (var i=0; i<linesOfLastRow.length; i++) {
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
						for (var j=0; j<commit.length; j++) {
							var parent = commit[j];
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
					for (var i=0; i<reversedLines.length; i++) {
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
					for (var i=0; i<children.length; i++) {
						var child = children[i];
						if (child != rowIndex-1) {
							var line = [child, rowIndex];
							var cuttedLine = [child, rowIndex*-1];
							var lineKey = toKey(line);
							var cuttedLineKey = toKey(cuttedLine);
							if (lastRow[lineKey] == undefined) {
								if (lastRow[cuttedLineKey]) {
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
							for (var j=0; j<commit.lenght; j++) {
								var parent = commit[j];
								var line = [rowIndex-1, parent];
								var lineKey = toKey(line);
								var lineColumn = row[lineKey];
								if (lineColumn && lineColumn<commitColumn) {
									found = true;
									break;
								}
							}
							if (found) {
								insertColumn = i;
								break;
							}
						} else {
							var lineColumn = row[lineOfLastRowKey];
							if (lineColumn && lineColumn<commitColumn) {
								insertColumn = i;
								break;
							}
						}
					}
					column = insertColumn+1;
					for (var i=0; i<cuttedLines.length; i++) 
						lastRow[toKey(cuttedLines[i])] = column++;
					for (var i=insertColumn+1; i<linesOfLastRow.length; i++) 
						lastRow[linesOfLastRow[i]] = column++;
				}
				for (var lineKey in row) {
					var line = fromKey(lineKey);
					if (line.child == line.parent) {
						var children = parent2children[line.parent];
						if (children) {
							for (var i=0; i<children.length; i++) {
								var child = children[i];
								var lineToMe = [child, line.parent];
								colorStack.push(assignColor(toKey(lineToMe)));
							}
						}
					} else {
						if (line.parent < 0) 
							assignColor(toKey([line.child, line.parent*-1]));
						else
							assignColor(toKey(line));
					}
				}
			}
			rows.push(row);
			var keysLen = Object.keys(row).length;
			if (keysLen > maxColumns) 
				maxColumns = keysLen; 
		}
		gitplex.repocommits.rows = rows;
		gitplex.repocommits.commits = commits; 
		gitplex.repocommits.colorAssignments = colorAssignments;
		gitplex.repocommits.maxColumns = maxColumns;
		
		gitplex.repocommits.drawCommitLane();
	},
	drawCommitLane: function() {
		var columnWidth = 20;
		var topOffset = 12;
		var rightOffset = 8;
		var commitSize = 3;
		var commitColor = "#0000ff";

		var rows = gitplex.repocommits.rows;
		var commits = gitplex.repocommits.commits; 
		var colorAssignments = gitplex.repocommits.colorAssignments;
		var maxColumns = gitplex.repocommits.maxColumns;
		
		function fromKey(lineKey) {
			return lineKey.split(',').map(function(item) {
				return parseInt(item, 10);
			});
		}
		
		function toKey(line) {
			return line[0] + "," + line[1];
		}
		
		var $list = $("#repo-commits>.body>.list"); 
		$list.css("margin-left", maxColumns*columnWidth+8);
		
		var $lane = $("#repo-commits>.body>.lane"); 
		$lane.empty();
		$lane.height($list.outerHeight());
		$lane.width(maxColumns*columnWidth);
		var paper = Snap($lane[0]);
		
		var bodyTop = $("#repo-commits>.body").offset().top - rightOffset;
		var commitGroup = paper.g();
		for (var i=0; i<rows.length; i++) {
			var row = rows[i];
			var column = row[toKey([i, i])];
			var top = $("#commitindex-"+i).offset().top - bodyTop;
			var left = column*columnWidth + columnWidth/2;
			commitGroup.add(paper.circle(left, top, commitSize));
			
			if (i < rows.length-1) {
				
			}
		}
		commitGroup.attr({fill: commitColor, stroke: commitColor});
	}
	
}