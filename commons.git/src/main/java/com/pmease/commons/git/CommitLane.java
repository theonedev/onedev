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
	
	List<Map<Column, Integer>> rows = new ArrayList<>();

	public CommitLane(List<Commit> commits) {
		Map<String, Integer> commitRowIndexes = new HashMap<>();
		for (int i=0; i<commits.size(); i++) {
			Commit commit = commits.get(i);
			commitRowIndexes.put(commit.getHash(), i);
		}
		
		for (int rowIndex=0; rowIndex<commits.size(); rowIndex++) {
			Map<Column, Integer> row = new HashMap<>();
			if (rowIndex == 0) {
				row.put(new Column(0, 0), 0);
			} else {
				final Map<Column, Integer> lastRow = rows.get(rowIndex-1);
				List<Column> columnsOfLastRow = new ArrayList<>(lastRow.keySet());
				Collections.sort(columnsOfLastRow, new Comparator<Column>() {

					@Override
					public int compare(Column column1, Column column2) {
						return lastRow.get(column1) - lastRow.get(column2);
					}
					
				});
				int columnIndex = 0;
				for (int columnIndexOfLastRow = 0; columnIndexOfLastRow<columnsOfLastRow.size(); columnIndexOfLastRow++) {
					Column columnOfLastRow = columnsOfLastRow.get(columnIndexOfLastRow);
					if (columnOfLastRow.commitRowIndex != columnOfLastRow.parentRowIndex) {
						if (columnOfLastRow.parentRowIndex == rowIndex) { 
							Column column = new Column(rowIndex, rowIndex);
							if (!row.containsKey(column))
								row.put(column, columnIndex++);
						} else { 
							row.put(new Column(columnOfLastRow), columnIndex++);
						}
					} else {
						Commit lastCommit = commits.get(rowIndex-1);
						final Map<Column, Integer> columnScores = new HashMap<>();
						for (String parentHash: lastCommit.getParentHashes()) {
							Integer parentRowIndex = commitRowIndexes.get(parentHash);
							if (parentRowIndex != null) {
								Column column;
								if (parentRowIndex.intValue() == rowIndex) {
									column = new Column(rowIndex, rowIndex);
									if (row.containsKey(column))
										column = null;
								} else {
									column = new Column(rowIndex-1, parentRowIndex);
								}
								if (column != null) {
									int score = 0;
									for (int i=0; i<columnIndexOfLastRow; i++) {
										if (columnsOfLastRow.get(i).parentRowIndex == column.parentRowIndex)
											score--;
									}
									for (int i=columnIndexOfLastRow+1; i<columnsOfLastRow.size(); i++) {
										if (columnsOfLastRow.get(i).parentRowIndex == column.parentRowIndex)
											score++;
									}
									if (score > 0)
										score = 1;
									else if (score < 0)
										score = -1;
									columnScores.put(column, score);
								}
							}
						}
						List<Column> columns = new ArrayList<>(columnScores.keySet());
						Collections.sort(columns, new Comparator<Column>() {

							@Override
							public int compare(Column column1, Column column2) {
								int score1 = columnScores.get(column1);
								int score2 = columnScores.get(column2);
								if (score1 != score2)
									return score1 - score2;
								else if (score1>0)
									return column2.parentRowIndex - column1.parentRowIndex;
								else
									return column1.parentRowIndex - column2.parentRowIndex;
							}
							
						});
						for (Column column: columns)
							row.put(column, columnIndex++);
					}
				}
			}
			rows.add(row);
		}
	}
	
	static class Column implements Serializable {
		int commitRowIndex;
		
		int parentRowIndex;
		
		Column(int commitRowIndex, int parentRowIndex) {
			this.commitRowIndex = commitRowIndex;
			this.parentRowIndex = parentRowIndex;
		}
		
		Column(Column column) {
			this(column.commitRowIndex, column.parentRowIndex);
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Column))
				return false;
			if (this == other)
				return true;
			Column otherColumn = (Column) other;
			return new EqualsBuilder()
					.append(commitRowIndex, otherColumn.commitRowIndex)
					.append(parentRowIndex, otherColumn.parentRowIndex)
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37)
					.append(commitRowIndex)
					.append(parentRowIndex)
					.toHashCode();
		}
	}
}
