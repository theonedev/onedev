package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.Token;

public class AnyTokenElementSpec extends ElementSpec {

	private static final long serialVersionUID = 1L;

	public AnyTokenElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity) {
		super(codeAssist, label, multiplicity);
	}

	@Override
	public List<ElementSuggestion> doSuggestFirst(ParseTree parseTree, Node parent, 
			String matchWith, Set<String> checkedRules) {
		return new ArrayList<ElementSuggestion>();
	}

	@Override
	public MandatoryScan scanMandatories(Set<String> checkedRules) {
		return MandatoryScan.stop();
	}

	@Override
	public Map<TokenNode, Integer> matchOnce(AssistStream stream, Node parent, Node previous, 
			Map<String, Set<RuleRefContext>> ruleRefHistory) {
		Map<TokenNode, Integer> matches = new LinkedHashMap<>();
		if (!stream.isEof()) {
			Token token = stream.getCurrentToken();
			stream.increaseIndex();
			TokenNode tokenNode = new TokenNode(this, parent, previous, token);
			if (!stream.isEof())
				matches.put(tokenNode, stream.getIndex());
			else
				matches.put(new TokenNode(null, parent, tokenNode, AssistStream.EOF), stream.getIndex());
		} else {
			matches.put(new TokenNode(null, parent, previous, AssistStream.SOF), stream.getIndex());
		}
		return matches;
	}

	@Override
	protected String asString() {
		return "any";
	}
	
}
