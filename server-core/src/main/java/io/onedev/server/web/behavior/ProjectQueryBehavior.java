package io.onedev.server.web.behavior;

import static io.onedev.server.search.entity.project.ProjectQuery.getRuleName;
import static io.onedev.server.search.entity.project.ProjectQueryParser.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.server.cluster.ClusterManager;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.Element;
import io.onedev.commons.codeassist.parser.ParseExpect;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.search.entity.project.ProjectQueryLexer;
import io.onedev.server.search.entity.project.ProjectQueryParser;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class ProjectQueryBehavior extends ANTLRAssistBehavior {

	private static final String FUZZY_SUGGESTION_DESCRIPTION_PREFIX = "surround with ~";
	
	private final boolean childQuery;
	
	public ProjectQueryBehavior(boolean childQuery, boolean hideIfBlank) {
		super(ProjectQueryParser.class, "query", false, hideIfBlank);
		this.childQuery = childQuery;
	}

	public ProjectQueryBehavior(boolean childQuery) {
		this(childQuery, false);
	}
	
	@Override
	protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
			if (spec.getRuleName().equals("Quoted")) {
				return new FenceAware(codeAssist.getGrammar(), '"', '"') {

					@Override
					protected List<InputSuggestion> match(String matchWith) {
						if ("criteriaField".equals(spec.getLabel())) {
							List<String> candidates = new ArrayList<>(Project.QUERY_FIELDS);
							if (childQuery)
								candidates.remove(Project.NAME_PATH);
							if (OneDev.getInstance(SettingManager.class).getServiceDeskSetting() == null) 
								candidates.remove(Project.NAME_SERVICE_DESK_NAME);
							return SuggestionUtils.suggest(candidates, matchWith);
						} else if ("orderField".equals(spec.getLabel())) {
							List<String> candidates = new ArrayList<>(Project.ORDER_FIELDS.keySet());
							if (OneDev.getInstance(SettingManager.class).getServiceDeskSetting() == null) 
								candidates.remove(Project.NAME_SERVICE_DESK_NAME);
							return SuggestionUtils.suggest(candidates, matchWith);
						} else if ("criteriaValue".equals(spec.getLabel())) {
							List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
							List<Element> operatorElements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
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
									if (fieldName.equals(Project.NAME_LAST_ACTIVITY_DATE) || fieldName.equals(Project.NAME_LAST_COMMIT_DATE)) {
										List<InputSuggestion> suggestions = SuggestionUtils.suggest(DateUtils.RELAX_DATE_EXAMPLES, matchWith);
										return !suggestions.isEmpty()? suggestions: null;
									} else if (fieldName.equals(Project.NAME_NAME)) {
										if (!matchWith.contains("*"))
											return SuggestionUtils.suggestProjectNames(matchWith);
										else
											return null;
									} else if (fieldName.equals(Project.NAME_PATH)) {
										if (!matchWith.contains("*"))
											return SuggestionUtils.suggestProjectPaths(matchWith);
										else
											return null;
									} else if (fieldName.equals(Project.NAME_LABEL)) {
										return SuggestionUtils.suggestLabels(matchWith);
									} else if (fieldName.equals(Project.NAME_SERVICE_DESK_NAME)) {
										if (!matchWith.contains("*")) {
											ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
											ProjectCache cache = projectManager.cloneCache();
											Collection<Project> projects = projectManager.getPermittedProjects(new AccessProject());
											List<String> serviceDeskNames = projects.stream()
													.map(it->cache.get(it.getId()).getServiceDeskName())
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
								} catch (ExplicitException ex) {
								}
							}
						}
						return new ArrayList<>();
					}
					
					@Override
					protected String getFencingDescription() {
						return "value should be quoted";
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
						return FUZZY_SUGGESTION_DESCRIPTION_PREFIX + " to query name/path";
					}

				}.suggest(terminalExpect);
			}
		} 
		return null;
	}
	
	@Override
	protected Optional<String> describe(ParseExpect parseExpect, String suggestedLiteral) {
		if (!OneDev.getInstance(ClusterManager.class).isClusteringSupported() 
				&& (suggestedLiteral.equals(getRuleName(WithoutEnoughReplicas)) || suggestedLiteral.equals(getRuleName(HasOutdatedReplicas)))) {
			return null;
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
					if (fieldName.equals(Project.NAME_NAME) || fieldName.equals(Project.NAME_SERVICE_DESK_NAME)) {
						hints.add("Use '*' for wildcard match");
					} else if (fieldName.equals(Project.NAME_DESCRIPTION)) {
						hints.add("Use '*' for wildcard match");
						hints.add("Use '\\' to escape quotes");
					} else if (fieldName.equals(Project.NAME_PATH)) {
						hints.add("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>");
					}
				} else {
					Element operatorElement = terminalExpect.getState()
							.findMatchedElementsByLabel("operator", true).iterator().next();
					int type = operatorElement.getMatchedTokens().iterator().next().getType();
					if (type == ProjectQueryLexer.ForksOf || type == ProjectQueryLexer.ChildrenOf) 
						hints.add("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>");
					else
						hints.add("Use '\\' to escape quotes");
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
