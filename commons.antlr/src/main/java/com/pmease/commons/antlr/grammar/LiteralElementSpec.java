package com.pmease.commons.antlr.grammar;

import java.util.Set;

import com.google.common.collect.Lists;
import com.pmease.commons.antlr.codeassist.MandatoryScan;

public class LiteralElementSpec extends TerminalElementSpec {

	private static final long serialVersionUID = 1L;
	
	private final int tokenType;
	
	private final String literal;
	
	public LiteralElementSpec(String label, Multiplicity multiplicity, 
			int tokenType, String literal) {
		super(label, multiplicity);
		
		this.tokenType = tokenType;
		this.literal = literal;
	}

	public String getLiteral() {
		return literal;
	}

	@Override
	public MandatoryScan scanMandatories(Set<String> checkedRules) {
		return new MandatoryScan(Lists.newArrayList(literal), false);
	}

	@Override
	protected String toStringOnce() {
		return "'" + literal + "'";
	}

	@Override
	public boolean isToken(int tokenType) {
		return tokenType == this.tokenType;
	}
	
}
