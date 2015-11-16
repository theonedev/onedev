package com.pmease.commons.antlr.parsetree;

import javax.annotation.Nullable;

import com.pmease.commons.antlr.grammarspec.AlternativeSpec;
import com.pmease.commons.antlr.grammarspec.RuleSpec;

public class RuleNode extends ElementNode {

	private static final long serialVersionUID = 1L;
	
	private final AlternativeNode alternative;

	public RuleNode(RuleSpec spec, AlternativeNode parent, AlternativeNode alternative) {
		super(spec, parent);
		
		this.alternative = alternative;
	}

	public AlternativeNode getAlternative() {
		return alternative;
	}

	@Nullable 
	public AlternativeNode getAlternative(String label) {
		AlternativeSpec spec = (AlternativeSpec) alternative.getSpec();
		if (label.equals(spec.getLabel()))
			return alternative;
		else
			return null;
	}
	
}
