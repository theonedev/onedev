package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

public class RuleRefElementSpec extends ElementSpec {

	private final String ruleName;
	
	private transient RuleSpec rule;
	
	public RuleRefElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity, String ruleName) {
		super(codeAssist, label, multiplicity);
		
		this.ruleName = ruleName;
	}

	public RuleSpec getRule() {
		if (rule == null)
			rule = codeAssist.getRule(ruleName);
		return rule;
	}

	@Override
	public List<ElementSuggestion> suggestFirst(Node parent, String matchWith) {
		return getRule().suggestFirst(new Node(this, parent), matchWith);
	}

	@Override
	public boolean skipMandatories(TokenStream stream) {
		List<AlternativeSpec> alternatives = getRule().getAlternatives();
		if (alternatives.size() == 1) {
			AlternativeSpec alternativeSpec = alternatives.get(0);
			for (ElementSpec elementSpec: alternativeSpec.getElements()) {
				if (elementSpec.getMultiplicity() == Multiplicity.ZERO_OR_ONE 
						|| elementSpec.getMultiplicity() == Multiplicity.ZERO_OR_MORE) {
					return false;
				} else if (elementSpec.getMultiplicity() == Multiplicity.ONE_OR_MORE) {
					elementSpec.skipMandatories(stream);
					return false;
				} else {
					if (!elementSpec.skipMandatories(stream))
						return false;
				}
			}
			return true;
		} else {
			return false;
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
	protected boolean matchOnce(TokenStream stream) {
		return getRule().match(stream);
	}

	@Override
	protected List<TokenNode> getPartialMatchesOnce(TokenStream stream, Node parent) {
		return getRule().getPartialMatches(stream, new Node(this, parent, stream.getCurrentToken()));
	}

}
