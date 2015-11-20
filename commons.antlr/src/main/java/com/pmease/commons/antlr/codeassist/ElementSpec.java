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
		return null;
	}

	public abstract boolean skipMandatories(TokenStream stream);
	
	public abstract List<String> getMandatories();
	
	@Override
	public boolean matches(TokenStream stream) {
		if (multiplicity == Multiplicity.ONE) {
			return matchesOnce(stream);
		} else if (multiplicity == Multiplicity.ONE_OR_MORE) {
			if (!matchesOnce(stream)) {
				return false;
			} else {
				while(matchesOnce(stream));
				return true;
			}
		} else if (multiplicity == Multiplicity.ZERO_OR_MORE) {
			while (matchesOnce(stream));
			return true;
		} else {
			matchesOnce(stream);
			return true;
		}
	}
	
	protected abstract boolean matchesOnce(TokenStream stream);
}
