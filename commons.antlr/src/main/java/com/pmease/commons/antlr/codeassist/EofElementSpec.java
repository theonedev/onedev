package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.Token;

public class EofElementSpec extends TokenElementSpec {

	private static final long serialVersionUID = 1L;

	public EofElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity) {
		super(codeAssist, label, multiplicity, Token.EOF);
	}

	@Override
	public List<ElementSuggestion> doSuggestFirst(Node parent, ParseTree parseTree, 
			String matchWith, Set<String> checkedRules) {
		return new ArrayList<ElementSuggestion>();
	}

	@Override
	public MandatoryScan scanMandatories(Set<String> checkedRules) {
		return MandatoryScan.stop();
	}

	@Override
	protected SpecMatch matchOnce(AssistStream stream, 
			Node parent, Node previous, Map<String, Integer> checkedIndexes) {
		return new SpecMatch(new ArrayList<TokenNode>(), stream.isEof());
	}

	@Override
	public String toString() {
		return "EOF";
	}
	
}
