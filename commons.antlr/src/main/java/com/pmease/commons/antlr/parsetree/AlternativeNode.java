package com.pmease.commons.antlr.parsetree;

import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.antlr.grammarspec.AlternativeSpec;
import com.pmease.commons.antlr.grammarspec.ElementSpec;
import com.pmease.commons.antlr.grammarspec.RuleSpec;
import com.pmease.commons.antlr.grammarspec.Spec;

public class AlternativeNode extends Node {

	private static final long serialVersionUID = 1L;

	private final List<ElementNode> elements;
	
	public AlternativeNode(AlternativeSpec spec, RuleNode parent, List<ElementNode> elements) {
		super(spec, parent);
		
		this.elements = elements;
	}

	public List<ElementNode> getElements() {
		return elements;
	}

	public List<ElementNode> getElementsByLabel(String label) {
		List<ElementNode> nodes = new ArrayList<>();
		for (ElementNode node: elements) {
			ElementSpec spec = (ElementSpec) node.getSpec();
			if (label.equals(spec.getLabel()))
				nodes.add(node);
		}
		return nodes;
	}
	
	public List<RuleNode> getElementsByRule(String ruleName) {
		List<RuleNode> nodes = new ArrayList<>();
		for (ElementNode node: elements) {
			Spec spec = node.getSpec();
			if (spec instanceof RuleSpec) {
				RuleSpec ruleSpec = (RuleSpec) spec;
				if (ruleSpec.getName().equals(ruleName))
					nodes.add((RuleNode)node);
			}
		}
		return nodes;
	}
	
	public List<RuleNode> getElementsByLexerRule(String ruleName) {
		List<RuleNode> nodes = new ArrayList<>();
		for (ElementNode node: elements) {
			Spec spec = node.getSpec();
			if (spec instanceof RuleSpec) {
				RuleSpec ruleSpec = (RuleSpec) spec;
				if (ruleSpec.getName().equals(ruleName))
					nodes.add((RuleNode)node);
			}
		}
		return nodes;
	}
	
}
