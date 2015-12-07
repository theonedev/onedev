package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

public abstract class ElementSpec extends Spec {
	
	private static final long serialVersionUID = 1L;

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
	public List<TokenNode> match(AssistStream stream, Node parent, Node previous, 
			Map<String, Integer> checkedIndexes, boolean fullMatch) {
		return match(initMatches(stream, parent, previous), stream, parent, checkedIndexes, fullMatch);
	}
	
	public List<TokenNode> match(List<TokenNode> prevMatches, AssistStream stream, 
			Node parent, Map<String, Integer> checkedIndexes, boolean fullMatch) {
		if (multiplicity == Multiplicity.ONE) {
			return matchOnce(prevMatches, stream, parent, checkedIndexes, fullMatch, false);
		} else if (multiplicity == Multiplicity.ONE_OR_MORE) {
			List<TokenNode> matches = new ArrayList<>();
			prevMatches = matchOnce(prevMatches, stream, parent, checkedIndexes, fullMatch, false);
			matches.addAll(prevMatches);
			while (true) {
				prevMatches = matchOnce(prevMatches, stream, parent, checkedIndexes, fullMatch, true);
				matches.addAll(prevMatches);
				if (prevMatches.isEmpty())
					break;
			}
			codeAssist.prune(matches, stream);
			return matches;
		} else if (multiplicity == Multiplicity.ZERO_OR_MORE) {
			List<TokenNode> matches = Lists.newArrayList(prevMatches);
			while (true) {
				prevMatches = matchOnce(prevMatches, stream, parent, checkedIndexes, fullMatch, true);
				matches.addAll(prevMatches);
				if (prevMatches.isEmpty())
					break;
			}
			codeAssist.prune(matches, stream);
			return matches;
		} else {
			List<TokenNode> matches = Lists.newArrayList(prevMatches);
			matches.addAll(matchOnce(prevMatches, stream, parent, checkedIndexes, fullMatch, true));
			codeAssist.prune(matches, stream);
			return matches;
		}
	}
	
	public List<TokenNode> matchOnce(List<TokenNode> prevMatches, AssistStream stream, 
			Node parent, Map<String, Integer> checkedIndexes, boolean fullMatch, boolean mustAdvance) {
		List<TokenNode> matches = new ArrayList<>();
		for (TokenNode prevMatch: prevMatches) {
			int prevMatchIndex = prevMatch.getToken().getTokenIndex();
			stream.setIndex(prevMatchIndex+1);
			for (TokenNode match: matchOnce(stream, parent, prevMatch, new HashMap<>(checkedIndexes), fullMatch)) {
				if (!mustAdvance || match.getToken().getTokenIndex() != prevMatchIndex)
					matches.add(match);
			}
		}
		codeAssist.prune(matches, stream);
		return matches;
	}
	
	public abstract List<TokenNode> matchOnce(AssistStream stream, Node parent, Node previous, 
			Map<String, Integer> checkedIndexes, boolean fullMatch);

	public abstract MandatoryScan scanMandatories(Set<String> checkedRules);
	
	/**
	 * Provide suggestions after current element spec.
	 * 
	 * @param parseTree
	 * 			current parse tree
	 * @param parent
	 * 			parent node of current element spec
	 * @param matchWith
	 * 			string to match with
	 * @return
	 * 			list of element suggestions of current element spec
	 */
	public List<ElementSuggestion> suggestNext(ParseTree parseTree, Node parent, String matchWith) {
		List<ElementSuggestion> suggestions = new ArrayList<>();

		// if element spec can be repeated, next input candidate can also be taken from the element itself
		if (multiplicity == Multiplicity.ONE_OR_MORE || multiplicity == Multiplicity.ZERO_OR_MORE) 
			suggestions.addAll(suggestFirst(parseTree, parent, matchWith, new HashSet<String>()));
		suggestions.addAll(doSuggestNext(parseTree, parent, matchWith));
		return suggestions;
	}
	
	private List<ElementSuggestion> doSuggestNext(ParseTree parseTree, Node parent, String matchWith) {
		List<ElementSuggestion> suggestions = new ArrayList<>();
		AlternativeSpec alternativeSpec = (AlternativeSpec) parent.getSpec();
		int specIndex = alternativeSpec.getElements().indexOf(this);
		if (specIndex == alternativeSpec.getElements().size()-1) {
			Node parentElementNode = parent.getParent().getParent();
			if (parentElementNode != null) {
				ElementSpec parentElementSpec = (ElementSpec) parentElementNode.getSpec();
				suggestions.addAll(parentElementSpec.suggestNext(
						parseTree, parentElementNode.getParent(), matchWith));
			}
		} else {
			ElementSpec nextElementSpec = alternativeSpec.getElements().get(specIndex+1);
			suggestions.addAll(nextElementSpec.suggestFirst(
					parseTree, parent, matchWith, new HashSet<String>()));
			/*
			 * if next element is optional, the next next element can also be input candidate
			 */
			if (nextElementSpec.matchesEmpty())
				suggestions.addAll(nextElementSpec.doSuggestNext(parseTree, parent, matchWith));
		}
		return suggestions;
	}
	
	@Override
	public List<ElementSuggestion> suggestFirst(@Nullable ParseTree parseTree, Node parent, 
			String matchWith, Set<String> checkedRules) {
		Node elementNode = new Node(this, parent, null);
		List<InputSuggestion> suggestions = codeAssist.suggest(parseTree, elementNode, matchWith);
		if (suggestions != null)
			return Lists.newArrayList(new ElementSuggestion(parseTree, elementNode, matchWith, suggestions));
		else // user does not care about suggestion, so we continue with our default logic to drill down 
			return doSuggestFirst(parseTree, parent, matchWith, checkedRules);
	}

	protected abstract List<ElementSuggestion> doSuggestFirst(@Nullable ParseTree parseTree, 
			Node parent, String matchWith, Set<String> checkedRules);

	@Override
	public boolean matchesEmpty() {
		if (multiplicity == Multiplicity.ZERO_OR_MORE || multiplicity == Multiplicity.ZERO_OR_ONE)
			return true;
		else
			return matchesEmptyOnce();
	}
	
	protected abstract boolean matchesEmptyOnce();

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

	protected abstract String toStringOnce();
}
