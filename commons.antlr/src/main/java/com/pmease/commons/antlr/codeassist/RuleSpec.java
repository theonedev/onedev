package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pmease.commons.antlr.codeassist.ElementSpec.Multiplicity;

public class RuleSpec extends Spec {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final List<AlternativeSpec> alternatives;

	public RuleSpec(CodeAssist codeAssist, String name, List<AlternativeSpec> alternatives) {
		super(codeAssist);
		
		this.name = name;
		this.alternatives = alternatives;
	}
	
	public String getName() {
		return name;
	}

	public List<AlternativeSpec> getAlternatives() {
		return alternatives;
	}

	@Override
	public List<ElementSuggestion> suggestFirst(ParseTree parseTree, Node parent, 
			String matchWith, Set<String> checkedRules) {
		parent = new Node(this, parent, null);
		List<ElementSuggestion> first = new ArrayList<>();
		if (!checkedRules.contains(name)) {
			checkedRules.add(name);
			for (AlternativeSpec alternative: alternatives)
				first.addAll(alternative.suggestFirst(parseTree, parent, matchWith, new HashSet<>(checkedRules)));
		} 
		
		return first;
	}

	private Set<Integer> getMandatoryTokenTypesAfter(Node elementNode) {
		ElementSpec elementSpec = (ElementSpec) elementNode.getSpec();
		if (elementSpec.getMultiplicity() == Multiplicity.ONE 
				|| elementSpec.getMultiplicity() == Multiplicity.ZERO_OR_ONE) {
			AlternativeSpec alternativeSpec = (AlternativeSpec) elementNode.getParent().getSpec();
			int specIndex = alternativeSpec.getElements().indexOf(this);
			if (specIndex == alternativeSpec.getElements().size()-1) {
				Node parentElementNode = elementNode.getParent().getParent().getParent();
				if (parentElementNode != null) 
					return getMandatoryTokenTypesAfter(parentElementNode);
			} else {
				ElementSpec nextElementSpec = alternativeSpec.getElements().get(specIndex+1);
				if (nextElementSpec.getMultiplicity() == Multiplicity.ONE
						|| nextElementSpec.getMultiplicity() == Multiplicity.ONE_OR_MORE) {
					Set<Integer> tokenTypes = nextElementSpec.getMandatoryTokenTypes(new HashSet<String>());
					if (tokenTypes.isEmpty())
						return getMandatoryTokenTypesAfter(new Node(nextElementSpec, elementNode.getParent(), null));
				}
			}
		} 
		return new HashSet<>();
	}
	
	@Override
	public List<TokenNode> match(AssistStream stream, Node parent, Node previous, 
			Map<String, Integer> checkedIndexes, boolean fullMatch) {
		List<TokenNode> matches = new ArrayList<>();
		int index = stream.getIndex();
		Integer checkedIndex = checkedIndexes.get(name);
		if (checkedIndex != null && index == checkedIndex) {
			Set<Integer> tokenTypes = getMandatoryTokenTypesAfter(parent);
			if (!tokenTypes.isEmpty()) {
				int tokenIndex = -1;
				for (int i=stream.getIndex(); i<stream.size(); i++) {
					if (tokenTypes.contains(stream.getToken(i).getType())) {
						tokenIndex = i;
						break;
					}
				}
				if (tokenIndex != -1) {
					AssistStream streamBeforeToken = new AssistStream(stream.getTokens().subList(0, tokenIndex));
					matches = match(streamBeforeToken, parent, previous, new HashMap<String, Integer>(), true);
				} else if (stream.isEof() && !fullMatch) {
					matches = match(stream, parent, previous, new HashMap<String, Integer>(), true);
				}
			}
		} else {
			checkedIndexes.put(name, index);
			parent = new Node(this, parent, previous);
			boolean fakedAdded = false;
			for (AlternativeSpec alternative: alternatives) {
				for (TokenNode match: alternative.match(stream, parent, parent, new HashMap<>(checkedIndexes), fullMatch)) {
					if (match.getToken().getTokenIndex() == index-1) {
						if (!fakedAdded) {
							matches.add(new TokenNode(null, parent, parent, new FakedToken(index-1)));
							fakedAdded = true;
						}
					} else {
						matches.add(match);
					}
				}
				stream.setIndex(index);
			}
		}
		codeAssist.prune(matches, stream);
		return matches;
	}

	@Override
	public String toString() {
		return "name: " + name + ", alternatives: " + alternatives;
	}

	@Override
	public Set<Integer> getMandatoryTokenTypes(Set<String> checkedRules) {
		Set<Integer> tokenTypes = new HashSet<>();
		if (!checkedRules.contains(name)) {
			checkedRules.add(name);
			for (AlternativeSpec alternative: alternatives)
				tokenTypes.addAll(alternative.getMandatoryTokenTypes(new HashSet<>(checkedRules)));
		} 
		return tokenTypes;
	}

}
