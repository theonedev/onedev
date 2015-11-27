package com.pmease.gitplex.web.page.repository.commit;

import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.LexerRuleRefElementSpec;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;
import com.pmease.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;

@SuppressWarnings("serial")
public class QueryAssistBehavior extends ANTLRAssistBehavior {

	private static final String[] BRANCHS = new String[]{"master", "dev", "feature1", "feature2"};
	
	public QueryAssistBehavior() {
		super(CommitQueryParser.class, "query");
	}

	@Override
	protected List<InputSuggestion> suggest(ParseTree parseTree, Node elementNode, String matchWith) {
		if (elementNode.getSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) elementNode.getSpec();
			if (spec.getRuleName().equals("Value") 
					&& parseTree.getLastNode().getToken().getType() == CommitQueryParser.BRANCH) {
				List<InputSuggestion> suggestions = new ArrayList<>();
				for (String value: BRANCHS) {
					String bracedValue = "(" + value + ")";
					if (bracedValue.toLowerCase().contains(matchWith.toLowerCase()))
						suggestions.add(new InputSuggestion(bracedValue, bracedValue));
				}
				return suggestions;
			}
		}
		return null;
	}

}
