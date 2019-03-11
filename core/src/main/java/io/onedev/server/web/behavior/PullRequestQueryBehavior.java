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

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.Element;
import io.onedev.commons.codeassist.parser.ParseExpect;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.server.OneDev;
import io.onedev.server.exception.OneException;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryParser;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.PullRequestConstants;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class PullRequestQueryBehavior extends ANTLRAssistBehavior {

	private final IModel<Project> projectModel;
	
	private static final String VALUE_OPEN = "\"";
	
	private static final String VALUE_CLOSE = "\"";
	
	private static final String ESCAPE_CHARS = "\"\\";
	
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

	private List<InputSuggestion> escape(List<InputSuggestion> suggestions) {
		return suggestions.stream().map(it->it.escape(ESCAPE_CHARS)).collect(Collectors.toList());
	}
	
	@Override
	protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
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
							suggestions.addAll(escape(SuggestionUtils.suggest(PullRequestConstants.QUERY_FIELDS, unfencedLowerCaseMatchWith)));
						} else if ("orderField".equals(spec.getLabel())) {
							suggestions.addAll(escape(SuggestionUtils.suggest(new ArrayList<>(PullRequestConstants.ORDER_FIELDS.keySet()), 
									unfencedLowerCaseMatchWith)));
						} else if ("criteriaValue".equals(spec.getLabel())) {
							List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
							List<Element> operatorElements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
							Preconditions.checkState(operatorElements.size() == 1);
							String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
							int operator = PullRequestQuery.getOperator(operatorName);							
							if (fieldElements.isEmpty()) {
								if (operator == PullRequestQueryLexer.IncludesIssue)
									suggestions.addAll(escape(SuggestionUtils.suggestIssues(project, unfencedLowerCaseMatchWith)));
								else if (operator != PullRequestQueryLexer.IncludesCommit)
									suggestions.addAll(escape(SuggestionUtils.suggestUsers(unfencedLowerCaseMatchWith)));
								else
									return null;
							} else {
								String fieldName = PullRequestQuery.getValue(fieldElements.get(0).getMatchedText());
								try {
									PullRequestQuery.checkField(project, fieldName, operator);
									if (fieldName.equals(PullRequestConstants.FIELD_SUBMIT_DATE) 
											|| fieldName.equals(PullRequestConstants.FIELD_UPDATE_DATE)
											|| fieldName.equals(PullRequestConstants.FIELD_CLOSE_DATE)) {
										suggestions.addAll(SuggestionUtils.suggest(DateUtils.RELAX_DATE_EXAMPLES, unfencedLowerCaseMatchWith));
										CollectionUtils.addIgnoreNull(suggestions, suggestToFence(terminalExpect, unfencedMatchWith));
									} else if (fieldName.equals(PullRequestConstants.FIELD_SOURCE_PROJECT)) {
										List<String> candidates = new ArrayList<>();
										for (Project each: OneDev.getInstance(ProjectManager.class).query())
											candidates.add(each.getName());
										suggestions.addAll(escape(SuggestionUtils.suggest(candidates, unfencedLowerCaseMatchWith)));
									} else if (fieldName.equals(PullRequestConstants.FIELD_TARGET_BRANCH) 
											|| fieldName.equals(PullRequestConstants.FIELD_SOURCE_BRANCH)) {
										List<String> candidates = new ArrayList<>();
										for (RefInfo refInfo: project.getBranches()) 
											candidates.add(GitUtils.ref2branch(refInfo.getRef().getName()));
										suggestions.addAll(escape(SuggestionUtils.suggest(candidates, unfencedLowerCaseMatchWith)));
									} else if (fieldName.equals(PullRequestConstants.FIELD_MERGE_STRATEGY)) {
										List<String> candidates = new ArrayList<>();
										for (MergeStrategy strategy: MergeStrategy.values())
											candidates.add(strategy.toString());
										suggestions.addAll(escape(SuggestionUtils.suggest(candidates, unfencedLowerCaseMatchWith)));
									} else if (fieldName.equals(PullRequestConstants.FIELD_TITLE) 
											|| fieldName.equals(PullRequestConstants.FIELD_DESCRIPTION) 
											|| fieldName.equals(PullRequestConstants.FIELD_NUMBER) 
											|| fieldName.equals(PullRequestConstants.FIELD_COMMENT_COUNT)
											|| fieldName.equals(PullRequestConstants.FIELD_COMMENT)) {
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
					PullRequestQuery.checkField(getProject(), fieldName, PullRequestQuery.getOperator(suggestedLiteral));
				} catch (OneException e) {
					return null;
				}
			}
		}
		return super.describe(parseExpect, suggestedLiteral);
	}

}
