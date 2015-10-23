gitplex.repocommits = {
		
	/*
	 * commits is an array of ordered commit object, and the commit object is itself a 
	 * list of parent indexes
	 */
	drawCommitLane: function(commits) {

		function getSortedLines(row, boolean reverse) {
			var lines = [];
			for (var line in row.keys())
				lines.add(line);

			lines.sort(function(x, y) {
				if (reverse)
					return row[y] - row[x];
				else
					return row[x] - row[y];
			});
			return lines;
		}
		
		function fromKey(lineKey) {
			return line.split(',').map(function(item) {
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
				children.add(i);
			}
		}
		
		for (int rowIndex=0; rowIndex<commits.length; rowIndex++) {
			var row = {};
			if (rowIndex == 0) {
				row["0,0"] = 0;
			} else {
				// special line represents the commit at rowIndex itself
				var commitLineKey = toKeyrowIndex + "," + rowIndex;
				var lastRow = rows[rowIndex-1];
				var column = 0;
				var linesOfLastRow = getSortedLines(lastRow, false);
				for (var i=0; i<linesOfLastRow.length; i++) {
					var lineOfLastRow = fromKey(linesOfLastRows[i]);
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
						var commit = commits[rowIndex-1];
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
				
				=================================================
					
				if (!row.containsKey(commitLine))
					row.put(commitLine, column++);
				if (column > maxColumns) {
					for (Line line: getSortedLines(row, true)) {
						if (line[0] == rowIndex-1) {
							row.put(line.toggle(), row.remove(line));
							column--;
							if (column == maxColumns)
								break;
						}
					}
					Preconditions.checkState(column == maxColumns);
				}
				
				List<Line> cuttedLines = new ArrayList<>();
				List<Integer> children = mapOfParentToChildren.get(rowIndex);
				if (children != null) {
					for (int child: children) {
						if (child != rowIndex-1) {
							Line line = new Line(child, rowIndex);
							Line cuttedLine = line.toggle();
							if (!lastRow.containsKey(line)) {
								if (lastRow.containsKey(cuttedLine))
									lastRow.put(line, lastRow.remove(cuttedLine));
								else 
									cuttedLines.add(cuttedLine);
							}
						}
					}
				}
				if (!cuttedLines.isEmpty()) {
					// for every disappeared line, we need to make them appear again in last row
					// so that end part of the line can be drawn from last row to this row. 
					// Below code find column in last row to insert these appeared lines, and 
					// we want to make sure that this column can result in minimum line crossovers. 
					int commitColumn = row.get(commitLine);
					int insertColumn = 0;
					for (int i=linesOfLastRow.size()-1; i>=0; i--) {
						Line lineOfLastRow = linesOfLastRow.get(i);
						if (lineOfLastRow.isCommit()) {
							boolean found = false;
							for (String parentHash: commits.get(rowIndex-1).getParentHashes()) {
								Integer parent = mapOfHashToRow.get(parentHash);
								if (parent != null) {
									Line line = new Line(rowIndex-1, parent);
									Integer lineColumn = row.get(line);
									if (lineColumn!=null && lineColumn.intValue()<commitColumn) {
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
							Integer lineColumn = row.get(lineOfLastRow);
							if (lineColumn!=null && lineColumn.intValue()<commitColumn) {
								insertColumn = i;
								break;
							}
						}
					}
					column = insertColumn+1;
					for (Line cuttedLine: cuttedLines) 
						lastRow.put(cuttedLine, column++);
					for (int i=insertColumn+1; i<linesOfLastRow.size(); i++) 
						lastRow.put(linesOfLastRow.get(i), column++);
				}
			}
			rows.add(row);
		}
	}
	
}