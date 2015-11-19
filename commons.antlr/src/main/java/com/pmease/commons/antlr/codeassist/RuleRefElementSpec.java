package com.pmease.commons.antlr.codeassist;

import java.util.List;

public class RuleRefElementSpec extends ElementSpec {

	private final String ruleName;
	
	private transient RuleSpec rule;
	
	public RuleRefElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity, String ruleName) {
		super(codeAssist, label, multiplicity);
		
		this.ruleName = ruleName;
	}

	@Override
	protected boolean matchEmptyInElement() {
		return getRule().matchEmpty();
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
}
