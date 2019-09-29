package io.onedev.server.web.behavior;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.ParseExpect;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.match.PatternApplied;
import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cache.CommitInfoManager;
import io.onedev.server.git.NameAndEmail;
import io.onedev.server.model.Project;
import io.onedev.server.search.commit.CommitQueryParser;
import io.onedev.server.util.Constants;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class CommitQueryBehavior extends ANTLRAssistBehavior {

	private final IModel<Project> projectModel;
	
	private static final List<String> DATE_EXAMPLES = Lists.newArrayList(
			"one hour ago", "2 hours ago", "3PM", "noon", "today", "yesterday", 
			"yesterday midnight", "3 days ago", "last week", "last Monday", 
			"4 weeks ago", "1 month 2 days ago", "1 year ago"); 
	
	public CommitQueryBehavior(IModel<Project> projectModel) {
		super(CommitQueryParser.class, "query", false);
		this.projectModel = projectModel;
	}

	@Override
	public void detach(Component component) {
		super.detach(component);
		projectModel.detach();
	}

	private List<InputSuggestion> escape(List<InputSuggestion> suggestions) {
		return suggestions.stream().map(it->it.escape("()")).collect(Collectors.toList());
	}
	
	@Override
	protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
			if (spec.getRuleName().equals("Value")) {
				return new FenceAware(codeAssist.getGrammar(), "(", ")") {

					@Override
					protected List<InputSuggestion> match(String matchWith) {
						String normalizedMatchWith = ReviewRequirement.unescapeBraces(matchWith.toLowerCase());
						int tokenType = terminalExpect.getState().getLastMatchedToken().getType();
						List<InputSuggestion> suggestions = new ArrayList<>();
						Project project = projectModel.getObject();
						switch (tokenType) {
						case CommitQueryParser.BRANCH:
							suggestions.addAll(escape(SuggestionUtils.suggestBranches(project, normalizedMatchWith)));
							break;
						case CommitQueryParser.TAG:
							suggestions.addAll(escape(SuggestionUtils.suggestTags(project, normalizedMatchWith)));
							break;
						case CommitQueryParser.BUILD:
							suggestions.addAll(escape(SuggestionUtils.suggestBuilds(project, normalizedMatchWith)));
							break;
						case CommitQueryParser.AUTHOR:
						case CommitQueryParser.COMMITTER:
							Map<String, LinearRange> suggestedInputs = new LinkedHashMap<>();
							CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class);
							List<NameAndEmail> users = commitInfoManager.getUsers(project);
							for (NameAndEmail user: users) {
								String content;
								if (StringUtils.isNotBlank(user.getEmailAddress()))
									content = user.getName() + " <" + user.getEmailAddress() + ">";
								else
									content = user.getName();
								content = content.trim();
								PatternApplied applied = WildcardUtils.applyPattern(normalizedMatchWith, content, false);
								if (applied != null)
									suggestedInputs.put(applied.getText(), applied.getMatch());
							}
							
							for (Map.Entry<String, LinearRange> entry: suggestedInputs.entrySet()) 
								suggestions.add(new InputSuggestion(entry.getKey(), -1, null, entry.getValue()).escape("()"));
							break;
						case CommitQueryParser.BEFORE:
						case CommitQueryParser.AFTER:
							List<String> candidates = new ArrayList<>(DATE_EXAMPLES);
							candidates.add(Constants.DATETIME_FORMATTER.print(System.currentTimeMillis()));
							candidates.add(Constants.DATE_FORMATTER.print(System.currentTimeMillis()));
							suggestions.addAll(SuggestionUtils.suggest(candidates, normalizedMatchWith));
							CollectionUtils.addIgnoreNull(suggestions, suggestToFence(terminalExpect, matchWith));
							break;
						case CommitQueryParser.PATH:
							suggestions.addAll(escape(SuggestionUtils.suggestBlobs(projectModel.getObject(), normalizedMatchWith)));
							break;
						default: 
							return null;
						} 
						return suggestions;
					}

					@Override
					protected String getFencingDescription() {
						return "value needs to be enclosed in parenthesis";
					}
					
				}.suggest(terminalExpect);
			}
		} 
		return null;
	}
	
	@Override
	protected List<String> getHints(TerminalExpect terminalExpect) {
		List<String> hints = new ArrayList<>();
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
			if (spec.getRuleName().equals("Value") && !terminalExpect.getUnmatchedText().contains(")")) {
				int tokenType = terminalExpect.getState().getLastMatchedToken().getType();
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
	protected Optional<String> describe(ParseExpect parseExpect, String suggestedLiteral) {
		String description;
		switch (suggestedLiteral) {
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
