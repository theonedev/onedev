package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
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
	public List<TokenNode> getPartialMatches(AssistStream stream, 
			Node parent, Node previous, Map<String, Integer> checkedIndexes) {
		Preconditions.checkArgument(!stream.isEof());
		
		if (multiplicity == Multiplicity.ONE || multiplicity == Multiplicity.ZERO_OR_ONE) {
			return getPartialMatchesOnce(stream, parent, previous, checkedIndexes);
		} else {
			List<TokenNode> matches = getPartialMatchesOnce(stream, parent, previous, checkedIndexes);
			if (!matches.isEmpty()) {
				while (!stream.isEof()) {
					List<TokenNode> nextMatches = getPartialMatchesOnce(stream, parent, previous, checkedIndexes);
					if (!nextMatches.isEmpty())
						matches = nextMatches;
					else
						break;
				}
			} 
			return matches;
		}
	}
	
	protected abstract List<TokenNode> getPartialMatchesOnce(AssistStream stream, 
			Node parent, Node previous, Map<String, Integer> checkedIndexes);

	public abstract CaretMove skipMandatories(String content, int offset);
	
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
			if (nextElementSpec.match(codeAssist.lex(""), new HashMap<String, Integer>()))
				suggestions.addAll(nextElementSpec.doSuggestNext(parseTree, parent, matchWith));
		}
		return suggestions;
	}
	
	@Override
	public List<ElementSuggestion> suggestFirst(ParseTree parseTree, Node parent, 
			String matchWith, Set<String> checkedRules) {
		Node elementNode = new Node(this, parent, null);
		List<InputSuggestion> texts = codeAssist.suggest(parseTree, elementNode, matchWith);
		if (texts != null)
			return Lists.newArrayList(new ElementSuggestion(elementNode, texts));
		else
			return doSuggestFirst(parent, parseTree, matchWith, checkedRules);
	}

	protected abstract List<ElementSuggestion> doSuggestFirst(Node parent, 
			ParseTree parseTree, String matchWith, Set<String> checkedRules);
	
	@Override
	public boolean match(AssistStream stream, Map<String, Integer> checkedIndexes) {
		if (multiplicity == Multiplicity.ONE) {
			return matchOnce(stream, checkedIndexes);
		} else if (multiplicity == Multiplicity.ONE_OR_MORE) {
			if (!matchOnce(stream, checkedIndexes)) {
				return false;
			} else {
				while(matchOnce(stream, checkedIndexes));
				return true;
			}
		} else if (multiplicity == Multiplicity.ZERO_OR_MORE) {
			while (matchOnce(stream, checkedIndexes));
			return true;
		} else {
			matchOnce(stream, checkedIndexes);
			return true;
		}
	}
	
	protected abstract boolean matchOnce(AssistStream stream, Map<String, Integer> indexes);

}
