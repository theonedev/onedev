package com.pmease.commons.antlr.parsetree;

import com.pmease.commons.antlr.grammarspec.ElementSpec;

public class ElementNode extends Node {

	public ElementNode(ElementSpec spec, AlternativeNode parent) {
		super(spec, parent);
	}

}
