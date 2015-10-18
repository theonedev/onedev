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
	
	private final Map<Line, String> colorAssignments = new HashMap<>();

	public CommitLane(List<Commit> commits, List<String> colors) {
		Map<String, Integer> commitRowIndexes = new HashMap<>();
		for (int i=0; i<commits.size(); i++) {
			Commit commit = commits.get(i);
			commitRowIndexes.put(commit.getHash(), i);
		}
		
		for (int rowIndex=0; rowIndex<commits.size(); rowIndex++) {
			Map<Line, Integer> row = new HashMap<>();
			if (rowIndex == 0) {
				row.put(new Line(0, 0), 0);
			} else {
				// special line represents the commit at rowIndex itself
				Line commitLine = new Line(rowIndex, rowIndex);
				final Map<Line, Integer> lastRow = rows.get(rowIndex-1);
				List<Line> linesOfLastRow = new ArrayList<>(lastRow.keySet());

				// examine lines of last row from left to right, so we sort them
				Collections.sort(linesOfLastRow, new Comparator<Line>() {

					@Override
					public int compare(Line line1, Line line2) {
						return lastRow.get(line1) - lastRow.get(line2);
					}
					
				});
				int column = 0;
				for (int columnOfLastRow = 0; columnOfLastRow<linesOfLastRow.size(); columnOfLastRow++) {
					Line lineOfLastRow = linesOfLastRow.get(columnOfLastRow);
					
					if (lineOfLastRow.childRowIndex != lineOfLastRow.parentRowIndex) {
						// line not started from last row, in this case, the line 
						// only occupies a column when it goes through current row 
						if (lineOfLastRow.parentRowIndex == rowIndex) { 
							if (!row.containsKey(commitLine))
								row.put(commitLine, column++);
						} else { 
							row.put(lineOfLastRow, column++);
						}
					} else {
						// determine columns for lines starting from last row is a bit complicated, 
						// if commit of last row has N parents, we have to assign N columns at 
						// current row, the order of these columns matters: to minimize line 
						// crossovers (at least for these parents), we calculate score of each line 
						// and use the score to order the line
						Commit lastCommit = commits.get(rowIndex-1);
						final Map<Line, Integer> lineScores = new HashMap<>();
						for (String parentHash: lastCommit.getParentHashes()) {
							Integer parentRowIndex = commitRowIndexes.get(parentHash);
							if (parentRowIndex != null) {
								Line line;
								if (parentRowIndex.intValue() == rowIndex) {
									if (!row.containsKey(commitLine))
										line = commitLine;
									else
										line = null;
								} else {
									line = new Line(rowIndex-1, parentRowIndex);
								}
								if (line != null) {
									int score = 0;
									for (int i=0; i<columnOfLastRow; i++) {
										// minus score by one if there is a line pulling current line 
										// from left
										if (linesOfLastRow.get(i).parentRowIndex == line.parentRowIndex)
											score--;
									}
									for (int i=columnOfLastRow+1; i<linesOfLastRow.size(); i++) {
										// plus score by one if there is a line pulling current line 
										// from right
										if (linesOfLastRow.get(i).parentRowIndex == line.parentRowIndex)
											score++;
									}
									// normalize the score so we can use score subtraction to arrange the 
									// order later
									if (score > 0)
										score = 1;
									else if (score < 0)
										score = -1;
									lineScores.put(line, score);
								}
							}
						}
						List<Line> lines = new ArrayList<>(lineScores.keySet());
						Collections.sort(lines, new Comparator<Line>() {

							@Override
							public int compare(Line line1, Line line2) {
								int score1 = lineScores.get(line1);
								int score2 = lineScores.get(line2);
								if (score1 != score2) 
									// put the line at the side where there is a pull from that side
									return score1 - score2; 
								else if (score1>0)
									// if both lines are pulled from right side, we put longer line at left side, 
									// otherwise there will exist a line crossover
									return line2.parentRowIndex - line1.parentRowIndex;
								else
									// if both lines are pulled from left side, we put shorter line at left side, 
									// otherwise there will exist a line crossover. Additionally, if both lines
									// have score of 0, we put shorter line at left side in order to make commit
									// points at left side as possible as we can
									return line1.parentRowIndex - line2.parentRowIndex;
							}
							
						});
						for (Line line: lines)
							row.put(line, column++);
					}
				}
				if (!row.containsKey(commitLine))
					row.put(commitLine, column++);
			}
			rows.add(row);
		}
		
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
		
		private final int childRowIndex;
		
		private final int parentRowIndex;
		
		public Line(int commitRowIndex, int parentRowIndex) {
			this.childRowIndex = commitRowIndex;
			this.parentRowIndex = parentRowIndex;
		}
		
		public int getChildRowIndex() {
			return childRowIndex;
		}

		public int getParentRowIndex() {
			return parentRowIndex;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Line))
				return false;
			if (this == other)
				return true;
			Line otherCell = (Line) other;
			return new EqualsBuilder()
					.append(childRowIndex, otherCell.childRowIndex)
					.append(parentRowIndex, otherCell.parentRowIndex)
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37)
					.append(childRowIndex)
					.append(parentRowIndex)
					.toHashCode();
		}

		@Override
		public String toString() {
			return childRowIndex+","+parentRowIndex;
		}
		
	}
}
