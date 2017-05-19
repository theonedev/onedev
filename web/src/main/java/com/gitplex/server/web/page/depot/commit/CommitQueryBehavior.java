package com.gitplex.server.web.page.depot.commit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.gitplex.codeassist.FenceAware;
import com.gitplex.codeassist.InputSuggestion;
import com.gitplex.codeassist.ParentedElement;
import com.gitplex.codeassist.grammar.ElementSpec;
import com.gitplex.codeassist.grammar.LexerRuleRefElementSpec;
import com.gitplex.jsymbol.Range;
import com.gitplex.server.GitPlex;
import com.gitplex.server.git.NameAndEmail;
import com.gitplex.server.manager.CommitInfoManager;
import com.gitplex.server.model.Depot;
import com.gitplex.server.util.StringUtils;
import com.gitplex.server.util.stringmatch.PatternApplied;
import com.gitplex.server.util.stringmatch.WildcardUtils;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.behavior.inputassist.ANTLRAssistBehavior;
import com.gitplex.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class CommitQueryBehavior extends ANTLRAssistBehavior {

	private final IModel<Depot> depotModel;
	
	private static final String VALUE_OPEN = "(";
	
	private static final String VALUE_CLOSE = ")";
	
	private static final String[] DATE_EXAMPLES = new String[]{
			"one hour ago", "2 hours ago", "3PM", "noon", "today", "yesterday", 
			"yesterday midnight", "3 days ago", "last week", "last Monday", 
			"4 weeks ago", "1 month 2 days ago", "1 year ago"}; 
	
	public CommitQueryBehavior(IModel<Depot> depotModel) {
		super(CommitQueryParser.class, "query");
		this.depotModel = depotModel;
	}

	@Override
	public void detach(Component component) {
		super.detach(component);
		depotModel.detach();
	}

	@Override
	protected List<InputSuggestion> suggest(ParentedElement expectedElement, String matchWith) {
		if (expectedElement.getSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) expectedElement.getSpec();
			if (spec.getRuleName().equals("Value")) {
				return new FenceAware(codeAssist.getGrammar(), VALUE_OPEN, VALUE_CLOSE) {

					@Override
					protected List<InputSuggestion> match(String unfencedMatchWith) {
						int tokenType = expectedElement.getRoot().getLastMatchedToken().getType();
						String unfencedLowerCaseMatchWith = unfencedMatchWith.toLowerCase();
						List<InputSuggestion> suggestions = new ArrayList<>();
						Depot depot = depotModel.getObject();
						switch (tokenType) {
						case CommitQueryParser.BRANCH:
							suggestions.addAll(SuggestionUtils.suggestBranch(depot, unfencedMatchWith));
							break;
						case CommitQueryParser.TAG:
							suggestions.addAll(SuggestionUtils.suggestTag(depot, unfencedMatchWith));
							break;
						case CommitQueryParser.AUTHOR:
						case CommitQueryParser.COMMITTER:
							Map<String, Range> suggestedInputs = new LinkedHashMap<>();
							CommitInfoManager commitInfoManager = GitPlex.getInstance(CommitInfoManager.class);
							List<NameAndEmail> contributors;
							if (tokenType == CommitQueryParser.AUTHOR)
								contributors = commitInfoManager.getAuthors(depot);
							else
								contributors = commitInfoManager.getCommitters(depot);
							for (NameAndEmail contributor: contributors) {
								String content;
								if (StringUtils.isNotBlank(contributor.getEmailAddress()))
									content = contributor.getName() + " <" + contributor.getEmailAddress() + ">";
								else
									content = contributor.getName();
								content = content.trim();
								PatternApplied applied = WildcardUtils.applyPattern(unfencedLowerCaseMatchWith, content, 
										false);
								if (applied != null)
									suggestedInputs.put(applied.getText(), applied.getMatchRange());
							}
							
							for (Map.Entry<String, Range> entry: suggestedInputs.entrySet()) 
								suggestions.add(new InputSuggestion(entry.getKey(), -1, true, null, entry.getValue()));
							break;
						case CommitQueryParser.BEFORE:
						case CommitQueryParser.AFTER:
							if (!unfencedMatchWith.contains(VALUE_CLOSE)) {
								if (unfencedMatchWith.length() != 0) {
									String fenced = VALUE_OPEN + unfencedMatchWith + VALUE_CLOSE; 
									Range matchRange = new Range(0, fenced.length());
									suggestions.add(new InputSuggestion(fenced, -1, true, getFencingDescription(), 
											matchRange));
								}
								suggestions.add(new InputSuggestion(
										WebConstants.DATETIME_FORMATTER.print(System.currentTimeMillis())));
								suggestions.add(new InputSuggestion(
										WebConstants.DATE_FORMATTER.print(System.currentTimeMillis())));
								for (String dateExample: DATE_EXAMPLES) { 
									suggestions.add(new InputSuggestion(dateExample));
								}
							}
							break;
						case CommitQueryParser.PATH:
							suggestions.addAll(SuggestionUtils.suggestPath(depotModel.getObject(), unfencedMatchWith));
							break;
						} 
						return suggestions;
					}

					@Override
					protected String getFencingDescription() {
						return "value needs to be enclosed in parenthesis";
					}
					
				}.suggest(expectedElement.getSpec(), matchWith);
			}
		} 
		return null;
	}
	
	@Override
	protected List<String> getHints(ParentedElement expectedElement, String matchWith) {
		List<String> hints = new ArrayList<>();
		if (expectedElement.getSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) expectedElement.getSpec();
			if (spec.getRuleName().equals("Value") && !matchWith.contains(VALUE_CLOSE)) {
				int tokenType = expectedElement.getRoot().getLastMatchedToken().getType();
				if (tokenType == CommitQueryParser.COMMITTER) {
					hints.add("Use * to match any part of committer");
				} else if (tokenType == CommitQueryParser.AUTHOR) {
					hints.add("Use * to match any part of author");
				} else if (tokenType == CommitQueryParser.PATH) {
					hints.add("Use * to match any part of path");
				} else if (tokenType == CommitQueryParser.MESSAGE) {
					hints.add("Use * to match any part of message");
					hints.add("Use '\\\\' to escape special characters in regular expression");
					hints.add("Use '\\(' and '\\)' to represent brackets in message");
				}
			}
		} 
		return hints;
	}

	@Override
	protected int getEndOfMatch(ElementSpec spec, String content) {
		if (content.startsWith(VALUE_OPEN+VALUE_CLOSE))
			return 2;
		else
			return super.getEndOfMatch(spec, content);
	}

	@Override
	protected InputSuggestion wrapAsSuggestion(ParentedElement expectedElement, 
			String suggestedLiteral, boolean complete) {
		String description;
		switch (suggestedLiteral) {
		case "revision":
			description = "any revision string"; 
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
		case " ":
			description = "space";
			break;
		default:
			description = null;
		}
		return new InputSuggestion(suggestedLiteral, description, null);
	}

}
