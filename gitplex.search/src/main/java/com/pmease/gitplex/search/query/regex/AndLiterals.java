package com.pmease.gitplex.search.query.regex;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class AndLiterals extends LogicalLiterals {

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
			List<List<LeafLiterals>> rows = new ArrayList<>();
			
			List<List<LeafLiterals>> column1 = getElements().get(0).flattern(false); 
			List<List<LeafLiterals>> column2 = new AndLiterals(getElements().subList(1, getElements().size())).flattern(false);
			for (List<LeafLiterals> rowOfColumn1: column1) {
				for (List<LeafLiterals> rowOfColumn2: column2) {
					List<LeafLiterals> joinedRow = new ArrayList<>(rowOfColumn1);
					joinedRow.addAll(rowOfColumn2);
					rows.add(LiteralUtils.merge(joinedRow, outmost));
				}
			}
			// we must add trim here, otherwise, the resulting production will be increased exponentially
			LiteralUtils.trim(rows, outmost);
			return rows;
		}
	}

}
