package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.Token;

import com.google.common.collect.Lists;

public class NotTokenElementSpec extends ElementSpec {

	private static final long serialVersionUID = 1L;
	
	private final Set<Integer> notTokenTypes;
	
	public NotTokenElementSpec(CodeAssist codeAssist, String label, 
			Multiplicity multiplicity, Set<Integer> notTokenTypes) {
		super(codeAssist, label, multiplicity);
		
		this.notTokenTypes = notTokenTypes;
	}

	public Set<Integer> getNotTokenTypes() {
		return notTokenTypes;
	}

	@Override
	public List<ElementSuggestion> doSuggestFirst(Node parent, ParseTree parseTree, 
			String matchWith, Set<String> checkedRules) {
		return new ArrayList<>();
	}

	@Override
	public MandatoryScan scanMandatories(Set<String> checkedRules) {
		return MandatoryScan.stop();
	}

	@Override
	protected SpecMatch matchOnce(AssistStream stream, 
			Node parent, Node previous, Map<String, Integer> checkedIndexes) {
		if (stream.isEof()) {
			return new SpecMatch(new ArrayList<TokenNode>(), false);
		} else {
			Token token = stream.getCurrentToken();
			if (!notTokenTypes.contains(token.getType())) {
				stream.increaseIndex();
				TokenNode tokenNode = new TokenNode(this, parent, previous, token);
				return new SpecMatch(Lists.newArrayList(tokenNode), true);
			} else {
				return new SpecMatch(new ArrayList<TokenNode>(), false);
			}
		}
	}

	@Override
	public String toString() {
		return "not: " + notTokenTypes;
	}
	
}
