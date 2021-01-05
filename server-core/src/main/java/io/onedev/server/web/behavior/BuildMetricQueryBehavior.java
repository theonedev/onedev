package io.onedev.server.web.behavior;

import java.util.ArrayList;
import java.util.List;

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
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.BuildMetric;
import io.onedev.server.search.buildmetric.BuildMetricQuery;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.buildmetric.BuildMetricQueryParser;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class BuildMetricQueryBehavior extends ANTLRAssistBehavior {

	private final IModel<Project> projectModel;
	
	private final Class<? extends AbstractEntity> metricClass;
	
	public BuildMetricQueryBehavior(IModel<Project> projectModel, Class<? extends AbstractEntity> metricClass) {
		super(BuildMetricQueryParser.class, "query", false);
		this.projectModel = projectModel;
		this.metricClass = metricClass;
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
							List<String> fields = new ArrayList<>(Build.METRIC_QUERY_FIELDS);
							BuildParamManager buildParamManager = OneDev.getInstance(BuildParamManager.class);
							List<String> paramNames = new ArrayList<>(buildParamManager.getBuildParamNames(project));
							Collections.sort(paramNames);
							fields.addAll(paramNames);
							return SuggestionUtils.suggest(fields, matchWith);
						} else if ("criteriaValue".equals(spec.getLabel())) {
							List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
							List<Element> operatorElements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
							Preconditions.checkState(operatorElements.size() == 1);
							String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
							int operator = BuildMetricQuery.getOperator(operatorName);							
							if (fieldElements.isEmpty()) {
								List<InputSuggestion> suggestions = SuggestionUtils.suggest(DateUtils.RELAX_DATE_EXAMPLES, matchWith);
								return !suggestions.isEmpty()? suggestions: null;
							} else {
								String fieldName = EntityQuery.getValue(fieldElements.get(0).getMatchedText());
								try {
									BuildMetricQuery.checkField(project, fieldName, operator);
									if (fieldName.equals(Build.NAME_JOB)) {
										if (!matchWith.contains("*")) 
											return SuggestionUtils.suggestJobs(project, matchWith);
										else 
											return null;
									} else if (fieldName.equals(BuildMetric.NAME_REPORT)) {
										if (!matchWith.contains("*"))
											return SuggestionUtils.suggestReports(project, metricClass, matchWith);
										else
											return null;
									} else if (fieldName.equals(Build.NAME_BRANCH)) {
										if (!matchWith.contains("*"))
											return SuggestionUtils.suggestBranches(project, matchWith);
										else
											return null;
									} else {
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
				String fieldName = EntityQuery.getValue(fieldElements.iterator().next().getMatchedText());
				try {
					BuildMetricQuery.checkField(getProject(), fieldName, BuildMetricQuery.getOperator(suggestedLiteral));
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
			if ("criteriaValue".equals(spec.getLabel()) && EntityQuery.isInsideQuote(terminalExpect.getUnmatchedText())) {
				List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
				if (!fieldElements.isEmpty()) {
					String fieldName = EntityQuery.getValue(fieldElements.get(0).getMatchedText());
					if (fieldName.equals(Build.NAME_PROJECT) || fieldName.equals(Build.NAME_VERSION)
							|| fieldName.equals(Build.NAME_JOB)) {
						hints.add("Use '*' for wildcard match");
						hints.add("Use '\\' to escape quotes");
					} else if (fieldName.equals(Build.NAME_BRANCH) || fieldName.equals(Build.NAME_TAG)) {
						hints.add("Use '*' for wildcard match");
					}
				}
			}
		} 
		return hints;
	}
	
}
