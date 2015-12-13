package com.pmease.commons.antlr.grammar;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.pmease.commons.antlr.Grammar;
import com.pmease.commons.antlr.codeassist.MandatoryScan;

public class LexerRuleRefElementSpec extends TerminalElementSpec {

	private static final long serialVersionUID = 1L;

	private final Grammar grammar;
	
	private final int tokenType;
	
	private final String ruleName;
	
	private transient Optional<RuleSpec> rule;
	
	public LexerRuleRefElementSpec(Grammar grammar, String label, Multiplicity multiplicity, 
			int tokenType, String ruleName) {
		super(label, multiplicity);
		
		this.grammar = grammar;
		this.tokenType = tokenType;
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
	public MandatoryScan scanMandatories(Set<String> checkedRules) {
		if (getRule() != null) 
			return getRule().scanMandatories(checkedRules);
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
	public boolean isToken(int tokenType) {
		return tokenType == this.tokenType;
	}

	@Override
	public Set<String> getFirstSet(Set<String> checkedRules) {
		Set<String> firstSet = new HashSet<>();
		RuleSpec rule = getRule();
		if (rule != null && !checkedRules.contains(getRuleName())) {
			checkedRules.add(getRuleName());
			for (AlternativeSpec alternative: rule.getAlternatives()) {
				for (ElementSpec elementSpec: alternative.getElements()) {
					TerminalElementSpec terminalElementSpec = (TerminalElementSpec) elementSpec;
					firstSet.addAll(terminalElementSpec.getFirstSet(new HashSet<>(checkedRules)));
					if (!terminalElementSpec.matchesEmpty())
						break;
				}
			}
		}
		return firstSet;
	}
	
}
