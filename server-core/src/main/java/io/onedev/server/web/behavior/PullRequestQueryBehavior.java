package io.onedev.server.web.behavior;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputCompletion;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.onedev.server.model.AbstractEntity.NAME_NUMBER;
import static io.onedev.server.search.entity.EntityQuery.getValue;
import static io.onedev.server.search.entity.pullrequest.PullRequestQuery.*;
import static io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer.*;
import static io.onedev.server.web.translation.Translation._T;

public class PullRequestQueryBehavior extends ANTLRAssistBehavior {

	private static final String FUZZY_SUGGESTION_DESCRIPTION_PREFIX = "enclose with ~";
	
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
			if (spec.getRuleName().equals("Number")) {
				return SuggestionUtils.suggestNumber(
						terminalExpect.getUnmatchedText(),
						_T("find pull request with this number"));
			} else if (spec.getRuleName().equals("Quoted")) {
				return new FenceAware(codeAssist.getGrammar(), '"', '"') {

					@Override
					protected List<InputSuggestion> match(String matchWith) {
						Project project = getProject();
						ParseExpect criteriaValueExpect;
						if ("criteriaField".equals(spec.getLabel())) {
							List<String> candidates = new ArrayList<>(PullRequest.QUERY_FIELDS.stream()
									.filter(it -> !it.startsWith("Reaction: "))
									.collect(Collectors.toList()));						
							if (getProject() != null)
								candidates.remove(PullRequest.NAME_TARGET_PROJECT);
							candidates.addAll(PullRequest.QUERY_FIELDS.stream()
									.filter(it -> it.startsWith("Reaction: "))
									.collect(Collectors.toList()));
							return SuggestionUtils.suggest(candidates, matchWith);
						} else if ("orderField".equals(spec.getLabel())) {
							List<String> candidates = new ArrayList<>(PullRequest.SORT_FIELDS.keySet().stream()
									.filter(it -> !it.startsWith("Reaction: "))
									.collect(Collectors.toList()));
							if (getProject() != null)
								candidates.remove(PullRequest.NAME_TARGET_PROJECT);
							candidates.addAll(PullRequest.SORT_FIELDS.keySet().stream()
									.filter(it -> it.startsWith("Reaction: "))
									.collect(Collectors.toList()));
							return SuggestionUtils.suggest(candidates, matchWith);
						} else if ((criteriaValueExpect = terminalExpect.findExpectByLabel("criteriaValue")) != null) {
							List<Element> fieldElements = criteriaValueExpect.getState().findMatchedElementsByLabel("criteriaField", true);
							List<Element> operatorElements = criteriaValueExpect.getState().findMatchedElementsByLabel("operator", true);
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
									switch (fieldName) {
										case PullRequest.NAME_SUBMIT_DATE:
										case PullRequest.NAME_LAST_ACTIVITY_DATE:
										case PullRequest.NAME_CLOSE_DATE:
											List<InputSuggestion> suggestions = SuggestionUtils.suggest(DateUtils.RELAX_DATE_EXAMPLES, matchWith);
											return !suggestions.isEmpty() ? suggestions : null;
										case PullRequest.NAME_TARGET_PROJECT:
										case PullRequest.NAME_SOURCE_PROJECT:
											if (!matchWith.contains("*"))
												return SuggestionUtils.suggestProjectPaths(matchWith);
											else
												return null;
										case PullRequest.NAME_TARGET_BRANCH:
										case PullRequest.NAME_SOURCE_BRANCH:
											if (project != null && !matchWith.contains("*"))
												return SuggestionUtils.suggestBranches(project, matchWith);
											else
												return null;
										case NAME_NUMBER:
											return SuggestionUtils.suggestPullRequests(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
										case PullRequest.NAME_MERGE_STRATEGY: {
											List<String> candidates = new ArrayList<>();
											for (MergeStrategy strategy : MergeStrategy.values())
												candidates.add(strategy.toString());
											return SuggestionUtils.suggest(candidates, matchWith);
										}
										case PullRequest.NAME_LABEL:
											return SuggestionUtils.suggestLabels(matchWith);
										case PullRequest.NAME_TITLE:
										case PullRequest.NAME_DESCRIPTION:
										case PullRequest.NAME_COMMENT_COUNT:
										case PullRequest.NAME_COMMENT:
										case PullRequest.NAME_THUMBS_UP_COUNT:
										case PullRequest.NAME_THUMBS_DOWN_COUNT:
										case PullRequest.NAME_SMILE_COUNT:
										case PullRequest.NAME_TADA_COUNT:
										case PullRequest.NAME_CONFUSED_COUNT:
										case PullRequest.NAME_HEART_COUNT:
										case PullRequest.NAME_ROCKET_COUNT:
										case PullRequest.NAME_EYES_COUNT:
											return null;
									}
								} catch (ExplicitException ignored) {
								}
							}
						}
						return new ArrayList<>();
					}

					@Override
					protected String getFencingDescription() {
						return _T("value should be quoted");
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
						return _T("enclose with ~ to query title/description/comment");
					}

				}.suggest(terminalExpect);
			}
		}
		return null;
	}
	
	@Override
	protected Optional<String> describe(ParseExpect parseExpect, String suggestedLiteral) {
		if (!withOrder && suggestedLiteral.equals(getRuleName(OrderBy))
				|| !withCurrentUserCriteria && (suggestedLiteral.equals(getRuleName(SubmittedByMe)) || suggestedLiteral.equals(getRuleName(WatchedByMe)) || suggestedLiteral.equals(getRuleName(IgnoredByMe)) || suggestedLiteral.equals(getRuleName(NeedMyAction)) || suggestedLiteral.equals(getRuleName(CommentedByMe)) || suggestedLiteral.equals(getRuleName(MentionedMe)) || suggestedLiteral.equals(getRuleName(ToBeReviewedByMe)) || suggestedLiteral.equals(getRuleName(RequestedForChangesByMe)) || suggestedLiteral.equals(getRuleName(ApprovedByMe)) || suggestedLiteral.equals(getRuleName(AssignedToMe)) || suggestedLiteral.equals(getRuleName(ToBeChangedByMe)) || suggestedLiteral.equals(getRuleName(ToBeMergedByMe)))) {
			return null;
		} else if (suggestedLiteral.equals(",")) {
			if (parseExpect.findExpectByLabel("orderOperator") != null)
				return Optional.of(_T("add another order"));
			else
				return Optional.of(_T("or match another value"));
		} else if (suggestedLiteral.equals("#")) {
			if (getProject() != null)
				return Optional.of(_T("find pull request by number"));
			else
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
						hints.add(_T("Use '**', '*', or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>"));
					} else if (fieldName.equals(PullRequest.NAME_TITLE) 
							|| fieldName.equals(PullRequest.NAME_DESCRIPTION)
							|| fieldName.equals(PullRequest.NAME_COMMENT)
							|| fieldName.equals(PullRequest.NAME_SOURCE_PROJECT) 
							|| fieldName.equals(PullRequest.NAME_SOURCE_BRANCH)) {
						hints.add(_T("Use '*' for wildcard match"));
						hints.add(_T("Use '\\' to escape quotes"));
					}
				}
			}
		} 
		return hints;
	}

	@Override
	protected boolean isFuzzySuggestion(InputCompletion suggestion) {
		return suggestion.getDescription() != null 
				&& suggestion.getDescription().startsWith(FUZZY_SUGGESTION_DESCRIPTION_PREFIX);
	}
	
}
