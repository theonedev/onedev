package com.pmease.commons.antlr.grammarspec;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.pmease.commons.antlr.parsetree.Node;
import com.pmease.commons.antlr.parsetree.TokenNode;

public class NotTokenElementSpec extends ElementSpec {

	private final Set<Integer> notTokenTypes;
	
	public NotTokenElementSpec(CodeAssist codeAssist, String label, 
			Multiplicity multiplicity, Set<Integer> notTokenTypes) {
		super(codeAssist, label, multiplicity);
		
		this.notTokenTypes = notTokenTypes;
	}

	public Set<Integer> getNotTokenTypes() {
		return notTokenTypes;
	}

	@Override
	protected boolean matchEmptyInElement() {
		return false;
	}

	@Override
	public List<TokenNode> getFirst(Node parent) {
		return new ArrayList<>();
	}

}
