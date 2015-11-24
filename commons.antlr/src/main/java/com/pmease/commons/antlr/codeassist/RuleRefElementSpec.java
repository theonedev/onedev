package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

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
	public List<ElementSuggestion> doSuggestFirst(Node parent, String matchWith, AssistStream stream) {
		return getRule().suggestFirst(new Node(this, parent), matchWith, stream);
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
	public List<String> getMandatories() {
		List<String> mandatoryLiterals = new ArrayList<>();
		List<AlternativeSpec> alternatives = getRule().getAlternatives();
		if (alternatives.size() == 1) {
			for (ElementSpec elementSpec: alternatives.get(0).getElements()) {
				if (elementSpec.getMultiplicity() == Multiplicity.ONE 
						|| elementSpec.getMultiplicity() == Multiplicity.ONE_OR_MORE) {
					mandatoryLiterals.addAll(elementSpec.getMandatories());
				}
			}
		} 
		return mandatoryLiterals;
	}

	@Override
	protected boolean matchOnce(AssistStream stream) {
		return getRule().match(stream);
	}

	@Override
	protected List<TokenNode> getPartialMatchesOnce(AssistStream stream, Node parent) {
		return getRule().getPartialMatches(stream, new Node(this, parent, stream.getCurrentToken()));
	}

}
