package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.Token;

public abstract class TokenElementSpec extends ElementSpec {

	private static final long serialVersionUID = 1L;
	
	protected final int type;

	public TokenElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity, int type) {
		super(codeAssist, label, multiplicity);
		
		this.type = type;
	}

	/**
	 * Get token type of this element
	 * 
	 * @return
	 * 		token type of the element, 0 if the element refers to a fragment rule
	 */
	public int getType() {
		return type;
	}

	@Override
	public List<TokenNode> matchOnce(AssistStream stream, Node parent, Node previous, 
			Map<String, Set<RuleRefContext>> ruleRefHistory, boolean fullMatch) {
		List<TokenNode> matches = new ArrayList<>();
		if (!stream.isEof()) {
			Token token = stream.getCurrentToken();
			if (token.getType() == type) {
				stream.increaseIndex();
				TokenNode tokenNode = new TokenNode(this, parent, previous, token);
				matches.add(tokenNode);
			} 
		} else if (!fullMatch) {
			matches.add(new TokenNode(null, parent, previous, new FakedToken(stream)));
		}
		return matches;
	}

}
