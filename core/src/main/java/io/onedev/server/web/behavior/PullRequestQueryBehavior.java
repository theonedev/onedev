package io.onedev.server.web.behavior;

import java.util.ArrayList;
import java.util.List;

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
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.CloseInfo;
import io.onedev.server.model.support.pullrequest.query.PullRequestQuery;
import io.onedev.server.model.support.pullrequest.query.PullRequestQueryLexer;
import io.onedev.server.model.support.pullrequest.query.PullRequestQueryParser;
import io.onedev.server.util.Constants;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.utils.Range;

@SuppressWarnings("serial")
public class PullRequestQueryBehavior extends ANTLRAssistBehavior {

	private final IModel<Project> projectModel;
	
	private static final String VALUE_OPEN = "\"";
	
	private static final String VALUE_CLOSE = "\"";
	
	public PullRequestQueryBehavior(IModel<Project> projectModel) {
		super(PullRequestQueryParser.class, "query");
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
							List<String> candidates = new ArrayList<>(PullRequest.FIELD_PATHS.keySet());
							candidates.remove(PullRequest.FIELD_SUBMITTER);
							suggestions.addAll(getSuggestions(candidates, unfencedLowerCaseMatchWith, "\"\\"));
						} else if ("orderField".equals(spec.getLabel())) {
							List<String> candidates = Lists.newArrayList(PullRequest.FIELD_NUMBER, 
									PullRequest.FIELD_SUBMIT_DATE, PullRequest.FIELD_UPDATE_DATE, PullRequest.FIELD_CLOSE_DATE);
							suggestions.addAll(getSuggestions(candidates, unfencedLowerCaseMatchWith, "\"\\"));
						} else if ("criteriaValue".equals(spec.getLabel())) {
							List<Element> fieldElements = expectedElement.getParent().findChildrenByLabel("criteriaField", true);
							List<Element> operatorElements = expectedElement.getParent().findChildrenByLabel("operator", true);
							Preconditions.checkState(operatorElements.size() == 1);
							String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
							int operator = PullRequestQuery.getOperator(operatorName);							
							if (fieldElements.isEmpty()) {
								if (operator != PullRequestQueryLexer.ContainsCommit)
									suggestions.addAll(getUserSuggestions(unfencedLowerCaseMatchWith));
								else
									return null;
							} else {
								String fieldName = PullRequestQuery.getValue(fieldElements.get(0).getMatchedText());
								
								try {
									PullRequestQuery.checkField(project, fieldName, operator);
									if (fieldName.equals(PullRequest.FIELD_SUBMIT_DATE) || fieldName.equals(PullRequest.FIELD_UPDATE_DATE)
											|| fieldName.equals(PullRequest.FIELD_CLOSE_DATE)) {
										List<String> candidates = new ArrayList<>(DateUtils.RELAX_DATE_EXAMPLES);
										candidates.add(Constants.DATETIME_FORMATTER.print(System.currentTimeMillis()));
										candidates.add(Constants.DATE_FORMATTER.print(System.currentTimeMillis()));
										suggestions.addAll(getSuggestions(candidates, unfencedLowerCaseMatchWith, null));
										CollectionUtils.addIgnoreNull(suggestions, suggestToFence(unfencedMatchWith));
									} else if (fieldName.equals(PullRequest.FIELD_STATE)) {
										List<String> candidates = Lists.newArrayList(PullRequest.STATE_OPEN);
										for (CloseInfo.Status status: CloseInfo.Status.values())
											candidates.add(status.toString());
										suggestions.addAll(getSuggestions(candidates, unfencedLowerCaseMatchWith, "\"\\"));
									} else if (fieldName.equals(PullRequest.FIELD_SOURCE_PROJECT)) {
										List<String> candidates = new ArrayList<>();
										for (Project each: OneDev.getInstance(ProjectManager.class).findAll())
											candidates.add(each.getName());
										suggestions.addAll(getSuggestions(candidates, unfencedLowerCaseMatchWith, "\"\\"));
									} else if (fieldName.equals(PullRequest.FIELD_TARGET_BRANCH) || fieldName.equals(PullRequest.FIELD_SOURCE_BRANCH)) {
										List<String> candidates = new ArrayList<>();
										for (RefInfo refInfo: project.getBranches()) 
											candidates.add(GitUtils.ref2branch(refInfo.getRef().getName()));
										suggestions.addAll(getSuggestions(candidates, unfencedLowerCaseMatchWith, "\"\\"));
									} else if (fieldName.equals(PullRequest.FIELD_TITLE) || fieldName.equals(PullRequest.FIELD_DESCRIPTION) 
											|| fieldName.equals(PullRequest.FIELD_NUMBER)) {
										return null;
									}
								} catch (OneException ex) {
								}
							}
						}
						return suggestions;
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
		if (expectedElement.getParent() != null 
				&& expectedElement.getParent().getParent() != null
				&& expectedElement.getParent().getParent().getSpec() instanceof RuleRefElementSpec) {
			RuleRefElementSpec ruleRefElementSpec = (RuleRefElementSpec) expectedElement.getParent().getParent().getSpec();
			if (ruleRefElementSpec.getRuleName().equals("criteria")) {
				List<Element> fieldElements = expectedElement.getParent().getParent()
						.findChildrenByLabel("criteriaField", false);
				if (!fieldElements.isEmpty()) {
					String fieldName = PullRequestQuery.getValue(fieldElements.iterator().next().getMatchedText());
					try {
						PullRequestQuery.checkField(getProject(), fieldName, PullRequestQuery.getOperator(suggestedLiteral));
					} catch (OneException e) {
						return null;
					}
				}
			}
		} 
		return super.describe(expectedElement, suggestedLiteral);
	}

}
