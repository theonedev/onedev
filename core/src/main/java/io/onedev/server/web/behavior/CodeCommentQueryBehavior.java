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
import io.onedev.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.codeassist.parser.Element;
import io.onedev.codeassist.parser.TerminalExpect;
import io.onedev.codeassist.parser.ParseExpect;
import io.onedev.server.OneDev;
import io.onedev.server.entityquery.codecomment.CodeCommentQuery;
import io.onedev.server.entityquery.codecomment.CodeCommentQueryParser;
import io.onedev.server.exception.OneException;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.utils.Range;

@SuppressWarnings("serial")
public class CodeCommentQueryBehavior extends ANTLRAssistBehavior {

	private final IModel<Project> projectModel;
	
	private static final String VALUE_OPEN = "\"";
	
	private static final String VALUE_CLOSE = "\"";
	
	private static final String ESCAPE_CHARS = "\"\\";
	
	public CodeCommentQueryBehavior(IModel<Project> projectModel) {
		super(CodeCommentQueryParser.class, "query", false);
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
								suggestions.add(new InputSuggestion(user.getName(), description, match).escape(ESCAPE_CHARS));
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
							List<String> candidates = new ArrayList<>(CodeComment.FIELD_PATHS.keySet());
							suggestions.addAll(getSuggestions(candidates, unfencedLowerCaseMatchWith, ESCAPE_CHARS));
						} else if ("orderField".equals(spec.getLabel())) {
							List<String> candidates = Lists.newArrayList(CodeComment.FIELD_CREATE_DATE, 
									CodeComment.FIELD_UPDATE_DATE);
							suggestions.addAll(getSuggestions(candidates, unfencedLowerCaseMatchWith, ESCAPE_CHARS));
						} else if ("criteriaValue".equals(spec.getLabel())) {
							List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
							if (fieldElements.isEmpty()) {
								suggestions.addAll(getUserSuggestions(unfencedLowerCaseMatchWith));
							} else {
								String fieldName = CodeCommentQuery.getValue(fieldElements.get(0).getMatchedText());
								List<Element> operatorElements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
								Preconditions.checkState(operatorElements.size() == 1);
								String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
								int operator = CodeCommentQuery.getOperator(operatorName);							
								try {
									CodeCommentQuery.checkField(project, fieldName, operator);
									if (fieldName.equals(CodeComment.FIELD_CREATE_DATE) || fieldName.equals(CodeComment.FIELD_UPDATE_DATE)) {
										suggestions.addAll(getSuggestions(DateUtils.RELAX_DATE_EXAMPLES, unfencedLowerCaseMatchWith, null));
										CollectionUtils.addIgnoreNull(suggestions, suggestToFence(terminalExpect, unfencedMatchWith));
									} else if (fieldName.equals(CodeComment.FIELD_PATH)) {
										suggestions.addAll(SuggestionUtils.suggestPath(projectModel.getObject(), unfencedLowerCaseMatchWith, ESCAPE_CHARS));
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
				String fieldName = CodeCommentQuery.getValue(fieldElements.iterator().next().getMatchedText());
				try {
					CodeCommentQuery.checkField(getProject(), fieldName, CodeCommentQuery.getOperator(suggestedLiteral));
				} catch (OneException e) {
					return null;
				}
			}
		}
		return super.describe(parseExpect, suggestedLiteral);
	}

}
