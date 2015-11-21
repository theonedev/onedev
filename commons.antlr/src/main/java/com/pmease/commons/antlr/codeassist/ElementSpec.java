package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

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
	public List<TokenNode> getPartialMatches(TokenStream stream, Node parent) {
		if (multiplicity == Multiplicity.ONE) {
			return getPartialMatchesOnce(stream, parent);
		} else if (multiplicity == Multiplicity.ONE_OR_MORE) {
			List<TokenNode> partialMatches = getPartialMatchesOnce(stream, parent);
			if (partialMatches != null && !partialMatches.isEmpty()) {
				while (!stream.isEof()) {
					List<TokenNode> prevPartialMatches = partialMatches;
					partialMatches = getPartialMatchesOnce(stream, parent);
					if (partialMatches == null || partialMatches.isEmpty())
						return prevPartialMatches;
				}
			} 
			return partialMatches;
		} else if (multiplicity == Multiplicity.ZERO_OR_MORE) {
			List<TokenNode> partialMatches = new ArrayList<>();
			while (!stream.isEof()) {
				List<TokenNode> prevMatches = partialMatches;
				partialMatches = getPartialMatchesOnce(stream, parent);
				if (partialMatches == null || partialMatches.isEmpty())
					return prevMatches;
			}
			return partialMatches;
		} else {
			List<TokenNode> partialMatches = getPartialMatchesOnce(stream, parent);
			if (partialMatches != null && !partialMatches.isEmpty() && !stream.isEof()) {
				List<TokenNode> prevMatches = partialMatches;
				partialMatches = getPartialMatchesOnce(stream, parent);
				if (partialMatches == null || partialMatches.isEmpty())
					return prevMatches;
			}
			return partialMatches;
		}
	}
	
	protected abstract List<TokenNode> getPartialMatchesOnce(TokenStream stream, Node parent);

	public abstract boolean skipMandatories(TokenStream stream);
	
	public abstract List<String> getMandatories();
	
	@Override
	public boolean match(TokenStream stream) {
		if (multiplicity == Multiplicity.ONE) {
			return matchOnce(stream);
		} else if (multiplicity == Multiplicity.ONE_OR_MORE) {
			if (!matchOnce(stream)) {
				return false;
			} else {
				while(matchOnce(stream));
				return true;
			}
		} else if (multiplicity == Multiplicity.ZERO_OR_MORE) {
			while (matchOnce(stream));
			return true;
		} else {
			matchOnce(stream);
			return true;
		}
	}
	
	protected abstract boolean matchOnce(TokenStream stream);
}
