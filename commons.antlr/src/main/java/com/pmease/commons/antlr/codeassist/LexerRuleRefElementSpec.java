package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

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
		if (!checkedRules.contains(ruleName) && getRule() != null) { // to avoid infinite loop
			checkedRules.add(ruleName);
		
			List<AlternativeSpec> alternatives = getRule().getAlternatives();
			// nothing will be mandatory if we have multiple alternatives 
			if (alternatives.size() == 1) {
				List<String> mandatories = new ArrayList<>();
				for (ElementSpec elementSpec: alternatives.get(0).getElements()) {
					if (elementSpec.getMultiplicity() == Multiplicity.ZERO_OR_ONE 
							|| elementSpec.getMultiplicity() == Multiplicity.ZERO_OR_MORE) {
						// next input can either be current element, or other elements, so 
						// mandatory scan can be stopped
						return new MandatoryScan(mandatories, true);
					} else if (elementSpec.getMultiplicity() == Multiplicity.ONE_OR_MORE) {
						MandatoryScan scan = elementSpec.scanMandatories(new HashSet<>(checkedRules));
						mandatories.addAll(scan.getMandatories());
						// next input can either be current element, or other elements, so 
						// mandatory scan can be stopped
						return new MandatoryScan(mandatories, true);
					} else {
						MandatoryScan scan = elementSpec.scanMandatories(new HashSet<>(checkedRules));
						mandatories.addAll(scan.getMandatories());
						// if internal of the element tells use to stop, let's stop 
						if (scan.isStop())
							return new MandatoryScan(mandatories, true);
					}
				}
				return new MandatoryScan(mandatories, false);
			} else {
				return MandatoryScan.stop();
			}
		} else {
			return MandatoryScan.stop();
		}
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
	
}
