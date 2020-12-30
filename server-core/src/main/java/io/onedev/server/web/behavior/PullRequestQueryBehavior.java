package io.onedev.server.web.behavior;

import static io.onedev.server.model.Issue.NAME_NUMBER;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.Element;
import io.onedev.commons.codeassist.parser.ParseExpect;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryParser;
import io.onedev.server.util.DateUtils;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class PullRequestQueryBehavior extends ANTLRAssistBehavior {

	private final IModel<Project> projectModel;
	
	public PullRequestQueryBehavior(IModel<Project> projectModel) {
		super(PullRequestQueryParser.class, "query", false);
		this.projectModel = projectModel;
	}

	@Override
	public void detach(Component component) {
		super.detach(component);
		projectModel.detach();
	}
	
	private Project getProject() {
		return projectModel.getObject();
	}

	@Override
	protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
			if (spec.getRuleName().equals("Quoted")) {
				return new FenceAware(codeAssist.getGrammar(), '"', '"') {

					@Override
					protected List<InputSuggestion> match(String matchWith) {
						Project project = getProject();
						if ("criteriaField".equals(spec.getLabel())) {
							List<String> candidates = new ArrayList<>(PullRequest.QUERY_FIELDS);
							if (getProject() != null)
								candidates.remove(PullRequest.NAME_TARGET_PROJECT);
							return SuggestionUtils.suggest(candidates, matchWith);
						} else if ("orderField".equals(spec.getLabel())) {
							List<String> candidates = new ArrayList<>(PullRequest.ORDER_FIELDS.keySet());
							if (getProject() != null)
								candidates.remove(PullRequest.NAME_TARGET_PROJECT);
							return SuggestionUtils.suggest(candidates, matchWith);
						} else if ("criteriaValue".equals(spec.getLabel())) {
							List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
							List<Element> operatorElements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
							Preconditions.checkState(operatorElements.size() == 1);
							String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
							int operator = PullRequestQuery.getOperator(operatorName);							
							if (fieldElements.isEmpty()) {
								if (operator == PullRequestQueryLexer.IncludesIssue)
									return SuggestionUtils.suggestIssues(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
								else if (operator != PullRequestQueryLexer.IncludesCommit)
									return SuggestionUtils.suggestUsers(matchWith);
								else
									return null;
							} else {
								String fieldName = PullRequestQuery.getValue(fieldElements.get(0).getMatchedText());
								try {
									PullRequestQuery.checkField(fieldName, operator);
									if (fieldName.equals(PullRequest.NAME_SUBMIT_DATE) 
											|| fieldName.equals(PullRequest.NAME_UPDATE_DATE)
											|| fieldName.equals(PullRequest.NAME_CLOSE_DATE)) {
										List<InputSuggestion> suggestions = SuggestionUtils.suggest(DateUtils.RELAX_DATE_EXAMPLES, matchWith);
										return !suggestions.isEmpty()? suggestions: null;
									} else if (fieldName.equals(PullRequest.NAME_TARGET_PROJECT)
											|| fieldName.equals(PullRequest.NAME_SOURCE_PROJECT)) {
										if (!matchWith.contains("*"))
											return SuggestionUtils.suggestProjects(matchWith);
										else
											return null;
									} else if (fieldName.equals(PullRequest.NAME_TARGET_BRANCH) 
											|| fieldName.equals(PullRequest.NAME_SOURCE_BRANCH)) {
										if (project != null && !matchWith.contains("*"))
											return SuggestionUtils.suggestBranches(project, matchWith);
										else
											return null;
									} else if (fieldName.equals(NAME_NUMBER)) {
										return SuggestionUtils.suggestPullRequests(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
									} else if (fieldName.equals(PullRequest.NAME_MERGE_STRATEGY)) {
										List<String> candidates = new ArrayList<>();
										for (MergeStrategy strategy: MergeStrategy.values())
											candidates.add(strategy.toString());
										return SuggestionUtils.suggest(candidates, matchWith);
									} else if (fieldName.equals(PullRequest.NAME_TITLE) 
											|| fieldName.equals(PullRequest.NAME_DESCRIPTION) 
											|| fieldName.equals(PullRequest.NAME_COMMENT_COUNT)
											|| fieldName.equals(PullRequest.NAME_COMMENT)) {
										return null;
									}
								} catch (ExplicitException ex) {
								}
							}
						}
						return new ArrayList<>();
					}
					
					@Override
					protected String getFencingDescription() {
						return "quote as literal value";
					}
					
				}.suggest(terminalExpect);
			}
		} 
		return null;
	}
	
	@Override
	protected Optional<String> describe(ParseExpect parseExpect, String suggestedLiteral) {
		parseExpect = parseExpect.findExpectByLabel("operator");
		if (parseExpect != null) {
			List<Element> fieldElements = parseExpect.getState().findMatchedElementsByLabel("criteriaField", false);
			if (!fieldElements.isEmpty()) {
				String fieldName = PullRequestQuery.getValue(fieldElements.iterator().next().getMatchedText());
				try {
					PullRequestQuery.checkField(fieldName, PullRequestQuery.getOperator(suggestedLiteral));
				} catch (ExplicitException e) {
					return null;
				}
			}
		}
		return super.describe(parseExpect, suggestedLiteral);
	}

	@Override
	protected List<String> getHints(TerminalExpect terminalExpect) {
		List<String> hints = new ArrayList<>();
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
			if ("criteriaValue".equals(spec.getLabel()) && ProjectQuery.isInsideQuote(terminalExpect.getUnmatchedText())) {
				List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
				if (!fieldElements.isEmpty()) {
					String fieldName = ProjectQuery.getValue(fieldElements.get(0).getMatchedText());
					if (fieldName.equals(PullRequest.NAME_TARGET_PROJECT)
							|| fieldName.equals(PullRequest.NAME_TARGET_BRANCH) 
							|| fieldName.equals(PullRequest.NAME_SOURCE_PROJECT) 
							|| fieldName.equals(PullRequest.NAME_SOURCE_BRANCH) 
							|| fieldName.equals(PullRequest.NAME_TITLE) 
							|| fieldName.equals(PullRequest.NAME_DESCRIPTION)
							|| fieldName.equals(PullRequest.NAME_COMMENT)) {
						hints.add("Use '*' for wildcard match");
						hints.add("Use '\\' to escape quotes");
					}
				}
			}
		} 
		return hints;
	}

}
