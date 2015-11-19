package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.Token;

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
	public List<ElementSuggestion> suggestFirst(@Nullable Node parent, String matchWith) {
		Node ruleNode =  new Node(this, parent);
		List<ElementSuggestion> first = new ArrayList<>();
		for (AlternativeSpec alternative: alternatives)
			first.addAll(alternative.suggestFirst(ruleNode, matchWith));
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
