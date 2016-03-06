package com.pmease.gitplex.web.page.depot.commit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.pmease.commons.antlr.codeassist.FenceAware;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.ParentedElement;
import com.pmease.commons.antlr.grammar.ElementSpec;
import com.pmease.commons.antlr.grammar.LexerRuleRefElementSpec;
import com.pmease.commons.git.NameAndEmail;
import com.pmease.commons.util.Range;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.util.match.WildcardApplied;
import com.pmease.commons.util.match.WildcardUtils;
import com.pmease.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.AuxiliaryManager;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class QueryAssistBehavior extends ANTLRAssistBehavior {

	private final IModel<Depot> depotModel;
	
	private static final String VALUE_OPEN = "(";
	
	private static final String VALUE_CLOSE = ")";
	
	private static final String[] DATE_EXAMPLES = new String[]{
			"one hour ago", "2 hours ago", "3PM", "noon", "today", "yesterday", 
			"yesterday midnight", "3 days ago", "last week", "last Monday", 
			"4 weeks ago", "1 month 2 days ago", "1 year ago"}; 
	
	public QueryAssistBehavior(IModel<Depot> depotModel) {
		super(CommitQueryParser.class, "query");
		this.depotModel = depotModel;
	}

	@Override
	public void detach(Component component) {
		super.detach(component);
		depotModel.detach();
	}

	@Override
	protected List<InputSuggestion> suggest(final ParentedElement expectedElement, String matchWith, int count) {
		if (expectedElement.getSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) expectedElement.getSpec();
			if (spec.getRuleName().equals("Value")) {
				return new FenceAware(codeAssist.getGrammar(), VALUE_OPEN, VALUE_CLOSE) {

					@Override
					protected List<InputSuggestion> match(String unfencedMatchWith, int count) {
						int tokenType = expectedElement.getRoot().getLastMatchedToken().getType();
						String unfencedLowerCaseMatchWith = unfencedMatchWith.toLowerCase();
						List<InputSuggestion> suggestions = new ArrayList<>();
						Depot depot = depotModel.getObject();
						switch (tokenType) {
						case CommitQueryParser.BRANCH:
							suggestions.addAll(SuggestionUtils.suggestBranch(depot, unfencedMatchWith, count, null, null));
							break;
						case CommitQueryParser.TAG:
							suggestions.addAll(SuggestionUtils.suggestTag(depot, unfencedMatchWith, count));
							break;
						case CommitQueryParser.AUTHOR:
						case CommitQueryParser.COMMITTER:
							Map<String, Range> suggestedInputs = new LinkedHashMap<>();
							AuxiliaryManager auxiliaryManager = GitPlex.getInstance(AuxiliaryManager.class);
							List<NameAndEmail> contributors = auxiliaryManager.getContributors(depot);
							for (NameAndEmail contributor: contributors) {
								String content;
								if (StringUtils.isNotBlank(contributor.getEmailAddress()))
									content = contributor.getName() + " <" + contributor.getEmailAddress() + ">";
								else
									content = contributor.getName();
								content = content.trim();
								WildcardApplied applied = WildcardUtils.applyWildcard(content, unfencedLowerCaseMatchWith, false);
								if (applied != null) {
									suggestedInputs.put(applied.getText(), applied.getMatchRange());
									if (suggestedInputs.size() == count)
										break;
								}
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
									if (suggestions.size() < count)
										suggestions.add(new InputSuggestion(fenced, -1, true, getFencingDescription(), matchRange));
								}
								if (suggestions.size() < count)
									suggestions.add(new InputSuggestion(Constants.DATETIME_FORMATTER.print(System.currentTimeMillis())));
								if (suggestions.size() < count)
									suggestions.add(new InputSuggestion(Constants.DATE_FORMATTER.print(System.currentTimeMillis())));
								for (String dateExample: DATE_EXAMPLES) { 
									if (suggestions.size() < count)
										suggestions.add(new InputSuggestion(dateExample));
								}
							}
							break;
						case CommitQueryParser.PATH:
							suggestions.addAll(SuggestionUtils.suggestPath(depotModel.getObject(), unfencedMatchWith, count));
							break;
						} 
						return suggestions;
					}

					@Override
					protected String getFencingDescription() {
						return "value needs to be enclosed in brackets";
					}
					
				}.suggest(expectedElement.getSpec(), matchWith, count);
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
					hints.add("Git log basic regular expression is accepted here");
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
			description = "exclude revision";
			break;
		case "..":
		case "...":
			description = "revision range";
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
