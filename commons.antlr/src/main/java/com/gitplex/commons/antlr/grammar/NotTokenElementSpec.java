package com.gitplex.commons.antlr.grammar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.gitplex.commons.antlr.codeassist.MandatoryScan;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

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
	public MandatoryScan scanMandatories() {
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
	public Set<String> getPossiblePrefixes() {
		return Sets.newLinkedHashSet();
	}

	@Override
	protected boolean isAllowEmptyOnce() {
		return false;
	}

	@Override
	public boolean isToken(int tokenType) {
		return !notTokenTypes.contains(tokenType);
	}

}
