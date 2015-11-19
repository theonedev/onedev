package com.pmease.commons.antlr.grammarspec;

import java.util.List;

import com.google.common.collect.Lists;
import com.pmease.commons.antlr.parsetree.Node;
import com.pmease.commons.antlr.parsetree.TokenNode;

public class LiteralElementSpec extends TokenElementSpec {

	private final String literal;
	
	public LiteralElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity, 
			int tokenType, String literal) {
		super(codeAssist, label, multiplicity, tokenType);
		
		this.literal = literal;
	}

	public String getLiteral() {
		return literal;
	}

	@Override
	protected boolean matchEmptyInElement() {
		return false;
	}

	@Override
	public List<TokenNode> getFirst(Node parent) {
		return Lists.newArrayList(new TokenNode(this, parent, newToken(null)));
	}

}
