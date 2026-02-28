package io.onedev.server.web.behavior;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.jspecify.annotations.Nullable;

import com.google.common.base.Optional;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.Element;
import io.onedev.commons.codeassist.parser.ParseExpect;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.ai.QueryDescriptions;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.workspace.WorkspaceQuery;
import io.onedev.server.search.entity.workspace.WorkspaceQueryLexer;
import io.onedev.server.search.entity.workspace.WorkspaceQueryParser;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.behavior.inputassist.NaturalLanguageTranslator;
import io.onedev.server.web.util.SuggestionUtils;

public class WorkspaceQueryBehavior extends ANTLRAssistBehavior {

	private final IModel<Project> projectModel;

	private final boolean withOrder;

	public WorkspaceQueryBehavior(boolean withOrder) {
		this(null, withOrder);
	}

	public WorkspaceQueryBehavior(@Nullable IModel<Project> projectModel, boolean withOrder) {
		super(WorkspaceQueryParser.class, "query", false);
		this.projectModel = projectModel;
		this.withOrder = withOrder;
	}

	@Override
	public void detach(Component component) {
		super.detach(component);
		if (projectModel != null)
			projectModel.detach();
	}

	@Nullable
	private Project getProject() {
		return projectModel != null ? projectModel.getObject() : null;
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
							List<String> fields = new ArrayList<>(Workspace.QUERY_FIELDS);
							if (project != null)
								fields.remove(Workspace.NAME_PROJECT);
							return SuggestionUtils.suggest(fields, matchWith);
						} else if ("orderField".equals(spec.getLabel())) {
							List<String> candidates = new ArrayList<>(Workspace.SORT_FIELDS.keySet());
							if (project != null)
								candidates.remove(Workspace.NAME_PROJECT);
							return SuggestionUtils.suggest(candidates, matchWith);
						} else if ((criteriaValueExpect = terminalExpect.findExpectByLabel("criteriaValue")) != null) {
							List<Element> fieldElements = criteriaValueExpect.getState().findMatchedElementsByLabel("criteriaField", true);
							List<Element> operatorElements = criteriaValueExpect.getState().findMatchedElementsByLabel("operator", true);
							if (fieldElements.isEmpty() && operatorElements.size() == 1) {
								String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
								int operator = WorkspaceQuery.getOperator(operatorName);
								if (operator == WorkspaceQueryLexer.CreatedBy)
									return SuggestionUtils.suggestUsers(matchWith);
							} else if (!fieldElements.isEmpty() && operatorElements.size() == 1) {
								String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
								int operator = WorkspaceQuery.getOperator(operatorName);
								String fieldName = WorkspaceQuery.getValue(fieldElements.get(0).getMatchedText());
								try {
									WorkspaceQuery.checkField(fieldName, operator);
									switch (fieldName) {
										case Workspace.NAME_PROJECT:
											if (!matchWith.contains("*"))
												return SuggestionUtils.suggestProjectPaths(matchWith);
											else
												return null;
										case Workspace.NAME_BRANCH:
											if (!matchWith.contains("*"))
												return SuggestionUtils.suggestBranches(project, matchWith);
											else
												return null;
										case Workspace.NAME_SPEC:
											if (project != null && !matchWith.contains("*"))
												return SuggestionUtils.suggestWorkspaceSpecs(project, matchWith);
											else
												return null;
										case Workspace.NAME_CREATE_DATE:
											case Workspace.NAME_ACTIVE_DATE:
												List<InputSuggestion> suggestions = SuggestionUtils.suggest(DateUtils.RELAX_DATE_EXAMPLES, matchWith);
												return !suggestions.isEmpty() ? suggestions : null;
											case AbstractEntity.NAME_NUMBER:
												return SuggestionUtils.suggestWorkspaces(project, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
											default:
												return new ArrayList<>();
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
						return _T("enclose with ~ to query branch");
					}

				}.suggest(terminalExpect);
			}
		}
		return null;
	}

	@Override
	protected boolean isFuzzySuggestion(InputCompletion suggestion) {
		return suggestion.getLabel().startsWith("~")
				&& suggestion.getLabel().endsWith("~");
	}

	@Override
	protected Optional<String> describe(ParseExpect parseExpect, String suggestedLiteral) {
		if (!withOrder && suggestedLiteral.equals(WorkspaceQuery.getRuleName(WorkspaceQueryLexer.OrderBy)))
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
				String fieldName = WorkspaceQuery.getValue(fieldElements.iterator().next().getMatchedText());
				try {
					WorkspaceQuery.checkField(fieldName, WorkspaceQuery.getOperator(suggestedLiteral));
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
					String fieldName = WorkspaceQuery.getValue(fieldElements.get(0).getMatchedText());
					if (fieldName.equals(Workspace.NAME_PROJECT)) {
						hints.add(_T("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>"));
					} else if (fieldName.equals(Workspace.NAME_BRANCH)) {
						hints.add(_T("Use '*' for wildcard match"));
					} else if (fieldName.equals(Workspace.NAME_SPEC)) {
						hints.add(_T("Use '*' for wildcard match"));
					}
				}
			}
		}
		if (getSettingService().getAiSetting().getLiteModelSetting() == null)
			hints.add(_T("<a href='/~administration/settings/lite-ai-model' target='_blank'>Set up AI</a> to query with natural language</a>"));
		return hints;
	}

	@Override
	protected NaturalLanguageTranslator getNaturalLanguageTranslator() {
		var liteModel = getSettingService().getAiSetting().getLiteModel();
		if (liteModel != null) {
			return new NaturalLanguageTranslator(liteModel) {
				@Override
				public String getQueryDescription() {
					return QueryDescriptions.getWorkspaceQueryDescription();
				}
			};
		} else {
			return null;
		}
	}

	private SettingService getSettingService() {
		return OneDev.getInstance(SettingService.class);
	}

}
