package io.onedev.server.web.behavior;

import static io.onedev.server.model.AbstractEntity.NAME_NUMBER;
import static io.onedev.server.search.entity.EntityQuery.getValue;
import static io.onedev.server.search.entity.pullrequest.PullRequestQuery.checkField;
import static io.onedev.server.search.entity.pullrequest.PullRequestQuery.getOperator;
import static io.onedev.server.search.entity.pullrequest.PullRequestQuery.getRuleName;
import static io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer.ApprovedByMe;
import static io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer.AssignedToMe;
import static io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer.IncludesCommit;
import static io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer.IncludesIssue;
import static io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer.OrderBy;
import static io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer.RequestedForChangesByMe;
import static io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer.SubmittedByMe;
import static io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer.ToBeReviewedByMe;

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
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryParser;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class PullRequestQueryBehavior extends ANTLRAssistBehavior {

	private final IModel<Project> projectModel;
	
	private final boolean withCurrentUserCriteria;
	
	private final boolean withOrder;
	
	public PullRequestQueryBehavior(IModel<Project> projectModel, boolean withCurrentUserCriteria, boolean withOrder) {
		super(PullRequestQueryParser.class, "query", false);
		this.projectModel = projectModel;
		this.withCurrentUserCriteria = withCurrentUserCriteria;
		this.withOrder = withOrder;
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
							int operator = getOperator(operatorName);							
							if (fieldElements.isEmpty()) {
								if (operator == IncludesIssue)
									return SuggestionUtils.suggestIssues(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
								else if (operator != IncludesCommit)
									return SuggestionUtils.suggestUsers(matchWith);
								else
									return null;
							} else {
								String fieldName = getValue(fieldElements.get(0).getMatchedText());
								try {
									checkField(fieldName, operator);
									if (fieldName.equals(PullRequest.NAME_SUBMIT_DATE) 
											|| fieldName.equals(PullRequest.NAME_UPDATE_DATE)
											|| fieldName.equals(PullRequest.NAME_CLOSE_DATE)) {
										List<InputSuggestion> suggestions = SuggestionUtils.suggest(DateUtils.RELAX_DATE_EXAMPLES, matchWith);
										return !suggestions.isEmpty()? suggestions: null;
									} else if (fieldName.equals(PullRequest.NAME_STATUS)) {
										List<String> candidates = new ArrayList<>();
										for (PullRequest.Status status: PullRequest.Status.values())
											candidates.add(status.toString());
										return SuggestionUtils.suggest(candidates, matchWith);
									} else if (fieldName.equals(PullRequest.NAME_TARGET_PROJECT)
											|| fieldName.equals(PullRequest.NAME_SOURCE_PROJECT)) {
										if (!matchWith.contains("*"))
											return SuggestionUtils.suggestProjectPaths(matchWith);
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
									} else if (fieldName.equals(PullRequest.NAME_LABEL)) {
										return SuggestionUtils.suggestLabels(matchWith);
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
						return "value should be quoted";
					}
					
				}.suggest(terminalExpect);
			}
		} 
		return null;
	}
	
	@Override
	protected Optional<String> describe(ParseExpect parseExpect, String suggestedLiteral) {
		if (!withOrder && suggestedLiteral.equals(getRuleName(OrderBy))
				|| !withCurrentUserCriteria && (suggestedLiteral.equals(getRuleName(SubmittedByMe)) 
						|| suggestedLiteral.equals(getRuleName(ToBeReviewedByMe))
						|| suggestedLiteral.equals(getRuleName(RequestedForChangesByMe))
						|| suggestedLiteral.equals(getRuleName(ApprovedByMe))
						|| suggestedLiteral.equals(getRuleName(AssignedToMe)))) {
			return null;
		}
		
		parseExpect = parseExpect.findExpectByLabel("operator");
		if (parseExpect != null) {
			List<Element> fieldElements = parseExpect.getState().findMatchedElementsByLabel("criteriaField", false);
			if (!fieldElements.isEmpty()) {
				String fieldName = getValue(fieldElements.iterator().next().getMatchedText());
				try {
					checkField(fieldName, getOperator(suggestedLiteral));
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
							|| fieldName.equals(PullRequest.NAME_TARGET_BRANCH)) {
						hints.add("Use '**', '*', or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>");
					} else if (fieldName.equals(PullRequest.NAME_TITLE) 
							|| fieldName.equals(PullRequest.NAME_DESCRIPTION)
							|| fieldName.equals(PullRequest.NAME_COMMENT)
							|| fieldName.equals(PullRequest.NAME_SOURCE_PROJECT) 
							|| fieldName.equals(PullRequest.NAME_SOURCE_BRANCH)) {
						hints.add("Use '*' for wildcard match");
						hints.add("Use '\\' to escape quotes");
					}
				}
			}
		} 
		return hints;
	}

}
