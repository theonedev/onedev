package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
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
			Map<String, Set<RuleRefContext>> ruleRefHistory) {
		if (multiplicity == Multiplicity.ONE) {
			return matchOnce(stream, parent, previous, ruleRefHistory);
		} else if (multiplicity == Multiplicity.ONE_OR_MORE) {
			List<TokenNode> paths = matchOnce(stream, parent, previous, ruleRefHistory);
			if (paths == null || paths.isEmpty() || stream.isEof()) {
				return paths;
			} else {
				while (true) {
					previous = paths.get(paths.size()-1);
					List<TokenNode> nextPaths = matchOnce(stream, parent, previous, ruleRefHistory);
					if (nextPaths == null || nextPaths.isEmpty())
						return paths;
					else if (stream.isEof())
						return nextPaths;
					else 
						paths = nextPaths;
				}
			}
		} else if (multiplicity == Multiplicity.ZERO_OR_MORE) {
			List<TokenNode> paths = new ArrayList<>();
			while (true) {
				if (!paths.isEmpty())
					previous = paths.get(paths.size()-1);
				List<TokenNode> nextPaths = matchOnce(stream, parent, previous, ruleRefHistory);
				if (nextPaths == null || nextPaths.isEmpty() || stream.isEof()) 
					return paths;
				else 
					paths = nextPaths;
			}
		} else {
			List<TokenNode> paths = matchOnce(stream, parent, previous, ruleRefHistory);
			if (paths != null)
				return paths;
			else
				return new ArrayList<>();
		}
	}
	
	public abstract List<TokenNode> matchOnce(AssistStream stream, Node parent, Node previous, 
			Map<String, Set<RuleRefContext>> ruleRefHistory);

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

		if (this instanceof RuleRefElementSpec) {
			Node ruleRefNode = new Node(this, parent, null);
			RuleRefElementSpec ruleRefElementSpec = (RuleRefElementSpec) this;
			RuleSpec ruleSpec = ruleRefElementSpec.getRule();
			Node ruleNode = new Node(ruleSpec, ruleRefNode, null);
			for (AlternativeSpec alternative: ruleRefElementSpec.getRule().getAlternatives()) {
				Node alternativeNode = new Node(alternative, ruleNode, null);
				if (alternative.getElements().size() > 1 
						&& alternative.getElements().get(0) instanceof RuleRefElementSpec) {
					RuleRefElementSpec firstElementSpec = (RuleRefElementSpec) alternative.getElements().get(0);
					if (firstElementSpec.getRuleName().equals(ruleRefElementSpec.getRuleName())) {
						for (int i=1; i<alternative.getElements().size(); i++) {
							ElementSpec elementSpec = alternative.getElements().get(i);
							suggestions.addAll(elementSpec.suggestFirst(parseTree, alternativeNode, 
									matchWith, new HashSet<String>()));
							if (!elementSpec.matches(codeAssist.lex("")))
								break;
						}
					}
				}
			}
		}
		
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
				// user suppresses suggestion explicitly, so we only provide suggestion if there are 
				// mandatory followings
				return doSuggestFirst(parseTree, parent, matchWith, checkedRules);
			else
				return Lists.newArrayList(new ElementSuggestion(parseTree, elementNode, matchWith, suggestions));
		} else {
			// user does not care about suggestion, so we continue with our default logic to drill down
			return doSuggestFirst(parseTree, parent, matchWith, checkedRules);
		}
	}

	protected abstract List<ElementSuggestion> doSuggestFirst(@Nullable ParseTree parseTree, 
			Node parent, String matchWith, Set<String> checkedRules);

	public final String toString() {
		if (multiplicity == Multiplicity.ONE)
			return asString();
		else if (multiplicity == Multiplicity.ONE_OR_MORE)
			return "(" + asString() + ")+";
		else if (multiplicity == Multiplicity.ZERO_OR_MORE)
			return "(" + asString() + ")*";
		else
			return "(" + asString() + ")?";
	}
	
	protected abstract String asString();
}
