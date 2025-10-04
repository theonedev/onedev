package io.onedev.server.web.behavior;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.Element;
import io.onedev.commons.codeassist.parser.ParseExpect;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.search.entity.project.ProjectQueryLexer;
import io.onedev.server.search.entity.project.ProjectQueryParser;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static io.onedev.server.search.entity.project.ProjectQuery.getRuleName;
import static io.onedev.server.search.entity.project.ProjectQueryParser.*;
import static io.onedev.server.web.translation.Translation._T;

public class ProjectQueryBehavior extends ANTLRAssistBehavior {

	private static final String FUZZY_SUGGESTION_DESCRIPTION_PREFIX = "enclose with ~";
	
	private final boolean childQuery;
	
	public ProjectQueryBehavior(boolean childQuery) {
		super(ProjectQueryParser.class, "query", false);
		this.childQuery = childQuery;
	}
	
	@Override
	protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
			if (spec.getRuleName().equals("Quoted")) {
				return new FenceAware(codeAssist.getGrammar(), '"', '"') {

					@Override
					protected List<InputSuggestion> match(String matchWith) {
						ParseExpect criteriaValueExpect;
						if ("criteriaField".equals(spec.getLabel())) {
							List<String> candidates = new ArrayList<>(Project.QUERY_FIELDS);
							if (childQuery)
								candidates.remove(Project.NAME_PATH);
							if (OneDev.getInstance(SettingService.class).getServiceDeskSetting() == null)
								candidates.remove(Project.NAME_SERVICE_DESK_EMAIL_ADDRESS);
							return SuggestionUtils.suggest(candidates, matchWith);
						} else if ("orderField".equals(spec.getLabel())) {
							List<String> candidates = new ArrayList<>(Project.SORT_FIELDS.keySet());
							if (OneDev.getInstance(SettingService.class).getServiceDeskSetting() == null)
								candidates.remove(Project.NAME_SERVICE_DESK_EMAIL_ADDRESS);
							return SuggestionUtils.suggest(candidates, matchWith);
						} else if ((criteriaValueExpect = terminalExpect.findExpectByLabel("criteriaValue")) != null) {
							List<Element> fieldElements = criteriaValueExpect.getState().findMatchedElementsByLabel("criteriaField", true);
							List<Element> operatorElements = criteriaValueExpect.getState().findMatchedElementsByLabel("operator", true);
							Preconditions.checkState(operatorElements.size() == 1);
							String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
							int operator = ProjectQuery.getOperator(operatorName);							
							if (fieldElements.isEmpty()) {
								if (operator == ProjectQueryLexer.ForksOf || operator == ProjectQueryLexer.ChildrenOf) {
									if (!matchWith.contains("*"))
										return SuggestionUtils.suggestProjectPaths(matchWith);
									else
										return null;
								} else { 
									return SuggestionUtils.suggestUsers(matchWith);
								}
							} else {
								String fieldName = ProjectQuery.getValue(fieldElements.get(0).getMatchedText());
								try {
									ProjectQuery.checkField(fieldName, operator);
									if (fieldName.equals(Project.NAME_LAST_ACTIVITY_DATE)) {
										List<InputSuggestion> suggestions = SuggestionUtils.suggest(DateUtils.RELAX_DATE_EXAMPLES, matchWith);
										return !suggestions.isEmpty()? suggestions: null;
									} else if (fieldName.equals(Project.NAME_NAME)) {
										if (!matchWith.contains("*"))
											return SuggestionUtils.suggestProjectNames(matchWith);
										else
											return null;
									} else if (fieldName.equals(Project.NAME_KEY)) {
										if (!matchWith.contains("*"))
											return SuggestionUtils.suggestProjectKeys(matchWith);
										else
											return null;
									} else if (fieldName.equals(Project.NAME_PATH)) {
										if (!matchWith.contains("*"))
											return SuggestionUtils.suggestProjectPaths(matchWith);
										else
											return null;
									} else if (fieldName.equals(Project.NAME_LABEL)) {
										return SuggestionUtils.suggestLabels(matchWith);
									} else if (fieldName.equals(Project.NAME_SERVICE_DESK_EMAIL_ADDRESS)) {
										if (!matchWith.contains("*")) {
											ProjectService projectService = OneDev.getInstance(ProjectService.class);
											ProjectCache cache = projectService.cloneCache();
											Collection<Project> projects = SecurityUtils.getAuthorizedProjects(new AccessProject());
											List<String> serviceDeskNames = projects.stream()
													.map(it->cache.get(it.getId()).getServiceDeskEmailAddress())
													.filter(it-> it != null)
													.sorted()
													.collect(Collectors.toList());
											return SuggestionUtils.suggest(serviceDeskNames, matchWith);
										} else {
											return null;
										}
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
						return _T("enclose with ~ to query name/path");
					}

				}.suggest(terminalExpect);
			}
		} 
		return null;
	}
	
	@Override
	protected Optional<String> describe(ParseExpect parseExpect, String suggestedLiteral) {
		if (!OneDev.getInstance(ClusterService.class).isClusteringSupported()
				&& (suggestedLiteral.equals(getRuleName(WithoutEnoughReplicas)) || suggestedLiteral.equals(getRuleName(HasOutdatedReplicas)))) {
			return null;
		} else if (suggestedLiteral.equals(",")) {
			if (parseExpect.findExpectByLabel("orderOperator") != null)
				return Optional.of(_T("add another order"));
			else
				return Optional.of(_T("or match another value"));
		}
		
		if (childQuery) {
			if (suggestedLiteral.equals(getRuleName(ChildrenOf))
					|| suggestedLiteral.equals(getRuleName(Roots))) { 
				return null;
			}
		}
		parseExpect = parseExpect.findExpectByLabel("operator");
		if (parseExpect != null) {
			List<Element> fieldElements = parseExpect.getState().findMatchedElementsByLabel("criteriaField", false);
			if (!fieldElements.isEmpty()) {
				String fieldName = ProjectQuery.getValue(fieldElements.iterator().next().getMatchedText());
				try {
					ProjectQuery.checkField(fieldName, ProjectQuery.getOperator(suggestedLiteral));
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
					if (fieldName.equals(Project.NAME_NAME)
							|| fieldName.equals(Project.NAME_KEY) 
							|| fieldName.equals(Project.NAME_SERVICE_DESK_EMAIL_ADDRESS)) {
						hints.add(_T("Use '*' for wildcard match"));
					} else if (fieldName.equals(Project.NAME_DESCRIPTION)) {
						hints.add(_T("Use '*' for wildcard match"));
						hints.add(_T("Use '\\' to escape quotes"));
					} else if (fieldName.equals(Project.NAME_PATH)) {
						hints.add(_T("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>"));
					}
				} else {
					Element operatorElement = terminalExpect.getState()
							.findMatchedElementsByLabel("operator", true).iterator().next();
					int type = operatorElement.getMatchedTokens().iterator().next().getType();
					if (type == ProjectQueryLexer.ForksOf || type == ProjectQueryLexer.ChildrenOf) 
						hints.add(_T("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>"));
					else
						hints.add(_T("Use '\\' to escape quotes"));
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
