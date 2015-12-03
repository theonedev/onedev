package com.pmease.commons.antlr.codeassist;

import java.util.LinkedHashMap;
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
	public Map<TokenNode, Integer> matchOnce(AssistStream stream, Node parent, Node previous, 
			Map<String, Set<RuleRefContext>> ruleRefHistory) {
		Map<TokenNode, Integer> matches = new LinkedHashMap<>();
		if (!stream.isEof()) {
			Token token = stream.getCurrentToken();
			if (token.getType() == type) {
				stream.increaseIndex();
				TokenNode tokenNode = new TokenNode(this, parent, previous, token);
				if (!stream.isEof())
					matches.put(tokenNode, stream.getIndex());
				else
					matches.put(new TokenNode(null, parent, tokenNode, AssistStream.EOF), stream.getIndex());
			} 
		} else {
			matches.put(new TokenNode(null, parent, previous, AssistStream.SOF), stream.getIndex());
		}
		return matches;
	}

}
