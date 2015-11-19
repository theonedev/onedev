package com.pmease.commons.antlr.grammarspec;

import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.antlr.parsetree.Node;
import com.pmease.commons.antlr.parsetree.TokenNode;

public class AnyTokenElementSpec extends ElementSpec {

	public AnyTokenElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity) {
		super(codeAssist, label, multiplicity);
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
