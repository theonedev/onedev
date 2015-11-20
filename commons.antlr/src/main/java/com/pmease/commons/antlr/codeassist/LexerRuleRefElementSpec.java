package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.pmease.commons.util.StringUtils;

public class LexerRuleRefElementSpec extends TokenElementSpec {

	private final String ruleName;
	
	private transient RuleSpec rule;
	
	public LexerRuleRefElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity, 
			int tokenType, String ruleName) {
		super(codeAssist, label, multiplicity, tokenType);
		
		this.ruleName = ruleName;
	}

	public RuleSpec getRule() {
		if (rule == null)
			rule = codeAssist.getRule(ruleName);
		return rule;
	}

	@Override
	public List<ElementSuggestion> suggestFirst(Node parent, String matchWith) {
		return Lists.newArrayList(new ElementSuggestion(new Node(this, parent), new ArrayList<CaretAwareText>()));
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
		List<String> mandatories = new ArrayList<>();
		List<AlternativeSpec> alternatives = getRule().getAlternatives();
		if (alternatives.size() == 1) {
			for (ElementSpec elementSpec: alternatives.get(0).getElements()) {
				if (elementSpec.getMultiplicity() == Multiplicity.ONE 
						|| elementSpec.getMultiplicity() == Multiplicity.ONE_OR_MORE) {
					mandatories.addAll(elementSpec.getMandatories());
				}
			}
		} 
		if (!mandatories.isEmpty())
			return Lists.newArrayList(StringUtils.join(mandatories, ""));
		else
			return mandatories;
	}

	@Override
	protected boolean matchesOnce(TokenStream stream) {
		if (stream.isEnd()) {
			return false;
		} else if (stream.getCurrentToken().getType() == type) {
			stream.increaseIndex();
			return true;
		} else {
			return false;
		}
	}
	
}
