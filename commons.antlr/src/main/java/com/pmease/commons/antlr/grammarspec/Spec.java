package com.pmease.commons.antlr.grammarspec;

import java.util.List;

import org.antlr.v4.runtime.Token;

import com.pmease.commons.antlr.parsetree.Node;
import com.pmease.commons.antlr.parsetree.TokenNode;

public abstract class Spec {
	
	protected final CodeAssist codeAssist;

	public Spec(CodeAssist codeAssist) {
		this.codeAssist = codeAssist;
	}
	
	public abstract List<TokenNode> match(List<Token> tokens, int from);
	
	public abstract List<TokenNode> getFirst(Node parent);
	
	public abstract boolean matchEmpty();

	public CodeAssist getCodeAssist() {
		return codeAssist;
	}
	
}
