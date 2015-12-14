package com.pmease.commons.antlr.grammar;

import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.Token;

import com.google.common.base.Preconditions;
import com.pmease.commons.antlr.codeassist.MandatoryScan;
import com.pmease.commons.antlr.parser.EarleyParser;

public class RuleRefElementSpec extends ElementSpec {

	private static final long serialVersionUID = 1L;

	private final Grammar grammar;
	
	private final String ruleName;
	
	private transient RuleSpec rule;
	
	public RuleRefElementSpec(Grammar grammar, String label, Multiplicity multiplicity, String ruleName) {
		super(label, multiplicity);
	
		this.grammar = grammar;
		this.ruleName = ruleName;
	}

	public RuleSpec getRule() {
		if (rule == null)
			rule = Preconditions.checkNotNull(grammar.getRule(ruleName));
		return rule;
	}
	
	public String getRuleName() {
		return ruleName;
	}

	@Override
	public MandatoryScan scanMandatories() {
		return getRule().scanMandatories();
	}

	@Override
	protected String toStringOnce() {
		if (grammar.isBlockRule(ruleName))
			return "(" + Preconditions.checkNotNull(getRule()) + ")";
		else 
			return ruleName;
	}

	@Override
	public int getEndOfMatch(List<Token> tokens) {
		return new EarleyParser(getRule(), tokens).getEndOfMatch();
	}

	@Override
	public Set<String> getLeadingLiterals() {
		return getRule().getLeadingLiterals();
	}

	@Override
	protected boolean isAllowEmptyOnce() {
		return getRule().isAllowEmpty();
	}
	
}
