package com.pmease.commons.antlr.codeassist.parse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.pmease.commons.antlr.codeassist.AssistStream;
import com.pmease.commons.antlr.codeassist.RuleSpec;

public class EarleyParser {
	
	private final List<ParseState> states = new ArrayList<>();

	private final RuleSpec rule;
	
	private final AssistStream stream;
	
	public EarleyParser(RuleSpec rule, AssistStream stream) {
		this.rule = rule;
		this.stream = stream;
		
		Set<ParseNode> nodes = new HashSet<>();
		for (int i=0; i<rule.getAlternatives().size(); i++)
			nodes.add(new ParseNode(0, rule, i, 0, false));

		ParseState state = new ParseState(stream.getIndex(), nodes);
		state.predict(stream);
		state.complete(states);
		states.add(state);
		
		while (!stream.isEof()) {
			state = state.scan(stream);
			stream.increaseIndex();
			if (state.getNodes().isEmpty())
				break;
			states.add(state);
			state.predict(stream);
		}
	}
	
	public List<ParseState> getStates() {
		return states;
	}
	
}
