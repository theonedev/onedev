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
	public List<ElementSuggestion> doSuggestFirst(Node parent, ParseTree parseTree, 
			String matchWith, Set<String> checkedRules) {
		if (!checkedRules.contains(ruleName)) {
			checkedRules.add(ruleName);
			return getRule().suggestFirst(parseTree, new Node(this, parent, null), matchWith, checkedRules);
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public MandatoryScan scanMandatories(Set<String> checkedRules) {
		if (!checkedRules.contains(ruleName) && getRule() != null) {
			checkedRules.add(ruleName);
		
			List<AlternativeSpec> alternatives = getRule().getAlternatives();
			if (alternatives.size() == 1) {
				List<String> mandatories = new ArrayList<>();
				for (ElementSpec elementSpec: alternatives.get(0).getElements()) {
					if (elementSpec.getMultiplicity() == Multiplicity.ZERO_OR_ONE 
							|| elementSpec.getMultiplicity() == Multiplicity.ZERO_OR_MORE) {
						return new MandatoryScan(mandatories, true);
					} else if (elementSpec.getMultiplicity() == Multiplicity.ONE_OR_MORE) {
						MandatoryScan scan = elementSpec.scanMandatories(new HashSet<>(checkedRules));
						mandatories.addAll(scan.getMandatories());
						return new MandatoryScan(mandatories, true);
					} else {
						MandatoryScan scan = elementSpec.scanMandatories(new HashSet<>(checkedRules));
						mandatories.addAll(scan.getMandatories());
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
	protected boolean matchOnce(AssistStream stream, Map<String, Integer> checkedIndexes) {
		Integer index = checkedIndexes.get(ruleName);
		if (index != null && index.intValue() == stream.getIndex()) {
			return false;
		} else {
			checkedIndexes.put(ruleName, stream.getIndex());
			return getRule().match(stream, checkedIndexes);
		}
	}

	@Override
	protected List<TokenNode> matchOnce(AssistStream stream, 
			Node parent, Node previous, Map<String, Integer> checkedIndexes) {
		Integer index = checkedIndexes.get(ruleName);
		if (index != null && index.intValue() == stream.getIndex()) {
			return new ArrayList<>();
		} else {
			checkedIndexes.put(ruleName, stream.getIndex());
			parent = new Node(this, parent, previous);
			return getRule().match(stream, parent, parent, checkedIndexes);
		}
	}

	@Override
	public String toString() {
		return "rule_ref: " + ruleName;
	}

}
