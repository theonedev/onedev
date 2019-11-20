package io.onedev.server.web.behavior;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.Element;
import io.onedev.commons.codeassist.parser.ParseExpect;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.search.entity.build.BuildQueryLexer;
import io.onedev.server.search.entity.build.BuildQueryParser;
import io.onedev.server.util.BuildConstants;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class BuildQueryBehavior extends ANTLRAssistBehavior {

	private final IModel<Project> projectModel;
	
	public BuildQueryBehavior(IModel<Project> projectModel) {
		super(BuildQueryParser.class, "query", false);
		this.projectModel = projectModel;
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
							List<String> fields = new ArrayList<>(BuildConstants.QUERY_FIELDS);
							BuildParamManager buildParamManager = OneDev.getInstance(BuildParamManager.class);
							List<String> paramNames = new ArrayList<>(buildParamManager.getBuildParamNames());
							Collections.sort(paramNames);
							fields.addAll(paramNames);
							return SuggestionUtils.suggest(fields, matchWith);
						} else if ("orderField".equals(spec.getLabel())) {
							return SuggestionUtils.suggest(new ArrayList<>(BuildConstants.ORDER_FIELDS.keySet()), matchWith);
						} else if ("criteriaValue".equals(spec.getLabel())) {
							List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
							List<Element> operatorElements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
							Preconditions.checkState(operatorElements.size() == 1);
							String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
							int operator = BuildQuery.getOperator(operatorName);							
							if (fieldElements.isEmpty()) {
								if (operator == BuildQueryLexer.SubmittedBy || operator == BuildQueryLexer.CancelledBy)
									return SuggestionUtils.suggestUsers(matchWith);
								else if (operator == BuildQueryLexer.DependsOn || operator == BuildQueryLexer.DependenciesOf)
									return SuggestionUtils.suggestBuilds(project, matchWith);
								else if (operator == BuildQueryLexer.FixedIssue)
									return SuggestionUtils.suggestIssues(project, matchWith);
								else 
									return SuggestionUtils.suggestPullRequests(project, matchWith);
							} else {
								String fieldName = BuildQuery.getValue(fieldElements.get(0).getMatchedText());
 								try {
									BuildQuery.checkField(project, fieldName, operator);
									if (fieldName.equals(BuildConstants.FIELD_SUBMIT_DATE) 
											|| fieldName.equals(BuildConstants.FIELD_QUEUEING_DATE)
											|| fieldName.equals(BuildConstants.FIELD_RUNNING_DATE)
											|| fieldName.equals(BuildConstants.FIELD_FINISH_DATE)) {
										List<InputSuggestion> suggestions = SuggestionUtils.suggest(DateUtils.RELAX_DATE_EXAMPLES, matchWith);
										return !suggestions.isEmpty()? suggestions: null;
									} else if (fieldName.equals(BuildConstants.FIELD_JOB)) {
										if (project != null)
											return SuggestionUtils.suggestJobs(project, matchWith);
										else
											return null;
									} else if (fieldName.equals(BuildConstants.FIELD_NUMBER)) {
										return SuggestionUtils.suggestBuilds(project, matchWith);
									} else {
										BuildParamManager buildParamManager = OneDev.getInstance(BuildParamManager.class);
										List<String> paramValues = new ArrayList<>(buildParamManager.getBuildParamValues(fieldName));
										Collections.sort(paramValues);
										List<InputSuggestion> suggestions = SuggestionUtils.suggest(paramValues, matchWith);
										return !suggestions.isEmpty()? suggestions: null;
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
		if (SecurityUtils.getUser() == null 
				&& (suggestedLiteral.equals(BuildQuery.getRuleName(BuildQueryLexer.SubmittedByMe)) 
						|| suggestedLiteral.equals(BuildQuery.getRuleName(BuildQueryLexer.CancelledByMe)))) {
			return null;
		}
		
		parseExpect = parseExpect.findExpectByLabel("operator");
		if (parseExpect != null) {
			List<Element> fieldElements = parseExpect.getState().findMatchedElementsByLabel("criteriaField", false);
			if (!fieldElements.isEmpty()) {
				String fieldName = BuildQuery.getValue(fieldElements.iterator().next().getMatchedText());
				try {
					BuildQuery.checkField(getProject(), fieldName, BuildQuery.getOperator(suggestedLiteral));
				} catch (OneException e) {
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
				String unmatched = terminalExpect.getUnmatchedText();
				if (unmatched.indexOf('"') == unmatched.lastIndexOf('"')) { // only when we input criteria value
					List<Element> elements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
					if (!elements.isEmpty() && elements.get(0).getFirstMatchedToken().getText().equals("\"" + BuildConstants.FIELD_VERSION + "\""))
						hints.add("Use * to match any part of version");
				}
			}
		} 
		return hints;
	}
	
}
