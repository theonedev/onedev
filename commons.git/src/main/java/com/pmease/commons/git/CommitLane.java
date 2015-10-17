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
			Commit commit = commits.get(rowIndex);
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
				for (Column columnOfLastRow: columnsOfLastRow) {
					if (columnOfLastRow.commitRowIndex != columnOfLastRow.parentRowIndex) {
						if (columnOfLastRow.parentRowIndex == rowIndex) { 
							Column column = new Column(rowIndex, rowIndex);
							if (!row.containsKey(column))
								row.put(column, columnIndex++);
						} else { 
							row.put(new Column(columnOfLastRow), columnIndex++);
						}
					} else {
						List<Column> columns = new ArrayList<>();
						for (String parentHash: commit.getParentHashes()) {
							Integer parentRowIndex = commitRowIndexes.get(parentHash);
							if (parentRowIndex != null) {
								if (parentRowIndex.intValue() == rowIndex) {
									Column column = new Column(rowIndex, rowIndex);
									if (!row.containsKey(column))
										columns.add(column);
								} else {
									columns.add(new Column(rowIndex-1, parentRowIndex));
								}
							}
						}
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
