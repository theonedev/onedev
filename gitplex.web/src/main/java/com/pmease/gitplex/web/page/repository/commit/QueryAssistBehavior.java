package com.pmease.gitplex.web.page.repository.commit;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.LexerRuleRefElementSpec;
import com.pmease.commons.antlr.codeassist.LiteralElementSpec;
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
									suggestions.add(new InputSuggestion(value));
							}
						}
						return suggestions;
					}
					
				}.suggest(elementNode, matchWith);
			}
		} else if (elementNode.getSpec() instanceof LiteralElementSpec) {
			LiteralElementSpec spec = (LiteralElementSpec) elementNode.getSpec();
			if (spec.getLiteral().toLowerCase().startsWith(matchWith.toLowerCase())) {
				String description;
				switch (spec.getLiteral()) {
				case "branch": 
					description = "commits of branch";
					break;
				case "tag":
					description = "commits of tag";
					break;
				case "id":
					description = "commits of hash";
					break;
				case "committer":
					description = "committed by";
					break;
				case "author":
					description = "authored by";
					break;
				case "message":
					description = "commit message contains";
					break;
				case "before":
					description = "before specified date";
					break;
				case "after":
					description = "after specified date";
					break;
				case "path":
					description = "touching specified path";
					break;
				case "^":
					description = "exclude";
					break;
				case "..":
				case "...":
					description = "range";
					break;
				default:
					description = null;
				}
				return Lists.newArrayList(new InputSuggestion(spec.getLiteral(), description));
			}
		} 
		return null;
	}

}
