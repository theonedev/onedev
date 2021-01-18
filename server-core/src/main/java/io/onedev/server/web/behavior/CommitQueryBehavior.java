package io.onedev.server.web.behavior;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import io.onedev.server.OneDev;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.model.Project;
import io.onedev.server.search.commit.CommitQueryParser;
import io.onedev.server.util.Constants;
import io.onedev.server.util.NameAndEmail;
import io.onedev.server.util.match.PatternApplied;
import io.onedev.server.util.match.WildcardUtils;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
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

	@Override
	protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
			if (spec.getRuleName().equals("Value")) {
				return new FenceAware(codeAssist.getGrammar(), '(', ')') {

					@Override
					protected List<InputSuggestion> match(String matchWith) {
						int tokenType = terminalExpect.getState().getLastMatchedToken().getType();
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
							CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class);
							List<NameAndEmail> users = commitInfoManager.getUsers(project);
							for (NameAndEmail user: users) {
								String content;
								if (StringUtils.isNotBlank(user.getEmailAddress()))
									content = user.getName() + " <" + user.getEmailAddress() + ">";
								else
									content = user.getName() + " <>";
								content = content.trim();
								PatternApplied applied = WildcardUtils.applyPattern(matchWith, content, false);
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
							candidates.add(Constants.DATETIME_FORMATTER.print(System.currentTimeMillis()));
							candidates.add(Constants.DATE_FORMATTER.print(System.currentTimeMillis()));
							suggestions = SuggestionUtils.suggest(candidates, matchWith);
							return !suggestions.isEmpty()? suggestions: null;
						case CommitQueryParser.PATH:
							return SuggestionUtils.suggestBlobs(projectModel.getObject(), matchWith);
						default: 
							return null;
						} 
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
				if (tokenType == CommitQueryParser.COMMITTER 
						|| tokenType == CommitQueryParser.AUTHOR
						|| tokenType == CommitQueryParser.PATH
						|| tokenType == CommitQueryParser.MESSAGE) {
					hints.add("Use '*' for wildcard match");
					hints.add("Use '\\' to escape brackets");
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
