package com.pmease.commons.antlr.parsetree;

import com.pmease.commons.antlr.grammarspec.BlockElementSpec;

public class BlockElementNode extends ElementNode {

	private static final long serialVersionUID = 1L;

	private final AlternativeNode alternative;
	
	public BlockElementNode(BlockElementSpec spec, AlternativeNode parent, AlternativeNode alternative) {
		super(spec, parent);
		this.alternative = alternative;
	}

	public AlternativeNode getAlternative() {
		return alternative;
	}

}
