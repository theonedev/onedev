package io.onedev.server.web.behavior;

import static io.onedev.server.search.entity.pullrequest.PullRequestQuery.getRuleName;
import static io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer.IncludesCommit;
import static io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer.IncludesIssue;

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
import io.onedev.server.OneException;
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
							return SuggestionUtils.suggest(PullRequestConstants.QUERY_FIELDS, matchWith);
						} else if ("orderField".equals(spec.getLabel())) {
							return SuggestionUtils.suggest(new ArrayList<>(PullRequestConstants.ORDER_FIELDS.keySet()), matchWith);
						} else if ("criteriaValue".equals(spec.getLabel())) {
							List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
							List<Element> operatorElements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
							Preconditions.checkState(operatorElements.size() == 1);
							String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
							int operator = PullRequestQuery.getOperator(operatorName);							
							if (fieldElements.isEmpty()) {
								if (operator == PullRequestQueryLexer.IncludesIssue)
									return SuggestionUtils.suggestIssues(project, matchWith);
								else if (operator != PullRequestQueryLexer.IncludesCommit)
									return SuggestionUtils.suggestUsers(matchWith);
								else
									return null;
							} else {
								String fieldName = PullRequestQuery.getValue(fieldElements.get(0).getMatchedText());
								try {
									PullRequestQuery.checkField(fieldName, operator);
									if (fieldName.equals(PullRequestConstants.FIELD_SUBMIT_DATE) 
											|| fieldName.equals(PullRequestConstants.FIELD_UPDATE_DATE)
											|| fieldName.equals(PullRequestConstants.FIELD_CLOSE_DATE)) {
										List<InputSuggestion> suggestions = SuggestionUtils.suggest(DateUtils.RELAX_DATE_EXAMPLES, matchWith);
										return !suggestions.isEmpty()? suggestions: null;
									} else if (fieldName.equals(PullRequestConstants.FIELD_SOURCE_PROJECT)) {
										return SuggestionUtils.suggestProjects(matchWith);
									} else if (fieldName.equals(PullRequestConstants.FIELD_TARGET_BRANCH) 
											|| fieldName.equals(PullRequestConstants.FIELD_SOURCE_BRANCH)) {
										return SuggestionUtils.suggestBranches(project, matchWith);
									} else if (fieldName.equals(PullRequestConstants.FIELD_MERGE_STRATEGY)) {
										List<String> candidates = new ArrayList<>();
										for (MergeStrategy strategy: MergeStrategy.values())
											candidates.add(strategy.toString());
										return SuggestionUtils.suggest(candidates, matchWith);
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
		if (getProject() == null && (suggestedLiteral.equals(getRuleName(IncludesCommit)) || suggestedLiteral.equals(getRuleName(IncludesIssue))))
			return null;
		
		parseExpect = parseExpect.findExpectByLabel("operator");
		if (parseExpect != null) {
			List<Element> fieldElements = parseExpect.getState().findMatchedElementsByLabel("criteriaField", false);
			if (!fieldElements.isEmpty()) {
				String fieldName = PullRequestQuery.getValue(fieldElements.iterator().next().getMatchedText());
				try {
					PullRequestQuery.checkField(fieldName, PullRequestQuery.getOperator(suggestedLiteral));
				} catch (OneException e) {
					return null;
				}
			}
		}
		return super.describe(parseExpect, suggestedLiteral);
	}

}
