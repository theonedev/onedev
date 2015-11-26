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
	public CaretMove skipMandatories(String content, int offset) {
		List<AlternativeSpec> alternatives = getRule().getAlternatives();
		if (alternatives.size() == 1) {
			AlternativeSpec alternativeSpec = alternatives.get(0);
			for (ElementSpec elementSpec: alternativeSpec.getElements()) {
				if (elementSpec.getMultiplicity() == Multiplicity.ZERO_OR_ONE 
						|| elementSpec.getMultiplicity() == Multiplicity.ZERO_OR_MORE) {
					return new CaretMove(offset, true);
				} else if (elementSpec.getMultiplicity() == Multiplicity.ONE_OR_MORE) {
					CaretMove move = elementSpec.skipMandatories(content, offset);
					return new CaretMove(move.getOffset(), true);
				} else {
					CaretMove move = elementSpec.skipMandatories(content, offset);
					offset = move.getOffset();
					if (move.isStop())
						return new CaretMove(offset, true);
				}
			}
			return new CaretMove(offset, false);
		} else {
			return new CaretMove(offset, true);
		}
	}

	@Override
	public MandatoryScan scanMandatories(Set<String> checkedRules) {
		if (!checkedRules.contains(ruleName) && getRule() != null) {
			checkedRules.add(ruleName);
		
			List<AlternativeSpec> alternatives = getRule().getAlternatives();
			if (alternatives.size() == 1) {
				List<String> literals = new ArrayList<>();
				for (ElementSpec elementSpec: alternatives.get(0).getElements()) {
					if (elementSpec.getMultiplicity() == Multiplicity.ZERO_OR_ONE 
							|| elementSpec.getMultiplicity() == Multiplicity.ZERO_OR_MORE) {
						return MandatoryScan.stop();
					} else if (elementSpec.getMultiplicity() == Multiplicity.ONE_OR_MORE) {
						MandatoryScan scan = elementSpec.scanMandatories(new HashSet<>(checkedRules));
						return new MandatoryScan(scan.getMandatories(), true);
					} else {
						MandatoryScan scan = elementSpec.scanMandatories(new HashSet<>(checkedRules));
						literals.addAll(scan.getMandatories());
						if (scan.isStop())
							return new MandatoryScan(literals, true);
					}
				}
				return new MandatoryScan(literals, false);
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
	protected List<TokenNode> getPartialMatchesOnce(AssistStream stream, 
			Node parent, Node previous, Map<String, Integer> checkedIndexes) {
		Integer index = checkedIndexes.get(ruleName);
		if (index != null && index.intValue() == stream.getIndex()) {
			return new ArrayList<>();
		} else {
			checkedIndexes.put(ruleName, stream.getIndex());
			parent = new Node(this, parent, previous);
			return getRule().getPartialMatches(stream, parent, parent, checkedIndexes);
		}
	}

	@Override
	public String toString() {
		return "rule_ref: " + ruleName;
	}

}
