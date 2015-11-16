package com.pmease.commons.antlr.parsetree;

import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.antlr.grammarspec.AlternativeSpec;
import com.pmease.commons.antlr.grammarspec.ElementSpec;
import com.pmease.commons.antlr.grammarspec.LexerRuleRefElementSpec;
import com.pmease.commons.antlr.grammarspec.RuleRefElementSpec;
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
	
	public List<RuleRefElementNode> getElementsByRule(String ruleName) {
		List<RuleRefElementNode> nodes = new ArrayList<>();
		for (ElementNode node: elements) {
			Spec spec = node.getSpec();
			if (spec instanceof RuleRefElementSpec) {
				RuleRefElementSpec ruleRefElementSpec = (RuleRefElementSpec) spec;
				if (ruleRefElementSpec.getRuleName().equals(ruleName))
					nodes.add((RuleRefElementNode)node);
			}
		}
		return nodes;
	}
	
	public List<LexerRuleRefElementNode> getElementsByLexerRule(String ruleName) {
		List<LexerRuleRefElementNode> nodes = new ArrayList<>();
		for (ElementNode node: elements) {
			Spec spec = node.getSpec();
			if (spec instanceof LexerRuleRefElementSpec) {
				LexerRuleRefElementSpec lexerRuleRefElementSpec = (LexerRuleRefElementSpec) spec;
				if (lexerRuleRefElementSpec.getRuleName().equals(ruleName))
					nodes.add((LexerRuleRefElementNode)node);
			}
		}
		return nodes;
	}
	
}
