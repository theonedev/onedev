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
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.field.spec.*;
import io.onedev.server.model.support.issue.field.spec.choicefield.ChoiceField;
import io.onedev.server.model.support.issue.field.spec.userchoicefield.UserChoiceField;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.search.entity.issue.IssueQueryParser;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.WicketUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static io.onedev.server.model.Issue.*;
import static io.onedev.server.search.entity.EntityQuery.getValue;
import static io.onedev.server.search.entity.issue.IssueQuery.*;
import static io.onedev.server.search.entity.issue.IssueQueryLexer.*;

@SuppressWarnings("serial")
public class IssueQueryBehavior extends ANTLRAssistBehavior {

	private static final String FUZZY_SUGGESTION_DESCRIPTION_PREFIX = "surround with ~";
	
	private final IModel<Project> projectModel;
	
	private final IssueQueryParseOption option;
	
	public IssueQueryBehavior(IModel<Project> projectModel, IssueQueryParseOption option, boolean hideIfBlank) {
		super(IssueQueryParser.class, "query", false, hideIfBlank);
		this.projectModel = projectModel;
		this.option = option;
	}

	public IssueQueryBehavior(IModel<Project> projectModel, IssueQueryParseOption option) {
		this(projectModel, option, false);
	}
	
	@Override
	public void detach(Component component) {
		super.detach(component);
		projectModel.detach();
	}
	
	@Nullable
	private Project getProject() {
		return projectModel.getObject();
	}

	private LinkSpecManager getLinkSpecManager() {
		return OneDev.getInstance(LinkSpecManager.class);
	}
	
	@Override
	protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
			if (spec.getRuleName().equals("Number")) {
				return SuggestionUtils.suggestNumber(
						terminalExpect.getUnmatchedText(), 
						"find issue with this number");
			} else if (spec.getRuleName().equals("Quoted")) {
				return new FenceAware(codeAssist.getGrammar(), '"', '"') {

					private Map<String, String> getFieldCandidates(Collection<String> fields) {
						Map<String, String> candidates = new LinkedHashMap<>();
						for (var field: fields) {
							if ((field.equals(NAME_ESTIMATED_TIME) || field.equals(NAME_SPENT_TIME) || field.equals(NAME_PROGRESS))
									&& issueSetting.getTimeTrackingSetting().getAggregationLink() != null) {
								if (field.equals(NAME_ESTIMATED_TIME))
									candidates.put(field, "Total estimated time");
								else if (field.equals(NAME_SPENT_TIME))
									candidates.put(field, "Total spent time");
								else
									candidates.put(field, "Total spent time / total estimated time");
							} else if (field.equals(NAME_PROGRESS)) {
								candidates.put(field, "Spent time / estimated time");
							} else {
								candidates.put(field, null);
							}
						}
						return candidates;
					}
					
					@Override
					protected List<InputSuggestion> match(String matchWith) {
						Project project = getProject();
						if ("criteriaField".equals(spec.getLabel())) {
							Map<String, String> candidates = getFieldCandidates(QUERY_FIELDS);
							if (!option.withProjectCriteria())
								candidates.remove(NAME_PROJECT);
							if (!option.withStateCriteria())
								candidates.remove(NAME_STATE);
							for (FieldSpec field: issueSetting.getFieldSpecs())
								candidates.put(field.getName(), null);
							if (project != null && !project.isTimeTracking() || !WicketUtils.isSubscriptionActive()) {
								candidates.remove(NAME_ESTIMATED_TIME);
								candidates.remove(NAME_SPENT_TIME);
								candidates.remove(NAME_PROGRESS);
							}
							return SuggestionUtils.suggest(candidates, matchWith);
						} else if ("orderField".equals(spec.getLabel())) {
							Map<String, String> candidates = getFieldCandidates(ORDER_FIELDS.keySet());
							if (getProject() != null)
								candidates.remove(Issue.NAME_PROJECT);
							if (project != null && !project.isTimeTracking() || !WicketUtils.isSubscriptionActive()) {
								candidates.remove(NAME_ESTIMATED_TIME);
								candidates.remove(NAME_SPENT_TIME);
								candidates.remove(NAME_PROGRESS);
							}
							for (FieldSpec field: issueSetting.getFieldSpecs()) {
								if (field instanceof IntegerField || field instanceof ChoiceField 
										|| field instanceof DateField || field instanceof DateTimeField 
										|| field instanceof MilestoneChoiceField) { 
									candidates.put(field.getName(), null);
								}
							}
							return SuggestionUtils.suggest(candidates, matchWith);
						} else if ("revisionValue".equals(spec.getLabel())) {
							String revisionType = terminalExpect.getState()
									.findMatchedElementsByLabel("revisionType", true).iterator().next().getMatchedText();
							switch (revisionType) {
							case "branch":
								return SuggestionUtils.suggestBranches(project, matchWith);
							case "tag":
								return SuggestionUtils.suggestTags(project, matchWith);
							case "commit":
								return SuggestionUtils.suggestCommits(project, matchWith);
							case "build":
								return SuggestionUtils.suggestBuilds(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
							}
						} else if ("criteriaValue".equals(spec.getLabel())) {
							List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
							List<Element> operatorElements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
							Preconditions.checkState(operatorElements.size() == 1);
							String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
							int operator = getOperator(operatorName);							
							if (fieldElements.isEmpty()) {
								if (operator == Mentioned || operator == SubmittedBy || operator == CommentedBy || operator == WatchedBy)
									return SuggestionUtils.suggestUsers(matchWith);
								else if (operator == FixedInBuild)
									return SuggestionUtils.suggestBuilds(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
								else if (operator == FixedInPullRequest) 
									return SuggestionUtils.suggestPullRequests(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
								else if (operator == HasAny) 
									return SuggestionUtils.suggestLinkSpecs(matchWith);
								else
									return SuggestionUtils.suggestCommits(project, matchWith);
							} else {
								String fieldName = getValue(fieldElements.get(0).getMatchedText());
								
								try {
									checkField(fieldName, operator, option);
									LinkSpec linkSpec = getLinkSpecManager().find(fieldName);
									if (linkSpec != null) {
										return SuggestionUtils.suggestIssues(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
									} else {
										FieldSpec fieldSpec = issueSetting.getFieldSpec(fieldName);
										if (fieldSpec instanceof DateField || fieldSpec instanceof DateTimeField 
												|| fieldName.equals(NAME_SUBMIT_DATE) || fieldName.equals(NAME_LAST_ACTIVITY_DATE)) {
											List<InputSuggestion> suggestions = SuggestionUtils.suggest(DateUtils.RELAX_DATE_EXAMPLES, matchWith);
											return !suggestions.isEmpty()? suggestions: null;
										} else if (fieldSpec instanceof UserChoiceField) {
											return SuggestionUtils.suggestUsers(matchWith);
										} else if (fieldSpec instanceof IssueChoiceField) {
											return SuggestionUtils.suggestIssues(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
										} else if (fieldSpec instanceof BuildChoiceField) {
											return SuggestionUtils.suggestBuilds(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
										} else if (fieldSpec instanceof CommitField) {
											return SuggestionUtils.suggestCommits(project, matchWith);
										} else if (fieldSpec instanceof PullRequestChoiceField) {
											return SuggestionUtils.suggestPullRequests(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
										} else if (fieldSpec instanceof BooleanField) {
											return SuggestionUtils.suggest(newArrayList("true", "false"), matchWith);
										} else if (fieldSpec instanceof GroupChoiceField) {
											List<String> candidates = OneDev.getInstance(GroupManager.class).query().stream().map(it->it.getName()).collect(Collectors.toList());
											return SuggestionUtils.suggest(candidates, matchWith);
										} else if (fieldName.equals(NAME_PROJECT)) {
											if (!matchWith.contains("*"))
												return SuggestionUtils.suggestProjectPaths(matchWith);
											else
												return null;
										} else if (fieldName.equals(NAME_STATE)) {
											List<String> candidates = issueSetting.getStateSpecs()
													.stream()
													.map(it->it.getName())
													.collect(Collectors.toList());
											return SuggestionUtils.suggest(candidates, matchWith);
										} else if (fieldSpec instanceof ChoiceField) {
											ComponentContext.push(new ComponentContext(getComponent()));
											try {
												List<String> candidates = new ArrayList<>(((ChoiceField)fieldSpec)
														.getChoiceProvider().getChoices(true).keySet());
												return SuggestionUtils.suggest(candidates, matchWith);
											} finally {
												ComponentContext.pop();
											}			
										} else if (fieldName.equals(NAME_NUMBER)) {
											return SuggestionUtils.suggestIssues(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
										} else if (fieldName.equals(IssueSchedule.NAME_MILESTONE)) {
											if (project != null && !matchWith.contains("*"))
												return SuggestionUtils.suggestMilestones(project, matchWith);
											else
												return null;
										} else if (fieldName.equals(NAME_ESTIMATED_TIME) || fieldName.equals(NAME_SPENT_TIME)) {
											var suggestions = new ArrayList<InputSuggestion>();
											if ("1w 1d 1h 1m".contains(matchWith.toLowerCase()))
												suggestions.add(new InputSuggestion("1w 1d 1h 1m", "specify working period, modify as necessary", null));
											return !suggestions.isEmpty()? suggestions: null;
										} else if (fieldName.equals(NAME_PROGRESS)) {
											var suggestions = new ArrayList<InputSuggestion>();
											if ("0.5".contains(matchWith.toLowerCase()))
												suggestions.add(new InputSuggestion("0.5", "specify decimal number, modify as necessary", null));
											return !suggestions.isEmpty() ? suggestions : null;
										} else if (fieldName.equals(NAME_TITLE) || fieldName.equals(NAME_DESCRIPTION) 
												|| fieldName.equals(NAME_COMMENT) || fieldName.equals(NAME_VOTE_COUNT) 
												|| fieldName.equals(NAME_COMMENT_COUNT) || fieldSpec instanceof IntegerField 
												|| fieldSpec instanceof TextField) {
											return null;
										}
									}
								} catch (ExplicitException ex) {
								}
							}
						} else if ("linkSpec".equals(spec.getLabel())) {
							return SuggestionUtils.suggestLinkSpecs(matchWith);
						}
						return new ArrayList<>();
					}
					
					@Override
					protected String getFencingDescription() {
						return "value should be quoted";
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
						return FUZZY_SUGGESTION_DESCRIPTION_PREFIX + " to query title/description/comment";
					}

				}.suggest(terminalExpect);
			}
		} 
		return null;
	}
	
	@Override
	protected Optional<String> describe(ParseExpect parseExpect, String suggestedLiteral) {
		if (!option.withOrder() && suggestedLiteral.equals(getRuleName(OrderBy))
				|| !option.withCurrentUserCriteria() && (suggestedLiteral.equals(getRuleName(SubmittedByMe)) || suggestedLiteral.equals(getRuleName(CommentedByMe)) || suggestedLiteral.equals(getRuleName(MentionedMe)) || suggestedLiteral.equals(getRuleName(WatchedByMe)))
				|| !option.withCurrentBuildCriteria() && suggestedLiteral.equals(getRuleName(FixedInCurrentBuild))
				|| !option.withCurrentPullRequestCriteria() && suggestedLiteral.equals(getRuleName(FixedInCurrentPullRequest))
				|| !option.withCurrentCommitCriteria() && suggestedLiteral.equals(getRuleName(FixedInCurrentCommit))
				|| !option.withCurrentIssueCriteria() && suggestedLiteral.equals(getRuleName(CurrentIssue))) {
			return null;
		} else if (suggestedLiteral.equals("#")) {
			return Optional.of("Find issue by number");
		}
		parseExpect = parseExpect.findExpectByLabel("operator");
		if (parseExpect != null) {
			List<Element> fieldElements = parseExpect.getState().findMatchedElementsByLabel("criteriaField", false);
			if (!fieldElements.isEmpty()) {
				String fieldName = getValue(fieldElements.iterator().next().getMatchedText());
				try {
					checkField(fieldName, getOperator(suggestedLiteral), option);
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
			if ("criteriaValue".equals(spec.getLabel())) {
				List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
				if (!fieldElements.isEmpty()) {
					String fieldName = ProjectQuery.getValue(fieldElements.get(0).getMatchedText());
					if (fieldName.equals(Issue.NAME_PROJECT)) {
						hints.add("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>");
					} else if (fieldName.equals(Issue.NAME_TITLE) 
							|| fieldName.equals(Issue.NAME_DESCRIPTION)
							|| fieldName.equals(Issue.NAME_COMMENT)
							|| fieldName.equals(IssueSchedule.NAME_MILESTONE)) {
						hints.add("Use '*' for wildcard match");
						hints.add("Use '\\' to escape quotes");
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
