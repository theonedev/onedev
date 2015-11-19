package com.pmease.commons.antlr.grammarspec;

import java.util.List;

import com.pmease.commons.antlr.parsetree.Node;
import com.pmease.commons.antlr.parsetree.TokenNode;

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
	public List<TokenNode> getFirst(Node parent) {
		return getRule().getFirst(new Node(this, parent));
	}
}
