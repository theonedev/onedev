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

	private Node getPrevious(SpecMatch match, Node currentPrevious) {
		if (match.getPaths().isEmpty())
			return currentPrevious;
		else
			return match.getPaths().get(match.getPaths().size()-1);
	}
	
	@Override
	public SpecMatch match(AssistStream stream, 
			Node parent, Node previous, Map<String, Integer> checkedIndexes) {
		if (multiplicity == Multiplicity.ONE) {
			return matchOnce(stream, parent, previous, checkedIndexes);
		} else if (multiplicity == Multiplicity.ONE_OR_MORE) {
			SpecMatch match = matchOnce(stream, parent, previous, checkedIndexes);
			if (!match.isMatched()) {
				return new SpecMatch(match.getPaths(), false);
			} else if (match.getPaths().isEmpty()) {
				return new SpecMatch(match.getPaths(), true);
			} else {
				List<TokenNode> paths = match.getPaths();
				while (true) {
					previous = getPrevious(match, previous);
					match = matchOnce(stream, parent, previous, checkedIndexes);
					if (!match.isMatched()) {
						if (!match.getPaths().isEmpty()) {
							paths = match.getPaths();
							return new SpecMatch(paths, false);
						} else {
							return new SpecMatch(paths, true);
						}
					} else if (match.getPaths().isEmpty()) {
						return new SpecMatch(paths, true);
					} else {
						paths = match.getPaths();
					}
				}
			}
		} else if (multiplicity == Multiplicity.ZERO_OR_MORE) {
			List<TokenNode> paths = new ArrayList<>();
			while (true) {
				SpecMatch match = matchOnce(stream, parent, previous, checkedIndexes);
				if (!match.isMatched()) {
					if (!match.getPaths().isEmpty()) {
						paths = match.getPaths();
						return new SpecMatch(paths, false);
					} else {
						return new SpecMatch(paths, true);
					}
				} else if (match.getPaths().isEmpty()) {
					return new SpecMatch(paths, true);
				} else {
					paths = match.getPaths();
				}
				previous = getPrevious(match, previous);
			}
		} else {
			SpecMatch match = matchOnce(stream, parent, previous, checkedIndexes);
			if (!match.isMatched()) {
				if (!match.getPaths().isEmpty()) 
					return new SpecMatch(match.getPaths(), false);
				else 
					return new SpecMatch(match.getPaths(), true);
			} else { 
				return match;
			}
		}
	}
	
	public boolean matchesOnce(AssistStream stream) {
		return matchOnce(stream, null, null, new HashMap<String, Integer>()).isMatched();
	}
	
	protected abstract SpecMatch matchOnce(AssistStream stream, 
			Node parent, Node previous, Map<String, Integer> checkedIndexes);

	public abstract MandatoryScan scanMandatories(Set<String> checkedRules);
	
	public List<ElementSuggestion> suggestNext(ParseTree parseTree, Node parent, String matchWith) {
		List<ElementSuggestion> suggestions = new ArrayList<>();
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
			if (nextElementSpec.matches(codeAssist.lex("")))
				suggestions.addAll(nextElementSpec.doSuggestNext(parseTree, parent, matchWith));
		}
		return suggestions;
	}
	
	@Override
	public List<ElementSuggestion> suggestFirst(@Nullable ParseTree parseTree, Node parent, 
			String matchWith, Set<String> checkedRules) {
		Node elementNode = new Node(this, parent, null);
		List<InputSuggestion> suggestions = codeAssist.suggest(parseTree, elementNode, matchWith);
		if (suggestions != null) {
			if (suggestions.isEmpty() && !scanMandatories(new HashSet<String>()).getMandatories().isEmpty())
				return doSuggestFirst(parent, parseTree, matchWith, checkedRules);
			else
				return Lists.newArrayList(new ElementSuggestion(parseTree, elementNode, matchWith, suggestions));
		} else {
			return doSuggestFirst(parent, parseTree, matchWith, checkedRules);
		}
	}

	protected abstract List<ElementSuggestion> doSuggestFirst(Node parent, 
			@Nullable ParseTree parseTree, String matchWith, Set<String> checkedRules);
	
}
