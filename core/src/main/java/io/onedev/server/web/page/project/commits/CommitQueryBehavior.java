package io.onedev.server.web.page.project.commits;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import io.onedev.codeassist.FenceAware;
import io.onedev.codeassist.InputSuggestion;
import io.onedev.codeassist.ParentedElement;
import io.onedev.codeassist.grammar.ElementSpec;
import io.onedev.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.server.OneDev;
import io.onedev.server.git.NameAndEmail;
import io.onedev.server.manager.CommitInfoManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.Constants;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.utils.Range;
import io.onedev.utils.StringUtils;
import io.onedev.utils.stringmatch.PatternApplied;
import io.onedev.utils.stringmatch.WildcardUtils;

@SuppressWarnings("serial")
public class CommitQueryBehavior extends ANTLRAssistBehavior {

	private final IModel<Project> projectModel;
	
	private static final String VALUE_OPEN = "(";
	
	private static final String VALUE_CLOSE = ")";
	
	private static final String ESCAPE_CHARS = "\\()";
	
	private static final List<String> DATE_EXAMPLES = Lists.newArrayList(
			"one hour ago", "2 hours ago", "3PM", "noon", "today", "yesterday", 
			"yesterday midnight", "3 days ago", "last week", "last Monday", 
			"4 weeks ago", "1 month 2 days ago", "1 year ago"); 
	
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
							suggestions.addAll(SuggestionUtils.suggestBranch(project, unfencedMatchWith, ESCAPE_CHARS));
							break;
						case CommitQueryParser.TAG:
							suggestions.addAll(SuggestionUtils.suggestTag(project, unfencedMatchWith, ESCAPE_CHARS));
							break;
						case CommitQueryParser.AUTHOR:
						case CommitQueryParser.COMMITTER:
							Map<String, Range> suggestedInputs = new LinkedHashMap<>();
							CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class);
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
									suggestedInputs.put(applied.getText(), applied.getMatch());
							}
							
							for (Map.Entry<String, Range> entry: suggestedInputs.entrySet()) 
								suggestions.add(new InputSuggestion(entry.getKey(), -1, true, null, entry.getValue()).escape(ESCAPE_CHARS));
							break;
						case CommitQueryParser.BEFORE:
						case CommitQueryParser.AFTER:
							List<String> candidates = new ArrayList<>(DATE_EXAMPLES);
							candidates.add(Constants.DATETIME_FORMATTER.print(System.currentTimeMillis()));
							candidates.add(Constants.DATE_FORMATTER.print(System.currentTimeMillis()));
							suggestions.addAll(getSuggestions(candidates, unfencedLowerCaseMatchWith, null));
							CollectionUtils.addIgnoreNull(suggestions, suggestToFence(unfencedMatchWith));
							break;
						case CommitQueryParser.PATH:
							suggestions.addAll(SuggestionUtils.suggestPath(projectModel.getObject(), unfencedMatchWith, ESCAPE_CHARS));
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
	protected Optional<String> describe(ParentedElement expectedElement, String suggestedLiteral) {
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
		return Optional.fromNullable(description);
	}

}
