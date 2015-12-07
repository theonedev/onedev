package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

public class RuleRefElementSpec extends ElementSpec {

	private static final long serialVersionUID = 1L;

	private final String ruleName;
	
	private transient RuleSpec rule;
	
	public RuleRefElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity, String ruleName) {
		super(codeAssist, label, multiplicity);
		
		this.ruleName = ruleName;
	}

	public RuleSpec getRule() {
		if (rule == null)
			rule = Preconditions.checkNotNull(codeAssist.getRule(ruleName));
		return rule;
	}
	
	public String getRuleName() {
		return ruleName;
	}

	@Override
	public List<ElementSuggestion> doSuggestFirst(ParseTree parseTree, Node parent, 
			String matchWith, Set<String> checkedRules) {
		return getRule().suggestFirst(parseTree, new Node(this, parent, null), matchWith, checkedRules);
	}

	@Override
	public MandatoryScan scanMandatories(Set<String> checkedRules) {
		if (!checkedRules.contains(ruleName)) {
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
			} 
		} 
		return MandatoryScan.stop();
	}

	@Override
	public List<TokenNode> matchOnce(AssistStream stream, Node parent, Node previous, 
			Map<String, Integer> checkedIndexes, boolean fullMatch) {
		parent = new Node(this, parent, previous);
		return getRule().match(stream, parent, parent, checkedIndexes, fullMatch);
	}

	@Override
	protected String toStringOnce() {
		if (codeAssist.isBlockRule(ruleName))
			return "(" + Preconditions.checkNotNull(getRule()) + ")";
		else 
			return ruleName;
	}

	@Override
	public Set<Integer> getLeadingTokenTypes() {
		return getRule().getLeadingTokenTypes();
	}

	@Override
	protected boolean matchesEmptyOnce() {
		return getRule().matchesEmpty();
	}

}
