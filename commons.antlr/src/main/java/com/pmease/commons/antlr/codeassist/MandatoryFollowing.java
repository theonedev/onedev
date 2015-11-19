package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

public class MandatoryFollowing {
	
	private List<String> literals = new ArrayList<>();
	
	private int nextEdit;

	public List<String> getLiterals() {
		return literals;
	}

	public void setLiterals(List<String> literals) {
		this.literals = literals;
	}

	public int getNextEdit() {
		return nextEdit;
	}

	public void setNextEdit(int nextEdit) {
		this.nextEdit = nextEdit;
	}
	
}
