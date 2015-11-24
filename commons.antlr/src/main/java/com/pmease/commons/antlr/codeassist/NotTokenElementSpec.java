package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.Token;

import com.google.common.base.Preconditions;
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
	public List<ElementSuggestion> doSuggestFirst(Node parent, String matchWith, AssistStream stream, Set<String> checkedRules) {
		return new ArrayList<>();
	}

	@Override
	public CaretMove skipMandatories(String content, int offset) {
		return new CaretMove(offset, true);
	}

	@Override
	public List<String> getMandatories(Set<String> checkedRules) {
		return new ArrayList<>();
	}

	@Override
	protected boolean matchOnce(AssistStream stream, Map<String, Integer> checkedIndexes) {
		if (stream.isEof()) {
			return !notTokenTypes.contains(Token.EOF);
		} else {
			Token token = stream.getCurrentToken();
			stream.increaseIndex();
			return !notTokenTypes.contains(token.getType());
		}
	}

	@Override
	protected List<TokenNode> getPartialMatchesOnce(AssistStream stream, Node parent, Map<String, Integer> checkedIndexes) {
		Preconditions.checkArgument(!stream.isEof());
		
		Token token = stream.getCurrentToken();
		if (!notTokenTypes.contains(token.getType())) {
			stream.increaseIndex();
			return Lists.newArrayList(new TokenNode(this, parent, token));
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public String toString() {
		return "not: " + notTokenTypes;
	}
	
}
