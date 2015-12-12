package com.pmease.commons.antlr.codeassist;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.Token;

public abstract class ElementSpec implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public enum Multiplicity{ONE, ZERO_OR_ONE, ZERO_OR_MORE, ONE_OR_MORE};
	
	private final String label;
	
	private final Multiplicity multiplicity;
	
	public ElementSpec(String label, Multiplicity multiplicity) {
		this.label = label;
		this.multiplicity = multiplicity;
	}

	public String getLabel() {
		return label;
	}

	public Multiplicity getMultiplicity() {
		return multiplicity;
	}

	public abstract MandatoryScan scanMandatories(Set<String> checkedRules);
	
	public final String toString() {
		if (multiplicity == Multiplicity.ONE)
			return toStringOnce();
		else if (multiplicity == Multiplicity.ONE_OR_MORE)
			return toStringOnce() + "+";
		else if (multiplicity == Multiplicity.ZERO_OR_MORE)
			return toStringOnce() + "*";
		else
			return toStringOnce() + "?";
	}
	
	public abstract int getMatchDistance(List<Token> tokens);
	
	protected abstract String toStringOnce();
}
