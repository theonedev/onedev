package com.pmease.commons.antlr.grammar;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.Token;

import com.pmease.commons.antlr.codeassist.MandatoryScan;

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
	
	public boolean isOptional() {
		return multiplicity == Multiplicity.ZERO_OR_MORE || multiplicity == Multiplicity.ZERO_OR_ONE;
	}

	public boolean isMultiple() {
		return multiplicity == Multiplicity.ONE_OR_MORE || multiplicity == Multiplicity.ZERO_OR_MORE;
	}
	
	public abstract Set<String> getLeadingChoices();
	
	protected abstract boolean isAllowEmptyOnce();
	
	public boolean isAllowEmpty() {
		if (isOptional())
			return true;
		else
			return isAllowEmptyOnce();
	}
	
	public abstract MandatoryScan scanMandatories();
	
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
	
	public abstract int getEndOfMatch(List<Token> tokens);
	
	protected abstract String toStringOnce();
}
