package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class AlternativeSpec extends Spec {

	private static final long serialVersionUID = 1L;

	private final String label;
	
	private final List<ElementSpec> elements;
	
	public AlternativeSpec(CodeAssist codeAssist, String label, List<ElementSpec> elements) {
		super(codeAssist);
		
		this.label = label;
		this.elements = elements;
	}

	public String getLabel() {
		return label;
	}

	public List<ElementSpec> getElements() {
		return elements;
	}
	
	@Override
	public List<TokenNode> match(AssistStream stream, Node parent, Node previous, boolean fullMatch) {
		parent = new Node(this, parent, previous);
		List<TokenNode> stopMatches = new ArrayList<>();
		List<TokenNode> matches = initMatches(stream, parent, parent);
		if (!stream.isEof() || fullMatch) {
			for (ElementSpec elementSpec: elements) {
				matches = elementSpec.match(matches, stream, parent, fullMatch);
				if (!fullMatch) {
					for (Iterator<TokenNode> it = matches.iterator(); it.hasNext();) {
						TokenNode match = it.next();
						if (match.getToken().getTokenIndex() == stream.size()-1) {
							stopMatches.add(match);
							it.remove();
						}
					}
				}
				codeAssist.prune(matches, stream);
				if (matches.isEmpty())
					break;
			}
		}
		matches.addAll(stopMatches);
		return matches;
	}

	@Override
	public List<ElementSuggestion> suggestFirst(ParseTree parseTree, Node parent, 
			String matchWith, Set<String> checkedRules) {
		List<ElementSuggestion> first = new ArrayList<>();
		parent = new Node(this, parent, null);
		for (ElementSpec element: elements) {
			first.addAll(element.suggestFirst(parseTree, parent, matchWith, new HashSet<>(checkedRules)));
			
			// consider next element if current element is optional
			if (!element.matchesEmpty())
				break;
		}
		return first;
	}

	@Override
	public Set<Integer> getFirstTokenTypes() {
		Set<Integer> leadingTokenTypes = new HashSet<>();
		for (ElementSpec elementSpec: elements) {
			Set<Integer> elementLeadingTokenTypes = elementSpec.getFirstTokenTypes();
			if (elementLeadingTokenTypes == null) {
				leadingTokenTypes = null;
				break;
			} else {
				leadingTokenTypes.addAll(elementLeadingTokenTypes);
				if (!elementSpec.matchesEmpty())
					break;
			}
		}
		return leadingTokenTypes;
	}

	@Override
	public boolean matchesEmpty() {
		for (ElementSpec elementSpec: elements) {
			if (!elementSpec.matchesEmpty())
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		List<String> elementStrings = new ArrayList<>();
		for (ElementSpec element: elements)
			elementStrings.add(element.toString());
		return StringUtils.join(elementStrings, " ");
	}

}
