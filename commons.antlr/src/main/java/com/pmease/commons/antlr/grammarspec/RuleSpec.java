package com.pmease.commons.antlr.grammarspec;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.Token;

import com.pmease.commons.antlr.parsetree.Node;
import com.pmease.commons.antlr.parsetree.TokenNode;

public class RuleSpec extends Spec {

	private final String name;
	
	private final List<AlternativeSpec> alternatives;

	public RuleSpec(CodeAssist codeAssist, String name, List<AlternativeSpec> alternatives) {
		super(codeAssist);
		
		this.name = name;
		this.alternatives = alternatives;
	}
	
	public String getName() {
		return name;
	}

	public List<AlternativeSpec> getAlternatives() {
		return alternatives;
	}

	@Override
	public List<TokenNode> getFirst(@Nullable Node parent) {
		Node ruleNode =  new Node(this, parent);
		List<TokenNode> first = new ArrayList<>();
		for (AlternativeSpec alternative: alternatives)
			first.addAll(alternative.getFirst(ruleNode));
		return first;
	}

	@Override
	public List<TokenNode> match(List<Token> tokens, int from) {
		return null;
	}

	@Override
	public boolean matchEmpty() {
		for (AlternativeSpec alternative: alternatives) {
			if (alternative.matchEmpty())
				return true;
		}
		return false;
	}

}
