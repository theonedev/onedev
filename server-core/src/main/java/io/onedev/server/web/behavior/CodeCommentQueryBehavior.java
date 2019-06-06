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
import io.onedev.server.OneException;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.codecomment.CodeCommentQuery;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryLexer;
import io.onedev.server.search.entity.codecomment.CodeCommentQueryParser;
import io.onedev.server.util.CodeCommentConstants;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

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
							suggestions.addAll(escape(SuggestionUtils.suggest(CodeCommentConstants.QUERY_FIELDS, unfencedLowerCaseMatchWith)));
						} else if ("orderField".equals(spec.getLabel())) {
							suggestions.addAll(escape(SuggestionUtils.suggest(new ArrayList<>(CodeCommentConstants.ORDER_FIELDS.keySet()), unfencedLowerCaseMatchWith)));
						} else if ("criteriaValue".equals(spec.getLabel())) {
							List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
							List<Element> operatorElements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
							Preconditions.checkState(operatorElements.size() == 1);
							String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
							int operator = CodeCommentQuery.getOperator(operatorName);							
							if (fieldElements.isEmpty()) {
								if (operator == CodeCommentQueryLexer.CreatedBy) {
									suggestions.addAll(SuggestionUtils.suggestUsers(unfencedLowerCaseMatchWith));
								} else {
									return null;
								}
							} else {
								String fieldName = CodeCommentQuery.getValue(fieldElements.get(0).getMatchedText());
								try {
									CodeCommentQuery.checkField(project, fieldName, operator);
									if (fieldName.equals(CodeCommentConstants.FIELD_CREATE_DATE) || fieldName.equals(CodeCommentConstants.FIELD_UPDATE_DATE)) {
										suggestions.addAll(SuggestionUtils.suggest(DateUtils.RELAX_DATE_EXAMPLES, unfencedLowerCaseMatchWith));
										CollectionUtils.addIgnoreNull(suggestions, suggestToFence(terminalExpect, unfencedMatchWith));
									} else if (fieldName.equals(CodeCommentConstants.FIELD_PATH)) {
										suggestions.addAll(escape(SuggestionUtils.suggestBlobs(projectModel.getObject(), unfencedLowerCaseMatchWith)));
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
