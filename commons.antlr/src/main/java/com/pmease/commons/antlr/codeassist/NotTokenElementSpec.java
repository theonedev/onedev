package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

public class NotTokenElementSpec extends TerminalElementSpec {

	private static final long serialVersionUID = 1L;
	
	private final Grammar grammar;
	
	private final Set<Integer> notTokenTypes;
	
	public NotTokenElementSpec(Grammar grammar, String label, 
			Multiplicity multiplicity, Set<Integer> notTokenTypes) {
		super(label, multiplicity);
		
		this.grammar = grammar;
		this.notTokenTypes = notTokenTypes;
	}

	public Set<Integer> getNotTokenTypes() {
		return notTokenTypes;
	}

	@Override
	public MandatoryScan scanMandatories(Set<String> checkedRules) {
		return MandatoryScan.stop();
	}

	@Override
	protected String toStringOnce() {
		List<String> notTokenNames = new ArrayList<>();
		for (int notTokenType: notTokenTypes) 
			notTokenNames.add(Preconditions.checkNotNull(grammar.getTokenNameByType(notTokenType)));
		return StringUtils.join(notTokenNames, " ");
	}

	@Override
	public boolean isToken(int tokenType) {
		return !notTokenTypes.contains(tokenType);
	}

}
