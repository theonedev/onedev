package com.pmease.commons.antlr.parsetree;

import com.pmease.commons.antlr.grammarspec.Spec;

public class ElementNode extends Node {

	private static final long serialVersionUID = 1L;

	public ElementNode(Spec spec, AlternativeNode parent) {
		super(spec, parent);
	}

}
