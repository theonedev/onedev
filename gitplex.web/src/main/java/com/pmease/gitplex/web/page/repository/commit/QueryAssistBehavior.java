package com.pmease.gitplex.web.page.repository.commit;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.ParentedElement;
import com.pmease.commons.antlr.codeassist.SurroundingAware;
import com.pmease.commons.antlr.grammar.LexerRuleRefElementSpec;
import com.pmease.commons.antlr.grammar.LiteralElementSpec;
import com.pmease.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;

@SuppressWarnings("serial")
public class QueryAssistBehavior extends ANTLRAssistBehavior {

	private static final String[] BRANCHS = new String[]{"master", "dev", "feature1", "feature2"};
	
	public QueryAssistBehavior() {
		super(CommitQueryParser.class, "query");
	}

	@Override
	protected List<InputSuggestion> suggest(final ParentedElement element, String matchWith) {
		if (element.getSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) element.getSpec();
			if (spec.getRuleName().equals("Value")) {
				return new SurroundingAware(codeAssist.getGrammar(), "(", ")") {

					@Override
					protected List<InputSuggestion> match(String matchWith) {
						List<InputSuggestion> suggestions = new ArrayList<>();
						if (element.getRoot().getLastMatchedToken().getType() == CommitQueryParser.BRANCH) {
							for (String value: BRANCHS) {
								if (value.toLowerCase().contains(matchWith.toLowerCase()))
									suggestions.add(new InputSuggestion(value));
							}
						}
						return suggestions;
					}
					
				}.suggest(element.getSpec(), matchWith);
			}
		} else if (element.getSpec() instanceof LiteralElementSpec) {
			LiteralElementSpec spec = (LiteralElementSpec) element.getSpec();
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
