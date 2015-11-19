package com.pmease.commons.antlr.codeassist;

import java.util.List;

import org.antlr.v4.runtime.Token;

public abstract class ElementSpec extends Spec {
	
	public enum Multiplicity{ONE, ZERO_OR_ONE, ZERO_OR_MORE, ONE_OR_MORE};
	
	private final String label;
	
	private final Multiplicity multiplicity;
	
	public ElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity) {
		super(codeAssist);
		
		this.label = label;
		this.multiplicity = multiplicity;
	}

	public String getLabel() {
		return label;
	}

	public Multiplicity getMultiplicity() {
		return multiplicity;
	}

	@Override
	public List<TokenNode> match(List<Token> tokens, int from) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchEmpty() {
		if (multiplicity == Multiplicity.ZERO_OR_MORE || multiplicity == Multiplicity.ZERO_OR_ONE)
			return true;
		else
			return matchEmptyInElement();
	}
	
	protected abstract boolean matchEmptyInElement();
	
	public abstract CaretMove moveCaretToEdit(TokenStream stream);
}
