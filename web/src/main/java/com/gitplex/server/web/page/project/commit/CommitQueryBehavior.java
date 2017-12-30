package com.gitplex.server.web.page.project.commit;

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
import com.gitplex.utils.Range;
import com.gitplex.utils.StringUtils;
import com.gitplex.utils.stringmatch.PatternApplied;
import com.gitplex.utils.stringmatch.WildcardUtils;
import com.gitplex.server.GitPlex;
import com.gitplex.server.git.NameAndEmail;
import com.gitplex.server.manager.CommitInfoManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.behavior.inputassist.ANTLRAssistBehavior;
import com.gitplex.server.web.page.project.commit.CommitQueryParser;
import com.gitplex.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class CommitQueryBehavior extends ANTLRAssistBehavior {

	private final IModel<Project> projectModel;
	
	private static final String VALUE_OPEN = "(";
	
	private static final String VALUE_CLOSE = ")";
	
	private static final String[] DATE_EXAMPLES = new String[]{
			"one hour ago", "2 hours ago", "3PM", "noon", "today", "yesterday", 
			"yesterday midnight", "3 days ago", "last week", "last Monday", 
			"4 weeks ago", "1 month 2 days ago", "1 year ago"}; 
	
	public CommitQueryBehavior(IModel<Project> projectModel) {
		super(CommitQueryParser.class, "query");
		this.projectModel = projectModel;
	}

	@Override
	public void detach(Component component) {
		super.detach(component);
		projectModel.detach();
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
						Project project = projectModel.getObject();
						switch (tokenType) {
						case CommitQueryParser.BRANCH:
							suggestions.addAll(SuggestionUtils.suggestBranch(project, unfencedMatchWith));
							break;
						case CommitQueryParser.TAG:
							suggestions.addAll(SuggestionUtils.suggestTag(project, unfencedMatchWith));
							break;
						case CommitQueryParser.AUTHOR:
						case CommitQueryParser.COMMITTER:
							Map<String, Range> suggestedInputs = new LinkedHashMap<>();
							CommitInfoManager commitInfoManager = GitPlex.getInstance(CommitInfoManager.class);
							List<NameAndEmail> users = commitInfoManager.getUsers(project);
							for (NameAndEmail user: users) {
								String content;
								if (StringUtils.isNotBlank(user.getEmailAddress()))
									content = user.getName() + " <" + user.getEmailAddress() + ">";
								else
									content = user.getName();
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
							suggestions.addAll(SuggestionUtils.suggestPath(projectModel.getObject(), unfencedMatchWith));
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
