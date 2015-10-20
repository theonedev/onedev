package com.pmease.commons.git;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
public class CommitLane implements Serializable {
	
	/*
	 * Each list item represents a row, and each row represents a map of 
	 * line to its drawn column at this row. With this info, one can 
	 * go through each row to draw every child->parent line. Taking 
	 * line (2,4) for instance, we first find commit column at row 2 
	 * (by looking up the special line (2,2)), then find its column 
	 * at row 3, and then find commit column at row 4 (by looking up the 
	 * special line (4,4)). Finally connect these columns we got the 
	 * line drawn.
	 */
	private final List<Map<Line, Integer>> rows = new ArrayList<>();
	
	public CommitLane(List<Commit> commits, int maxColumns) {
		Preconditions.checkArgument(!commits.isEmpty() && maxColumns>=1);
		
		Map<String, Integer> mapOfHashToRow = new HashMap<>();
		Map<Integer, List<Integer>> mapOfParentToChildren = new HashMap<>(); 
		for (int i=0; i<commits.size(); i++) {
			Commit commit = commits.get(i);
			mapOfHashToRow.put(commit.getHash(), i);
		}
		for (int i=0; i<commits.size(); i++) {
			Commit commit = commits.get(i);
			for (String parentHash: commit.getParentHashes()) {
				Integer parent = mapOfHashToRow.get(parentHash);
				if (parent != null) {
					List<Integer> children = mapOfParentToChildren.get(parent);
					if (children == null) {
						children = new ArrayList<>();
						mapOfParentToChildren.put(parent, children);
					}
					children.add(i);
				}
			}
		}
		
		for (int rowIndex=0; rowIndex<commits.size(); rowIndex++) {
			Map<Line, Integer> row = new HashMap<>();
			if (rowIndex == 0) {
				row.put(new Line(0, 0), 0);
			} else {
				// special line represents the commit at rowIndex itself
				Line commitLine = new Line(rowIndex, rowIndex);
				final Map<Line, Integer> lastRow = rows.get(rowIndex-1);
				int column = 0;
				List<Line> linesOfLastRow = getSortedLines(lastRow, false);
				for (Line lineOfLastRow: linesOfLastRow) {
					if (lineOfLastRow.isCutted()) // line is cutted due to max columns limitation
						continue;
					if (!lineOfLastRow.isCommit()) {
						// line not started from last row, in this case, the line 
						// only occupies a column when it goes through current row 
						if (lineOfLastRow.parent == rowIndex) { 
							if (!row.containsKey(commitLine))
								row.put(commitLine, column++);
						} else { 
							row.put(lineOfLastRow, column++);
						}
					} else {
						for (String parentHash: commits.get(rowIndex-1).getParentHashes()) {
							Integer parent = mapOfHashToRow.get(parentHash);
							if (parent != null) {
								if (parent.intValue() == rowIndex) {
									if (!row.containsKey(commitLine))
										row.put(commitLine, column++);
								} else {
									row.put(new Line(rowIndex-1, parent), column++);
								}
							}
						}
					}
				}
				if (!row.containsKey(commitLine))
					row.put(commitLine, column++);
				if (column > maxColumns) {
					for (Line line: getSortedLines(row, true)) {
						if (line.child == rowIndex-1) {
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
	
	private List<Line> getSortedLines(final Map<Line, Integer> row, boolean reverse) {
		List<Line> lines = new ArrayList<>(row.keySet());
		Collections.sort(lines, new Comparator<Line>() {

			@Override
			public int compare(Line line1, Line line2) {
				return row.get(line1) - row.get(line2);
			}
			
		});
		if (reverse)
			Collections.reverse(lines);
		return lines;
	}
	
	public List<Map<Line, Integer>> getRows() {
		return rows;
	}

	/**
	 * A line represents a child->parent relationship. For instance line(1,5)
	 * represents the line from commit at row 1 (the child ) to commit at 
	 * row 5 (the parent). In case child row index equals parent row index, 
	 * the line represents a commit. 
	 * 
	 * @author robin
	 *
	 */
	public static class Line implements Serializable {
		
		private final int child;
		
		private final int parent;
		
		public Line(int child, int parent) {
			this.child = child;
			this.parent = parent;
		}
		
		public int getChild() {
			return child;
		}

		public int getParent() {
			return parent;
		}

		public boolean isCutted() {
			return parent < 0;
		}
		
		public boolean isCommit() {
			return child == parent;
		}
		
		public Line toggle() {
			return new Line(child, parent*-1);
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Line))
				return false;
			if (this == other)
				return true;
			Line otherLine = (Line) other;
			return new EqualsBuilder()
					.append(child, otherLine.child)
					.append(parent, otherLine.parent)
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37)
					.append(child)
					.append(parent)
					.toHashCode();
		}

		@Override
		public String toString() {
			return child+","+parent;
		}
		
	}
}
