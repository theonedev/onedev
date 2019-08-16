package io.onedev.server.web.behavior;

import static io.onedev.server.util.IssueConstants.FIELD_COMMENT;
import static io.onedev.server.util.IssueConstants.FIELD_COMMENT_COUNT;
import static io.onedev.server.util.IssueConstants.FIELD_DESCRIPTION;
import static io.onedev.server.util.IssueConstants.FIELD_MILESTONE;
import static io.onedev.server.util.IssueConstants.FIELD_NUMBER;
import static io.onedev.server.util.IssueConstants.FIELD_STATE;
import static io.onedev.server.util.IssueConstants.FIELD_SUBMIT_DATE;
import static io.onedev.server.util.IssueConstants.FIELD_TITLE;
import static io.onedev.server.util.IssueConstants.FIELD_UPDATE_DATE;
import static io.onedev.server.util.IssueConstants.FIELD_VOTE_COUNT;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
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
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.setting.GlobalIssueSetting;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.search.entity.issue.IssueQueryParser;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.IssueConstants;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.inputspec.BuildChoiceInput;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.IssueChoiceInput;
import io.onedev.server.util.inputspec.PullRequestChoiceInput;
import io.onedev.server.util.inputspec.booleaninput.BooleanInput;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.dateinput.DateInput;
import io.onedev.server.util.inputspec.groupchoiceinput.GroupChoiceInput;
import io.onedev.server.util.inputspec.numberinput.NumberInput;
import io.onedev.server.util.inputspec.textinput.TextInput;
import io.onedev.server.util.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class IssueQueryBehavior extends ANTLRAssistBehavior {

	private final IModel<Project> projectModel;
	
	private static final String VALUE_OPEN = "\"";
	
	private static final String VALUE_CLOSE = "\"";

	private static final String ESCAPE_CHARS = "\"\\";
	
	private static final int MAX_ISSUE_TITLE_LEN = 75;
	
	public IssueQueryBehavior(IModel<Project> projectModel) {
		super(IssueQueryParser.class, "query", false);
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

	private List<InputSuggestion> escape(List<InputSuggestion> suggestions) {
		return suggestions.stream().map(it->it.escape(ESCAPE_CHARS)).collect(Collectors.toList());
	}
	
	@Override
	protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
			if (spec.getRuleName().equals("Quoted")) {
				return new FenceAware(codeAssist.getGrammar(), VALUE_OPEN, VALUE_CLOSE) {

					@Override
					protected List<InputSuggestion> match(String unfencedMatchWith) {
						String unfencedLowerCaseMatchWith = unfencedMatchWith.toLowerCase();
						List<InputSuggestion> suggestions = new ArrayList<>();
						Project project = getProject();

						if ("criteriaField".equals(spec.getLabel())) {
							List<String> candidates = new ArrayList<>(IssueConstants.QUERY_FIELDS);
							for (InputSpec field: issueSetting.getFieldSpecs())
								candidates.add(field.getName());
							suggestions.addAll(escape(SuggestionUtils.suggest(candidates, unfencedLowerCaseMatchWith)));
						} else if ("orderField".equals(spec.getLabel())) {
							List<String> candidates = new ArrayList<>(IssueConstants.ORDER_FIELDS.keySet());
							for (InputSpec field: issueSetting.getFieldSpecs()) {
								if (field instanceof NumberInput || field instanceof ChoiceInput || field instanceof DateInput) 
									candidates.add(field.getName());
							}
							suggestions.addAll(escape(SuggestionUtils.suggest(candidates, unfencedLowerCaseMatchWith)));
						} else if ("revisionValue".equals(spec.getLabel())) {
							String revisionType = terminalExpect.getState()
									.findMatchedElementsByLabel("revisionType", true).iterator().next().getMatchedText();
							switch (revisionType) {
							case "branch":
								if (getProject() != null)
									suggestions.addAll(escape(SuggestionUtils.suggestBranches(project, unfencedLowerCaseMatchWith)));
								break;
							case "tag":
								if (getProject() != null)
									suggestions.addAll(escape(SuggestionUtils.suggestTags(project, unfencedLowerCaseMatchWith)));
								break;
							case "build":
								if (getProject() != null)
									suggestions.addAll(escape(SuggestionUtils.suggestBuilds(project, unfencedLowerCaseMatchWith)));
								break;
							}
						} else if ("criteriaValue".equals(spec.getLabel())) {
							List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
							List<Element> operatorElements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
							Preconditions.checkState(operatorElements.size() == 1);
							String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
							int operator = IssueQuery.getOperator(operatorName);							
							if (fieldElements.isEmpty()) {
								if (operator == IssueQueryLexer.SubmittedBy)
									suggestions.addAll(escape(SuggestionUtils.suggestUsers(unfencedLowerCaseMatchWith)));
								else
									suggestions.addAll(escape(SuggestionUtils.suggestBuilds(project, unfencedLowerCaseMatchWith)));
							} else {
								String fieldName = IssueQuery.getValue(fieldElements.get(0).getMatchedText());
								
								try {
									IssueQuery.checkField(fieldName, operator);
									InputSpec fieldSpec = issueSetting.getFieldSpec(fieldName);
									if (fieldSpec instanceof DateInput || fieldName.equals(FIELD_SUBMIT_DATE) 
											|| fieldName.equals(FIELD_UPDATE_DATE)) {
										suggestions.addAll(SuggestionUtils.suggest(DateUtils.RELAX_DATE_EXAMPLES, unfencedLowerCaseMatchWith));
										CollectionUtils.addIgnoreNull(suggestions, suggestToFence(terminalExpect, unfencedMatchWith));
									} else if (fieldSpec instanceof UserChoiceInput) {
										suggestions.addAll(escape(SuggestionUtils.suggestUsers(unfencedLowerCaseMatchWith)));
									} else if (fieldSpec instanceof IssueChoiceInput) {
										List<Issue> issues = OneDev.getInstance(IssueManager.class).query(project, unfencedLowerCaseMatchWith, InputAssistBehavior.MAX_SUGGESTIONS);		
										for (Issue issue: issues) {
											InputSuggestion suggestion = new InputSuggestion("#" + issue.getNumber(), StringUtils.abbreviate(issue.getTitle(), MAX_ISSUE_TITLE_LEN), null);
											suggestions.add(suggestion);
										}
									} else if (fieldSpec instanceof BuildChoiceInput) {
										List<Build> builds = OneDev.getInstance(BuildManager.class).query(project, unfencedLowerCaseMatchWith, InputAssistBehavior.MAX_SUGGESTIONS);		
										for (Build build: builds) {
											InputSuggestion suggestion = new InputSuggestion("#" + build.getNumber(), null, null);
											suggestions.add(suggestion);
										}
									} else if (fieldSpec instanceof PullRequestChoiceInput) {
										List<PullRequest> requests = OneDev.getInstance(PullRequestManager.class).query(project, unfencedLowerCaseMatchWith, InputAssistBehavior.MAX_SUGGESTIONS);		
										for (PullRequest request: requests) {
											InputSuggestion suggestion = new InputSuggestion("#" + request.getNumber(), StringUtils.abbreviate(request.getTitle(), MAX_ISSUE_TITLE_LEN), null);
											suggestions.add(suggestion);
										}
									} else if (fieldSpec instanceof BooleanInput) {
										suggestions.addAll(SuggestionUtils.suggest(Lists.newArrayList("true", "false"), unfencedLowerCaseMatchWith));
									} else if (fieldSpec instanceof GroupChoiceInput) {
										List<String> candidates = OneDev.getInstance(GroupManager.class).query().stream().map(it->it.getName()).collect(Collectors.toList());
										suggestions.addAll(escape(SuggestionUtils.suggest(candidates, unfencedLowerCaseMatchWith)));
									} else if (fieldName.equals(FIELD_STATE)) {
										List<String> candidates = issueSetting.getStateSpecs().stream().map(it->it.getName()).collect(Collectors.toList());
										suggestions.addAll(escape(SuggestionUtils.suggest(candidates, unfencedLowerCaseMatchWith)));
									} else if (fieldSpec instanceof ChoiceInput) {
										OneContext.push(newOneContext());
										try {
											List<String> candidates = new ArrayList<>(((ChoiceInput)fieldSpec).getChoiceProvider().getChoices(true).keySet());
											suggestions.addAll(escape(SuggestionUtils.suggest(candidates, unfencedLowerCaseMatchWith)));
										} finally {
											OneContext.pop();
										}			
									} else if (fieldName.equals(FIELD_MILESTONE)) {
										List<String> candidates = project.getMilestones().stream().map(it->it.getName()).collect(Collectors.toList());
										suggestions.addAll(escape(SuggestionUtils.suggest(candidates, unfencedLowerCaseMatchWith)));
									} else if (fieldName.equals(FIELD_TITLE) || fieldName.equals(FIELD_DESCRIPTION) 
											|| fieldName.equals(FIELD_COMMENT) || fieldName.equals(FIELD_VOTE_COUNT) 
											|| fieldName.equals(FIELD_COMMENT_COUNT) || fieldName.equals(FIELD_NUMBER) 
											|| fieldSpec instanceof NumberInput || fieldSpec instanceof TextInput) {
										return null;
									}
								} catch (OneException ex) {
								}
							}
						}
						return suggestions;
					}
					
					private OneContext newOneContext() {
						return new OneContext(IssueQueryBehavior.this.getComponent()) {

							@Override
							public Project getProject() {
								return IssueQueryBehavior.this.getProject();
							}

							@Override
							public EditContext getEditContext(int level) {
								return new EditContext() {

									@Override
									public Object getInputValue(String name) {
										return null;
									}
									
								};
							}

							@Override
							public InputContext getInputContext() {
								throw new UnsupportedOperationException();
							}

						};
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
		if (suggestedLiteral.equals("mine")) {
			if (SecurityUtils.getUser() != null)
				return Optional.of("issues relevant to me");
			else
				return null;
		} 

		if ((suggestedLiteral.equals("is me") || suggestedLiteral.equals("is not me")) && SecurityUtils.getUser() == null)
			return null;	
		
		if (suggestedLiteral.equals("open"))
			return Optional.of("issues with state in open category");
		else if (suggestedLiteral.equals("closed"))
			return Optional.of("issues with state in closed category");
		
		parseExpect = parseExpect.findExpectByLabel("operator");
		if (parseExpect != null) {
			List<Element> fieldElements = parseExpect.getState().findMatchedElementsByLabel("criteriaField", false);
			if (!fieldElements.isEmpty()) {
				String fieldName = IssueQuery.getValue(fieldElements.iterator().next().getMatchedText());
				try {
					IssueQuery.checkField(fieldName, IssueQuery.getOperator(suggestedLiteral));
				} catch (OneException e) {
					return null;
				}
			}
		}
		return super.describe(parseExpect, suggestedLiteral);
	}

}
