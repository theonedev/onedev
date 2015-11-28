package com.pmease.gitplex.web.page.repository.commit;

import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.LexerRuleRefElementSpec;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;
import com.pmease.commons.antlr.codeassist.SurroundingAware;
import com.pmease.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;

@SuppressWarnings("serial")
public class QueryAssistBehavior extends ANTLRAssistBehavior {

	private static final String[] BRANCHS = new String[]{"master", "dev", "feature1", "feature2"};
	
	public QueryAssistBehavior() {
		super(CommitQueryParser.class, "query");
	}

	@Override
	protected List<InputSuggestion> suggest(final ParseTree parseTree, Node elementNode, String matchWith) {
		if (elementNode.getSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) elementNode.getSpec();
			if (spec.getRuleName().equals("Value")) {
				return new SurroundingAware(codeAssist, "(", ")") {

					@Override
					protected List<InputSuggestion> match(String matchWith) {
						List<InputSuggestion> suggestions = new ArrayList<>();
						if (parseTree.getLastNode().getToken().getType() == CommitQueryParser.BRANCH) {
							for (String value: BRANCHS) {
								if (value.toLowerCase().contains(matchWith.toLowerCase()))
									suggestions.add(new InputSuggestion(value, value));
							}
						}
						return suggestions;
					}
					
				}.suggest(elementNode, matchWith);
			}
		}
		return null;
	}

}
