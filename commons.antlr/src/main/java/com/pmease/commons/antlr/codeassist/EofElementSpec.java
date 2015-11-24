package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.Token;

import com.google.common.base.Preconditions;

public class EofElementSpec extends TokenElementSpec {

	private static final long serialVersionUID = 1L;

	public EofElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity) {
		super(codeAssist, label, multiplicity, Token.EOF);
	}

	@Override
	public List<ElementSuggestion> doSuggestFirst(Node parent, String matchWith, AssistStream stream, Set<String> checkedRules) {
		return new ArrayList<ElementSuggestion>();
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
		return stream.isEof();
	}

	@Override
	protected List<TokenNode> getPartialMatchesOnce(AssistStream stream, Node parent, Map<String, Integer> checkedIndexes) {
		Preconditions.checkArgument(!stream.isEof());
		return new ArrayList<>();
	}

	@Override
	public String toString() {
		return "EOF";
	}
	
}
