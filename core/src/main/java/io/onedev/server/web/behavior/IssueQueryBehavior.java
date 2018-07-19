package io.onedev.server.web.behavior;

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

import io.onedev.codeassist.FenceAware;
import io.onedev.codeassist.InputSuggestion;
import io.onedev.codeassist.ParentedElement;
import io.onedev.codeassist.grammar.ElementSpec;
import io.onedev.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.codeassist.grammar.LiteralElementSpec;
import io.onedev.codeassist.grammar.RuleRefElementSpec;
import io.onedev.codeassist.parser.Element;
import io.onedev.server.OneDev;
import io.onedev.server.exception.OneException;
import io.onedev.server.manager.GroupManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.model.support.issue.query.IssueQueryParser;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Constants;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.booleaninput.BooleanInput;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.dateinput.DateInput;
import io.onedev.server.util.inputspec.groupchoiceinput.GroupChoiceInput;
import io.onedev.server.util.inputspec.issuechoiceinput.IssueChoiceInput;
import io.onedev.server.util.inputspec.numberinput.NumberInput;
import io.onedev.server.util.inputspec.textinput.TextInput;
import io.onedev.server.util.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.utils.Range;

@SuppressWarnings("serial")
public class IssueQueryBehavior extends ANTLRAssistBehavior {

	private final IModel<Project> projectModel;
	
	private static final String VALUE_OPEN = "\"";
	
	private static final String VALUE_CLOSE = "\"";
	
	private static final int MAX_ISSUE_TITLE_LEN = 75;
	
	public IssueQueryBehavior(IModel<Project> projectModel) {
		super(IssueQueryParser.class, "query");
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
	protected int getEndOfMatch(ElementSpec spec, String content) {
		if (content.startsWith(VALUE_OPEN+VALUE_CLOSE))
			return 2;
		else
			return super.getEndOfMatch(spec, content);
	}

	@Override
	protected List<InputSuggestion> suggest(ParentedElement expectedElement, String matchWith) {
		if (expectedElement.getSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) expectedElement.getSpec();
			if (spec.getRuleName().equals("Quoted")) {
				return new FenceAware(codeAssist.getGrammar(), VALUE_OPEN, VALUE_CLOSE) {

					private List<InputSuggestion> getUserSuggestions(String matchWith) {
						List<InputSuggestion> suggestions = new ArrayList<>();
						for (User user: OneDev.getInstance(UserManager.class).findAll()) {
							Range match = Range.match(user.getName(), matchWith, true, false, true);
							if (match != null) {
								String description;
								if (!user.getDisplayName().equals(user.getName()))
									description = user.getDisplayName();
								else
									description = null;
								suggestions.add(new InputSuggestion(user.getName(), description, match).escape("\"\\"));
							}
						}
						return suggestions;
					}
					
					@Override
					protected List<InputSuggestion> match(String unfencedMatchWith) {
						String unfencedLowerCaseMatchWith = unfencedMatchWith.toLowerCase();
						List<InputSuggestion> suggestions = new ArrayList<>();
						Project project = getProject();

						if ("criteriaField".equals(spec.getLabel())) {
							List<String> candidates = new ArrayList<>(Issue.FIELD_PATHS.keySet());
							candidates.remove(Issue.FIELD_SUBMITTER);
							for (InputSpec field: project.getIssueWorkflow().getFieldSpecs())
								candidates.add(field.getName());
							suggestions.addAll(getSuggestions(candidates, unfencedLowerCaseMatchWith, "\"\\"));
						} else if ("orderField".equals(spec.getLabel())) {
							List<String> candidates = Lists.newArrayList(Issue.FIELD_VOTE_COUNT, Issue.FIELD_COMMENT_COUNT, Issue.FIELD_NUMBER, 
									Issue.FIELD_SUBMIT_DATE, Issue.FIELD_UPDATE_DATE);
							for (InputSpec field: project.getIssueWorkflow().getFieldSpecs()) {
								if (field instanceof NumberInput || field instanceof ChoiceInput || field instanceof DateInput) 
									candidates.add(field.getName());
							}
							suggestions.addAll(getSuggestions(candidates, unfencedLowerCaseMatchWith, "\"\\"));
						} else if ("criteriaValue".equals(spec.getLabel())) {
							List<Element> fieldElements = expectedElement.getParent().findChildrenByLabel("criteriaField", true);
							if (fieldElements.isEmpty()) {
								suggestions.addAll(getUserSuggestions(unfencedLowerCaseMatchWith));
							} else {
								String fieldName = IssueQuery.getValue(fieldElements.get(0).getMatchedText());
								List<Element> operatorElements = expectedElement.getParent().findChildrenByLabel("operator", true);
								Preconditions.checkState(operatorElements.size() == 1);
								String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
								
								try {
									IssueQuery.checkField(project, fieldName, IssueQuery.getOperator(operatorName));
									InputSpec fieldSpec = project.getIssueWorkflow().getFieldSpec(fieldName);
									if (fieldSpec instanceof DateInput || fieldName.equals(Issue.FIELD_SUBMIT_DATE) 
											|| fieldName.equals(Issue.FIELD_UPDATE_DATE)) {
										List<String> candidates = new ArrayList<>(DateUtils.RELAX_DATE_EXAMPLES);
										candidates.add(Constants.DATETIME_FORMATTER.print(System.currentTimeMillis()));
										candidates.add(Constants.DATE_FORMATTER.print(System.currentTimeMillis()));
										suggestions.addAll(getSuggestions(candidates, unfencedLowerCaseMatchWith, null));
										CollectionUtils.addIgnoreNull(suggestions, suggestToFence(unfencedMatchWith));
									} else if (fieldSpec instanceof UserChoiceInput) {
										suggestions.addAll(getUserSuggestions(unfencedLowerCaseMatchWith));
									} else if (fieldSpec instanceof IssueChoiceInput) {
										List<Issue> issues = OneDev.getInstance(IssueManager.class).query(project, unfencedLowerCaseMatchWith, InputAssistBehavior.MAX_SUGGESTIONS);		
										for (Issue issue: issues) {
											InputSuggestion suggestion = new InputSuggestion("#" + issue.getNumber(), StringUtils.abbreviate(issue.getTitle(), MAX_ISSUE_TITLE_LEN), null);
											suggestions.add(suggestion);
										}
									} else if (fieldSpec instanceof BooleanInput) {
										suggestions.addAll(getSuggestions(Lists.newArrayList("true", "false"), unfencedLowerCaseMatchWith, null));
									} else if (fieldSpec instanceof GroupChoiceInput) {
										List<String> candidates = OneDev.getInstance(GroupManager.class).findAll().stream().map(it->it.getName()).collect(Collectors.toList());
										suggestions.addAll(getSuggestions(candidates, unfencedLowerCaseMatchWith, "\"\\"));
									} else if (fieldName.equals(Issue.FIELD_STATE)) {
										List<String> candidates = project.getIssueWorkflow().getStateSpecs().stream().map(it->it.getName()).collect(Collectors.toList());
										suggestions.addAll(getSuggestions(candidates, unfencedLowerCaseMatchWith, "\"\\"));
									} else if (fieldSpec instanceof ChoiceInput) {
										OneContext.push(newOneContext());
										try {
											List<String> candidates = new ArrayList<>(((ChoiceInput)fieldSpec).getChoiceProvider().getChoices(true).keySet());
											suggestions.addAll(getSuggestions(candidates, unfencedLowerCaseMatchWith, "\"\\"));
										} finally {
											OneContext.pop();
										}			
									} else if (fieldName.equals(Issue.FIELD_MILESTONE)) {
										List<String> candidates = project.getMilestones().stream().map(it->it.getName()).collect(Collectors.toList());
										suggestions.addAll(getSuggestions(candidates, unfencedLowerCaseMatchWith, "\"\\"));
									} else if (fieldName.equals(Issue.FIELD_TITLE) || fieldName.equals(Issue.FIELD_DESCRIPTION) 
											|| fieldName.equals(Issue.FIELD_VOTE_COUNT) || fieldName.equals(Issue.FIELD_COMMENT_COUNT) 
											|| fieldName.equals(Issue.FIELD_NUMBER) || fieldSpec instanceof NumberInput 
											|| fieldSpec instanceof TextInput) {
										return null;
									}
								} catch (OneException ex) {
								}
							}
						}
						return suggestions;
					}
					
					private OneContext newOneContext() {
						return new OneContext() {

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
					
				}.suggest(expectedElement.getSpec(), matchWith);
			}
		} 
		return null;
	}
	
	@Override
	protected List<String> getHints(ParentedElement expectedElement, String matchWith) {
		List<String> hints = super.getHints(expectedElement, matchWith);
		if (expectedElement.getSpec() instanceof LiteralElementSpec 
				|| expectedElement.getSpec() instanceof LexerRuleRefElementSpec) { 
			hints.add("Input text without space to match options");
		}
		return hints;
	}

	@Override
	protected Optional<String> describe(ParentedElement expectedElement, String suggestedLiteral) {
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
		
		if (expectedElement.getParent() != null 
				&& expectedElement.getParent().getParent() != null
				&& expectedElement.getParent().getParent().getSpec() instanceof RuleRefElementSpec) {
			RuleRefElementSpec ruleRefElementSpec = (RuleRefElementSpec) expectedElement.getParent().getParent().getSpec();
			if (ruleRefElementSpec.getRuleName().equals("criteria")) {
				List<Element> fieldElements = expectedElement.getParent().getParent()
						.findChildrenByLabel("criteriaField", false);
				if (!fieldElements.isEmpty()) {
					String fieldName = IssueQuery.getValue(fieldElements.iterator().next().getMatchedText());
					try {
						IssueQuery.checkField(getProject(), fieldName, IssueQuery.getOperator(suggestedLiteral));
					} catch (OneException e) {
						return null;
					}
				}
			}
		} 
		return super.describe(expectedElement, suggestedLiteral);
	}

}
