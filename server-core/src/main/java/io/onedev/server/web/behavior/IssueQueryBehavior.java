package io.onedev.server.web.behavior;

import static io.onedev.server.model.Issue.NAME_COMMENT;
import static io.onedev.server.model.Issue.NAME_COMMENT_COUNT;
import static io.onedev.server.model.Issue.NAME_DESCRIPTION;
import static io.onedev.server.model.Issue.NAME_NUMBER;
import static io.onedev.server.model.Issue.NAME_PROJECT;
import static io.onedev.server.model.Issue.NAME_STATE;
import static io.onedev.server.model.Issue.NAME_SUBMIT_DATE;
import static io.onedev.server.model.Issue.NAME_TITLE;
import static io.onedev.server.model.Issue.NAME_UPDATE_DATE;
import static io.onedev.server.model.Issue.NAME_VOTE_COUNT;
import static io.onedev.server.search.entity.EntityQuery.getValue;
import static io.onedev.server.search.entity.issue.IssueQuery.checkField;
import static io.onedev.server.search.entity.issue.IssueQuery.getOperator;
import static io.onedev.server.search.entity.issue.IssueQuery.getRuleName;
import static io.onedev.server.search.entity.issue.IssueQueryLexer.CurrentIssue;
import static io.onedev.server.search.entity.issue.IssueQueryLexer.FixedInBuild;
import static io.onedev.server.search.entity.issue.IssueQueryLexer.FixedInCurrentBuild;
import static io.onedev.server.search.entity.issue.IssueQueryLexer.FixedInCurrentCommit;
import static io.onedev.server.search.entity.issue.IssueQueryLexer.FixedInCurrentPullRequest;
import static io.onedev.server.search.entity.issue.IssueQueryLexer.FixedInPullRequest;
import static io.onedev.server.search.entity.issue.IssueQueryLexer.HasAny;
import static io.onedev.server.search.entity.issue.IssueQueryLexer.OrderBy;
import static io.onedev.server.search.entity.issue.IssueQueryLexer.SubmittedBy;
import static io.onedev.server.search.entity.issue.IssueQueryLexer.SubmittedByMe;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.onedev.commons.codeassist.FenceAware;
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
import io.onedev.server.model.support.issue.field.spec.BooleanField;
import io.onedev.server.model.support.issue.field.spec.BuildChoiceField;
import io.onedev.server.model.support.issue.field.spec.ChoiceField;
import io.onedev.server.model.support.issue.field.spec.CommitField;
import io.onedev.server.model.support.issue.field.spec.DateField;
import io.onedev.server.model.support.issue.field.spec.DateTimeField;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.GroupChoiceField;
import io.onedev.server.model.support.issue.field.spec.IntegerField;
import io.onedev.server.model.support.issue.field.spec.IssueChoiceField;
import io.onedev.server.model.support.issue.field.spec.MilestoneChoiceField;
import io.onedev.server.model.support.issue.field.spec.PullRequestChoiceField;
import io.onedev.server.model.support.issue.field.spec.TextField;
import io.onedev.server.model.support.issue.field.spec.UserChoiceField;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.search.entity.issue.IssueQueryParser;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class IssueQueryBehavior extends ANTLRAssistBehavior {

	private final IModel<Project> projectModel;
	
	private final IssueQueryParseOption option;
	
	public IssueQueryBehavior(IModel<Project> projectModel, IssueQueryParseOption option) {
		super(IssueQueryParser.class, "query", false);
		this.projectModel = projectModel;
		this.option = option;
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
			if (spec.getRuleName().equals("Quoted")) {
				return new FenceAware(codeAssist.getGrammar(), '"', '"') {

					@Override
					protected List<InputSuggestion> match(String matchWith) {
						Project project = getProject();
						if ("criteriaField".equals(spec.getLabel())) {
							List<String> candidates = new ArrayList<>(Issue.QUERY_FIELDS);
							for (FieldSpec field: issueSetting.getFieldSpecs())
								candidates.add(field.getName());
							return SuggestionUtils.suggest(candidates, matchWith);
						} else if ("orderField".equals(spec.getLabel())) {
							List<String> candidates = new ArrayList<>(Issue.ORDER_FIELDS.keySet());
							if (getProject() != null)
								candidates.remove(Issue.NAME_PROJECT);
							for (FieldSpec field: issueSetting.getFieldSpecs()) {
								if (field instanceof IntegerField || field instanceof ChoiceField 
										|| field instanceof DateField || field instanceof DateTimeField 
										|| field instanceof MilestoneChoiceField) { 
									candidates.add(field.getName());
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
								if (operator == SubmittedBy)
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
												|| fieldName.equals(NAME_SUBMIT_DATE) || fieldName.equals(NAME_UPDATE_DATE)) {
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
											return SuggestionUtils.suggest(Lists.newArrayList("true", "false"), matchWith);
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
			}
		} 
		return null;
	}
	
	@Override
	protected Optional<String> describe(ParseExpect parseExpect, String suggestedLiteral) {
		if (!option.withOrder() && suggestedLiteral.equals(getRuleName(OrderBy))
				|| !option.withCurrentUserCriteria() && suggestedLiteral.equals(getRuleName(SubmittedByMe))
				|| !option.withCurrentBuildCriteria() && suggestedLiteral.equals(getRuleName(FixedInCurrentBuild))
				|| !option.withCurrentPullRequestCriteria() && suggestedLiteral.equals(getRuleName(FixedInCurrentPullRequest))
				|| !option.withCurrentCommitCriteria() && suggestedLiteral.equals(getRuleName(FixedInCurrentCommit))
				|| !option.withCurrentIssueCriteria() && suggestedLiteral.equals(getRuleName(CurrentIssue))) {
			return null;
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
	
}
