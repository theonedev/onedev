package io.onedev.server.web.behavior;

import static io.onedev.server.web.translation.Translation._T;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.ParseExpect;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.match.PatternApplied;
import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.search.commit.CommitQueryParser;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.NameAndEmail;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.xodus.CommitInfoService;

public class CommitQueryBehavior extends ANTLRAssistBehavior {

	private static final String FUZZY_SUGGESTION_DESCRIPTION_PREFIX = "enclose with ~";
	
	private final IModel<Project> projectModel;
	
	private final boolean withCurrentUserCriteria;
	
	private static final List<String> DATE_EXAMPLES = Lists.newArrayList(
			"one hour ago", "2 hours ago", "3PM", "noon", "today", "yesterday", 
			"yesterday midnight", "3 days ago", "last week", "last Monday", 
			"4 weeks ago", "1 month 2 days ago", "1 year ago"); 
	
	public CommitQueryBehavior(IModel<Project> projectModel, boolean withCurrentUserCriteria) {
		super(CommitQueryParser.class, "query", false);
		this.projectModel = projectModel;
		this.withCurrentUserCriteria = withCurrentUserCriteria;
	}
	
	@Override
	public void detach(Component component) {
		super.detach(component);
		projectModel.detach();
	}

	@Override
	protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
			if (spec.getRuleName().equals("Value")) {
				return new FenceAware(codeAssist.getGrammar(), '(', ')') {

					@Override
					protected List<InputSuggestion> match(String matchWith) {
						int tokenType = terminalExpect.getState().getFirstMatchedToken().getType();
						Project project = projectModel.getObject();
						switch (tokenType) {
							case CommitQueryParser.BRANCH:
								return SuggestionUtils.suggestBranches(project, matchWith);
							case CommitQueryParser.TAG:
								return SuggestionUtils.suggestTags(project, matchWith);
							case CommitQueryParser.BUILD:
								return SuggestionUtils.suggestBuilds(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
							case CommitQueryParser.AUTHOR:
							case CommitQueryParser.COMMITTER:
								Map<String, LinearRange> suggestedInputs = new LinkedHashMap<>();
								List<NameAndEmail> users = getCommitInfoManager().getUsers(project.getId());
								for (NameAndEmail user: users) {
									String content;
									if (StringUtils.isNotBlank(user.getEmailAddress()))
										content = user.getName() + " <" + user.getEmailAddress() + ">";
									else
										content = user.getName() + " <>";
									content = content.trim();
									PatternApplied applied = WildcardUtils.applyStringPattern(matchWith, content, false);
									if (applied != null)
										suggestedInputs.put(applied.getText(), applied.getMatch());
								}
								
								List<InputSuggestion> suggestions = new ArrayList<>();
								for (Map.Entry<String, LinearRange> entry: suggestedInputs.entrySet()) 
									suggestions.add(new InputSuggestion(entry.getKey(), -1, null, entry.getValue()));
								return suggestions;
							case CommitQueryParser.BEFORE:
							case CommitQueryParser.AFTER:
								List<String> candidates = new ArrayList<>(DATE_EXAMPLES);
								var currentDate = ZonedDateTime.now(DateUtils.getZoneId());
								candidates.add(currentDate.format(DateUtils.DATETIME_FORMATTER));
								candidates.add(currentDate.format(DateUtils.DATE_FORMATTER));
								suggestions = SuggestionUtils.suggest(candidates, matchWith);
								return !suggestions.isEmpty()? suggestions: null;
							case CommitQueryParser.PATH:
								return SuggestionUtils.suggestPathsByStringPattern(getCommitInfoManager().getFiles(projectModel.getObject().getId()), matchWith, false);
							default: 
								return null;
						} 
					}

					@Override
					protected String getFencingDescription() {
						return _T("value needs to be enclosed in parenthesis");
					}
					
				}.suggest(terminalExpect);
			} else if (spec.getRuleName().equals("Fuzzy")) {

				return new FenceAware(codeAssist.getGrammar(), '~', '~') {

					@Override
					protected List<InputSuggestion> match(String matchWith) {
						return null;
					}

					@Override
					protected String getFencingDescription() {
						return _T("enclose with ~ to query hash/message");
					}

				}.suggest(terminalExpect);
			}
		} 
		return null;
	}

	private CommitInfoService getCommitInfoManager() {
		return OneDev.getInstance(CommitInfoService.class);
	}
	
	@Override
	protected List<String> getHints(TerminalExpect terminalExpect) {
		List<String> hints = new ArrayList<>();
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
			if (spec.getRuleName().equals("Value") && !terminalExpect.getUnmatchedText().contains(")")) {
				int tokenType = terminalExpect.getState().getLastMatchedToken().getType();
				if (tokenType == CommitQueryParser.COMMITTER 
						|| tokenType == CommitQueryParser.AUTHOR
						|| tokenType == CommitQueryParser.PATH
						|| tokenType == CommitQueryParser.MESSAGE) {
					hints.add(_T("Use '*' for wildcard match"));
					hints.add(_T("Use '\\' to escape brackets"));
				}
			}
		} 
		return hints;
	}

	@Override
	protected Optional<String> describe(ParseExpect parseExpect, String suggestedLiteral) {
		if (!withCurrentUserCriteria 
				&& (suggestedLiteral.equals("authored-by-me") || suggestedLiteral.equals("committed-by-me"))) { 
			return null;
		}
		String description;
		switch (suggestedLiteral) {
			case "committer":
				description = _T("committed by");
				break;
			case "author":
				description = _T("authored by");
				break;
			case "message":
				description = _T("commit message contains");
				break;
			case "before":
				description = _T("before specified date");
				break;
			case "after":
				description = _T("after specified date");
				break;
			case "path":
				description = _T("touching specified path");
				break;
			case " ":
				description = _T("space");
				break;
			default:
				description = null;
		}
		return Optional.fromNullable(description);
	}

	@Override
	protected boolean isFuzzySuggestion(InputCompletion suggestion) {
		return suggestion.getDescription() != null 
				&& suggestion.getDescription().startsWith(FUZZY_SUGGESTION_DESCRIPTION_PREFIX);
	}
	
}
