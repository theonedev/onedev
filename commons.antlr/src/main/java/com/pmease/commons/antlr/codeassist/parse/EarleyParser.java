package com.pmease.commons.antlr.codeassist.parse;

import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.antlr.codeassist.AssistStream;
import com.pmease.commons.antlr.codeassist.RuleSpec;

public class EarleyParser {
	
	private final List<ParseState> states = new ArrayList<>();

	private final RuleSpec rule;
	
	private final AssistStream stream;
	
	public EarleyParser(RuleSpec rule, AssistStream stream) {
		this.rule = rule;
		this.stream = stream;
		
		for (int i=0; i<rule.getAlternatives().size(); i++) {
			ParseNode parseNode = new ParseNode(0, rule, i, 0);
			ParseState state = new ParseState();
			state.getNodes().add(parseNode);
		}
		
	}
	
	public List<ParseState> getStates() {
		return states;
	}
	
}
