gitplex.repocommits = {
		
	/*
	 * commits is an array of ordered commit object, and the commit object is itself a 
	 * list of parent indexes
	 */
	drawCommitLane: function(commits) {

		var maxColumns = 20;
		var colors = [];
			
		function getSortedLines(row, reverse) {
			var lines = [];
			for (var line in Object.keys(row))
				lines.push(line);

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
		
		/*
		 * rows store the map of line to row. A line represents a child->parent relationship. 
		 * For instance line(1,5) represents the line from commit at row 1 (the child ) to 
		 * commit at row 5 (the parent). In case child row index equals parent row index, 
		 * the line represents a commit. 
		 * 
		 */
		var rows = [];
		
		var parent2children = {}; 
		
		for (var i=0; i<commits.length; i++) {
			var commit = commits[i];
			for (var j=0; j<commit.length; j++) {
				var parent = commit[j];
				var children = parent2children[parent];
				if (!children) {
					children = [];
					parent2children[parent] = children;
				}
				children.push(i);
			}
		}
		
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
							if (!row[commitLineKey])
								row[commitLineKey] = column++;
						} else { 
							row[toKey(lineOfLastRow)] = column++;
						}
					} else {
						for (var j=0; j<commit.length; j++) {
							var parent = commit[j];
							if (parent == rowIndex) {
								if (!row[commitLineKey])
									row[commitLineKey] = column++;
							} else {
								row[toKey([rowIndex-1, parent])] = column++;
							}
						}
					}
				}
				
				if (!row[commitLineKey])
					row[commitLineKey] = column++;
				if (column > maxColumns) {
					var reverseLines = getSortedLines(row, true);
					for (var i=0; i<reversedLines.length; i++) {
						var line = reversedLines[i];
						if (line[0] == rowIndex-1) {
							var cuttedLine = [line[0], line[1]*-1];
							var lineKey = toKey(line);
							var lineColumn = row.get(lineKey);
							delete row[lineKey];
							row.put(toKey(cuttedLine), lineColumn);
							column--;
							if (column == maxColumns)
								break;
						}
					}
					if (column != maxColumns)
						throw "Illegal state";
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
							if (!lastRow[lineKey]) {
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
					for (var i=insertColumn+1; i<linesOfLastRow.size(); i++) 
						lastRow[linesOfLastRow[i]] = column++;
				}
			}
			rows.push(row);
		}
	}
	
}