package com.pmease.commons.antlr.grammarspec;

import java.util.List;

import com.google.common.collect.Lists;
import com.pmease.commons.antlr.parsetree.Node;
import com.pmease.commons.antlr.parsetree.TokenNode;

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
	public List<TokenNode> getFirst(Node parent) {
		return Lists.newArrayList(new TokenNode(this, parent, newToken(null)));
	}

}
