package io.onedev.server.web.behavior;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import io.onedev.codeassist.FenceAware;
import io.onedev.codeassist.InputSuggestion;
import io.onedev.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.codeassist.parser.Element;
import io.onedev.codeassist.parser.ParseExpect;
import io.onedev.codeassist.parser.TerminalExpect;
import io.onedev.server.exception.OneException;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.build.BuildConstants;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.search.entity.build.BuildQueryParser;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class BuildQueryBehavior extends ANTLRAssistBehavior {

	private final IModel<Project> projectModel;
	
	private static final String VALUE_OPEN = "\"";
	
	private static final String VALUE_CLOSE = "\"";
	
	private static final String ESCAPE_CHARS = "\"\\";
	
	public BuildQueryBehavior(IModel<Project> projectModel) {
		super(BuildQueryParser.class, "query", false);
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
				return new FenceAware(codeAssist.getGrammar(), VALUE_OPEN, VALUE_CLOSE) {

					@Override
					protected List<InputSuggestion> match(String unfencedMatchWith) {
						String unfencedLowerCaseMatchWith = unfencedMatchWith.toLowerCase();
						List<InputSuggestion> suggestions = new ArrayList<>();
						Project project = getProject();

						if ("criteriaField".equals(spec.getLabel())) {
							suggestions.addAll(SuggestionUtils.suggest(BuildConstants.QUERY_FIELDS, unfencedLowerCaseMatchWith, ESCAPE_CHARS));
						} else if ("orderField".equals(spec.getLabel())) {
							suggestions.addAll(SuggestionUtils.suggest(new ArrayList<>(BuildConstants.ORDER_FIELDS.keySet()), 
									unfencedLowerCaseMatchWith, ESCAPE_CHARS));
						} else if ("criteriaValue".equals(spec.getLabel())) {
							List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
							if (fieldElements.isEmpty()) {
								suggestions.addAll(SuggestionUtils.suggestIssue(project, unfencedLowerCaseMatchWith, ESCAPE_CHARS));
							} else {
								String fieldName = BuildQuery.getValue(fieldElements.get(0).getMatchedText());
								List<Element> operatorElements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
								Preconditions.checkState(operatorElements.size() == 1);
								String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
								int operator = BuildQuery.getOperator(operatorName);							
 								try {
									BuildQuery.checkField(project, fieldName, operator);
									if (fieldName.equals(BuildConstants.FIELD_BUILD_DATE)) {
										suggestions.addAll(SuggestionUtils.suggest(DateUtils.RELAX_DATE_EXAMPLES, 
												unfencedLowerCaseMatchWith, null));
										CollectionUtils.addIgnoreNull(suggestions, suggestToFence(terminalExpect, unfencedMatchWith));
									} else if (fieldName.equals(BuildConstants.FIELD_CONFIGURATION)) {
										suggestions.addAll(SuggestionUtils.suggestConfiguration(projectModel.getObject(), 
												unfencedLowerCaseMatchWith, ESCAPE_CHARS));
									} else if (fieldName.equals(BuildConstants.FIELD_VERSION)) {
										suggestions.addAll(SuggestionUtils.suggestBuild(project, unfencedLowerCaseMatchWith, false, ESCAPE_CHARS));
									} else {
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
					List<Element> elements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
					if (!elements.isEmpty() && elements.get(0).getFirstMatchedToken().getType() == BuildQueryParser.Matches)
						hints.add("Use * to match any part of version");
				}
			}
		} 
		return hints;
	}
	
}
