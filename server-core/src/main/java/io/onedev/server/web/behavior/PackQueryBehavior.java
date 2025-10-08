package io.onedev.server.web.behavior;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.onedev.commons.codeassist.AntlrUtils;
import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.Element;
import io.onedev.commons.codeassist.parser.ParseExpect;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.pack.PackQuery;
import io.onedev.server.search.entity.pack.PackQueryLexer;
import io.onedev.server.search.entity.pack.PackQueryParser;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

import static io.onedev.server.model.Pack.*;
import static io.onedev.server.web.translation.Translation._T;

public class PackQueryBehavior extends ANTLRAssistBehavior {

	private static final String FUZZY_SUGGESTION_DESCRIPTION_PREFIX = "enclose with ~";
	
	private final IModel<Project> projectModel;
	
	private final String packType;

	private final boolean withCurrentUserCriteria;
	
	private final boolean withOrder;
	
	public PackQueryBehavior(IModel<Project> projectModel, @Nullable String packType, 
							 boolean withOrder, boolean withCurrentUserCriteria) {
		super(PackQueryParser.class, "query", false);
		this.projectModel = projectModel;
		this.packType = packType;
		this.withOrder = withOrder;
		this.withCurrentUserCriteria = withCurrentUserCriteria;
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
						ParseExpect criteriaValueExpect;
						if ("criteriaField".equals(spec.getLabel())) {
							List<String> fields = new ArrayList<>(Pack.QUERY_FIELDS);
							if (getProject() != null)
								fields.remove(Pack.NAME_PROJECT);
							if (packType != null)
								fields.remove(NAME_TYPE);
							return SuggestionUtils.suggest(fields, matchWith);
						} else if ("orderField".equals(spec.getLabel())) {
							List<String> candidates = new ArrayList<>(Pack.SORT_FIELDS.keySet());
							if (getProject() != null)
								candidates.remove(Pack.NAME_PROJECT);
							return SuggestionUtils.suggest(candidates, matchWith);
						} else if ((criteriaValueExpect = terminalExpect.findExpectByLabel("criteriaValue")) != null) {
							List<Element> fieldElements = criteriaValueExpect.getState().findMatchedElementsByLabel("criteriaField", true);
							List<Element> operatorElements = criteriaValueExpect.getState().findMatchedElementsByLabel("operator", true);
							Preconditions.checkState(operatorElements.size() == 1);
							String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
							int operator = PackQuery.getOperator(operatorName);							
							if (fieldElements.isEmpty()) {
								if (operator == PackQueryLexer.PublishedByUser) {
									return SuggestionUtils.suggestUsers(matchWith);
								} else if (operator == PackQueryLexer.PublishedByBuild) {
									return SuggestionUtils.suggestBuilds(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
								} else {
									if (!matchWith.contains("*"))
										return SuggestionUtils.suggestProjectPaths(matchWith);
									else
										return null;
								}
							} else {
								String fieldName = PackQuery.getValue(fieldElements.get(0).getMatchedText());
								try {
									PackQuery.checkField(project, fieldName, operator);
									if (fieldName.equals(Pack.NAME_PUBLISH_DATE)) {
										List<InputSuggestion> suggestions = SuggestionUtils.suggest(DateUtils.RELAX_DATE_EXAMPLES, matchWith);
										return !suggestions.isEmpty()? suggestions: null;
									} else if (fieldName.equals(Pack.NAME_PROJECT)) {
										if (!matchWith.contains("*"))
											return SuggestionUtils.suggestProjectPaths(matchWith);
										else
											return null;
									} else if (fieldName.equals(NAME_TYPE)) {
										return SuggestionUtils.suggestPackTypes(matchWith);
									} else if (fieldName.equals(NAME_NAME)) {
										if (project != null && !matchWith.contains("*"))
											return SuggestionUtils.suggestPackProps(project, PROP_NAME, matchWith);
										else
											return null;
									} else if (fieldName.equals(NAME_VERSION)) {
										if (project != null && !matchWith.contains("*"))
											return SuggestionUtils.suggestPackProps(project, PROP_VERSION, matchWith);
										else
											return null;
									} else if (fieldName.equals(NAME_LABEL)) {
										return SuggestionUtils.suggestLabels(matchWith);
									} else {
										return null;
									}
								} catch (ExplicitException ignored) {
								}
							}
						}
						return new ArrayList<>();
					}
					
					@Override
					protected String getFencingDescription() {
						return _T("value should be quoted");
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
						return _T("enclose with ~ to query name/version");
					}

				}.suggest(terminalExpect);
			}
		} 
		return null;
	}
	
	@Override
	protected Optional<String> describe(ParseExpect parseExpect, String suggestedLiteral) {
		if (!withOrder && suggestedLiteral.equals(PackQuery.getRuleName(PackQueryLexer.OrderBy)))
			return null;
		if (!withCurrentUserCriteria && suggestedLiteral.equals(PackQuery.getRuleName(PackQueryLexer.PublishedByMe))) 
			return null;
		if (suggestedLiteral.equals(",")) {
			if (parseExpect.findExpectByLabel("orderOperator") != null)
				return Optional.of(_T("add another order"));
			else
				return Optional.of(_T("or match another value"));
		}
		
		parseExpect = parseExpect.findExpectByLabel("operator");
		if (parseExpect != null) {
			List<Element> fieldElements = parseExpect.getState().findMatchedElementsByLabel("criteriaField", false);
			if (!fieldElements.isEmpty()) {
				String fieldName = PackQuery.getValue(fieldElements.iterator().next().getMatchedText());
				try {
					PackQuery.checkField(getProject(), fieldName, PackQuery.getOperator(suggestedLiteral));
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
				List<Element> operatorElements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
				if (!fieldElements.isEmpty()) {
					String fieldName = PackQuery.getValue(fieldElements.get(0).getMatchedText());
					if (fieldName.equals(Pack.NAME_PROJECT)) {
						hints.add(_T("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>"));
					} else if (fieldName.equals(NAME_NAME) || fieldName.equals(NAME_VERSION)) {
						hints.add(_T("Use '*' for wildcard match"));
						hints.add(_T("Use '\\' to escape quotes"));
					}
				}
				if (!operatorElements.isEmpty()) {
					String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
					int operator = AntlrUtils.getLexerRule(PackQueryLexer.ruleNames, operatorName);
					if (operator == PackQueryLexer.PublishedByProject) 
						hints.add(_T("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>"));
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
