package io.onedev.server.search.code.query.regex;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class AndLiterals extends LogicalLiterals {

	// limit complexity of resulting product
	private static final int MAX_ROWS = 64;
	
	public AndLiterals(List<Literals> elements) {
		super(elements);
	}
	
	public AndLiterals(Literals...elements) {
		super(elements);
	}
	
	@Override
	public List<List<LeafLiterals>> flattern(boolean outmost) {
		if (getElements().size() == 0) {
			return Lists.newArrayList();
		} else if (getElements().size() == 1) {
			return getElements().get(0).flattern(outmost);
		} else {
			List<List<LeafLiterals>> column1 = getElements().get(0).flattern(false); 
			List<List<LeafLiterals>> column2 = new AndLiterals(getElements().subList(1, getElements().size())).flattern(false);
			
			List<List<LeafLiterals>> rows = new ArrayList<>();
			if (column1.size()*column2.size() <= MAX_ROWS) {
				for (List<LeafLiterals> rowOfColumn1: column1) {
					for (List<LeafLiterals> rowOfColumn2: column2) {
						List<LeafLiterals> joinedRow = new ArrayList<>(rowOfColumn1);
						joinedRow.addAll(rowOfColumn2);
						rows.add(LiteralUtils.merge(joinedRow, outmost));
					}
				}
			} else {
				if (getWeightOfRows(column1)>getWeightOfRows(column2)) {
					for (List<LeafLiterals> row: column1) {
						List<LeafLiterals> joinedRow = new ArrayList<>(row);
						joinedRow.add(new LeafLiterals(null));
						rows.add(LiteralUtils.merge(joinedRow, outmost));
					}
				} else {
					for (List<LeafLiterals> row: column2) {
						List<LeafLiterals> joinedRow = new ArrayList<>();
						joinedRow.add(new LeafLiterals(null));
						joinedRow.addAll(row);						
						rows.add(LiteralUtils.merge(joinedRow, outmost));
					}
				}
			}
			LiteralUtils.trim(rows, outmost);
			return rows;
		}
	}

	private long getWeightOfRow(List<LeafLiterals> row) {
		long weight = 1;
		for (LeafLiterals leaf: row) {
			if (!Strings.isNullOrEmpty(leaf.getLiteral()))
				weight *= leaf.getLiteral().length();
		}
		return weight;
	}

	private long getWeightOfRows(List<List<LeafLiterals>> rows) {
		long weight = Long.MAX_VALUE;
		for (List<LeafLiterals> row: rows) {
			long rowWeight = getWeightOfRow(row);
			if (rowWeight < weight)
				weight = rowWeight;
		}
		return weight;
	}
}
