package com.pmease.commons.antlr.grammar;

import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.pmease.commons.antlr.codeassist.MandatoryScan;

public class LexerRuleRefElementSpec extends TokenElementSpec {

	private static final long serialVersionUID = 1L;

	private final Grammar grammar;
	
	private final String ruleName;
	
	private transient Optional<RuleSpec> rule;
	
	public LexerRuleRefElementSpec(Grammar grammar, String label, Multiplicity multiplicity, 
			int tokenType, String ruleName) {
		super(label, multiplicity, tokenType);
		
		this.grammar = grammar;
		this.ruleName = ruleName;
	}

	public String getRuleName() {
		return ruleName;
	}
	
	public RuleSpec getRule() {
		if (rule == null)
			rule = Optional.fromNullable(grammar.getRule(ruleName));
		return rule.orNull();
	}

	@Override
	public MandatoryScan scanMandatories() {
		if (getRule() != null) 
			return getRule().scanMandatories();
		else 
			return MandatoryScan.stop();
	}

	@Override
	protected String toStringOnce() {
		if (grammar.isBlockRule(ruleName))
			return "(" + Preconditions.checkNotNull(getRule()) + ")";
		else 
			return ruleName;
	}

	@Override
	public Set<String> getLeadingLiterals() {
		if (getRule() != null)
			return getRule().getLeadingLiterals();
		else
			return Sets.newLinkedHashSet();
	}

	@Override
	protected boolean isAllowEmptyOnce() {
		if (getRule() != null)
			return getRule().isAllowEmpty();
		else
			return false;
	}
	
}
