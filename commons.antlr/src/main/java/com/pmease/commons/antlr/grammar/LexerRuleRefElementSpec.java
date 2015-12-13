package com.pmease.commons.antlr.grammar;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.pmease.commons.antlr.Grammar;
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
	public Set<String> getLeadingLiterals(Set<String> checkedRules) {
		Set<String> leadingLiterals = new HashSet<>();
		RuleSpec rule = getRule();
		if (rule != null && !checkedRules.contains(getRuleName())) {
			checkedRules.add(getRuleName());
			for (AlternativeSpec alternative: rule.getAlternatives()) {
				for (ElementSpec elementSpec: alternative.getElements()) {
					if (elementSpec instanceof TokenElementSpec) {
						TokenElementSpec tokenElementSpec = (TokenElementSpec) elementSpec;
						leadingLiterals.addAll(tokenElementSpec.getLeadingLiterals(new HashSet<>(checkedRules)));
						if (!tokenElementSpec.matchesEmpty(new HashSet<String>()))
							break;
					} else if (!elementSpec.isOptional()) {
						break;
					}
				}
			}
		}
		return leadingLiterals;
	}

	@Override
	protected boolean matchesEmptyOnce(Set<String> checkedRules) {
		RuleSpec rule = getRule();
		if (rule != null && !checkedRules.contains(getRuleName())) {
			checkedRules.add(getRuleName());
			for (AlternativeSpec alternative: rule.getAlternatives()) {
				boolean matchesEmpty = true;
				for (ElementSpec elementSpec: alternative.getElements()) {
					if (elementSpec instanceof TokenElementSpec) {
						TokenElementSpec tokenElementSpec = (TokenElementSpec) elementSpec;
						if (!tokenElementSpec.matchesEmpty(checkedRules)) {
							matchesEmpty = false;
							break;
						}
					} else if (!elementSpec.isOptional()) {
						matchesEmpty = false;
						break;
					}
				}
				if (matchesEmpty)
					return true;
			}
		}
		return false;
	}
	
}
