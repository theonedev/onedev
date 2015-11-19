package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

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
	protected boolean matchEmptyInElement() {
		return getRule().matchEmpty();
	}

	@Override
	public List<ElementSuggestion> suggestFirst(Node parent, String matchWith) {
		return Lists.newArrayList(new ElementSuggestion(this, parent, new ArrayList<CaretAwareText>()));
	}

}
