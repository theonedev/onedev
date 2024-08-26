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
import io.onedev.server.entitymanager.AgentAttributeManager;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.model.Agent;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.search.entity.agent.AgentQueryLexer;
import io.onedev.server.search.entity.agent.AgentQueryParser;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.sort;

@SuppressWarnings("serial")
public class AgentQueryBehavior extends ANTLRAssistBehavior {

	private static final String FUZZY_SUGGESTION_DESCRIPTION_PREFIX = "surround with ~";
	
	private final boolean forExecutor;
	
	public AgentQueryBehavior(boolean forExecutor, boolean hideIfBlank) {
		super(AgentQueryParser.class, "query", false, hideIfBlank);
		this.forExecutor = forExecutor;
	}

	public AgentQueryBehavior(boolean forExecutor) {
		this(forExecutor, false);
	}
	
	@Override
	protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
			if (spec.getRuleName().equals("Quoted")) {
				return new FenceAware(codeAssist.getGrammar(), '"', '"') {

					@Override
					protected List<InputSuggestion> match(String matchWith) {
						AgentManager agentManager = OneDev.getInstance(AgentManager.class);
						AgentAttributeManager attributeManager = OneDev.getInstance(AgentAttributeManager.class);
						if ("criteriaField".equals(spec.getLabel())) {
							var fields = new ArrayList<>(Agent.QUERY_FIELDS);
							var attributeNames = new ArrayList<>(attributeManager.getAttributeNames());
							sort(attributeNames);
							fields.addAll(attributeNames);
							return SuggestionUtils.suggest(fields, matchWith);
						} else if ("orderField".equals(spec.getLabel())) {
							List<String> candidates = new ArrayList<>(Agent.ORDER_FIELDS.keySet());
							return SuggestionUtils.suggest(candidates, matchWith);
						} else if ("criteriaValue".equals(spec.getLabel())) {
							List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
							List<Element> operatorElements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
							Preconditions.checkState(operatorElements.size() == 1);
							String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
							int operator = AgentQuery.getOperator(operatorName);							
							if (fieldElements.isEmpty()) {
								if (operator == AgentQueryLexer.EverUsedSince || operator == AgentQueryLexer.NotUsedSince) {
									return SuggestionUtils.suggest(DateUtils.RELAX_DATE_EXAMPLES, matchWith);
								} else if (operator == AgentQueryLexer.HasAttribute) {
									var attributeNames = new ArrayList<>(attributeManager.getAttributeNames());									
									sort(attributeNames);
									return SuggestionUtils.suggest(attributeNames, matchWith);
								} else if (operator == AgentQueryLexer.RanBuild) { 
									return SuggestionUtils.suggestBuilds(null, matchWith, InputAssistBehavior.MAX_SUGGESTIONS);
								}
							} else {
								String fieldName = AgentQuery.getValue(fieldElements.get(0).getMatchedText());
 								try {
									AgentQuery.checkField(fieldName, operator);
									if (fieldName.equals(Agent.NAME_OS_NAME)) {
										var osNames = new ArrayList<>(agentManager.getOsNames());
										sort(osNames);
										return SuggestionUtils.suggest(osNames, matchWith);
									} else if (fieldName.equals(Agent.NAME_NAME)) {
										return SuggestionUtils.suggestAgents(matchWith);
									} else if (fieldName.equals(Agent.NAME_OS_ARCH)) {
										var osArchs = new ArrayList<>(agentManager.getOsArchs());
										sort(osArchs);
										return SuggestionUtils.suggest(osArchs, matchWith);
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
						return FUZZY_SUGGESTION_DESCRIPTION_PREFIX + " to query name/ip/os";
					}

				}.suggest(terminalExpect);
			}
		} 
		return null;
	}
	
	@Override
	protected Optional<String> describe(ParseExpect parseExpect, String suggestedLiteral) {
		if (forExecutor) {
			if (suggestedLiteral.equals(AgentQuery.getRuleName(AgentQueryLexer.OrderBy))
					|| suggestedLiteral.equals(AgentQuery.getRuleName(AgentQueryLexer.Online))
					|| suggestedLiteral.equals(AgentQuery.getRuleName(AgentQueryLexer.Offline))
					|| suggestedLiteral.equals(AgentQuery.getRuleName(AgentQueryLexer.EverUsedSince))
					|| suggestedLiteral.equals(AgentQuery.getRuleName(AgentQueryLexer.HasRunningBuilds))
					|| suggestedLiteral.equals(AgentQuery.getRuleName(AgentQueryLexer.SelectedByExecutor))
					|| suggestedLiteral.equals(AgentQuery.getRuleName(AgentQueryLexer.NotUsedSince))
					|| suggestedLiteral.equals(AgentQuery.getRuleName(AgentQueryLexer.Paused))
					|| suggestedLiteral.equals(AgentQuery.getRuleName(AgentQueryLexer.RanBuild))) {
				return null;
			}
		}
		
		parseExpect = parseExpect.findExpectByLabel("operator");
		if (parseExpect != null) {
			List<Element> fieldElements = parseExpect.getState().findMatchedElementsByLabel("criteriaField", false);
			if (!fieldElements.isEmpty()) {
				String fieldName = AgentQuery.getValue(fieldElements.iterator().next().getMatchedText());
				try {
					AgentQuery.checkField(fieldName, AgentQuery.getOperator(suggestedLiteral));
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
			if ("criteriaValue".equals(spec.getLabel()) && AgentQuery.isInsideQuote(terminalExpect.getUnmatchedText())) {
				List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
				if (!fieldElements.isEmpty()) {
					String fieldName = AgentQuery.getValue(fieldElements.get(0).getMatchedText());
					if (fieldName.equals(Agent.NAME_NAME) 
							|| fieldName.equals(Agent.NAME_OS_NAME)
							|| fieldName.equals(Agent.NAME_OS_ARCH)
							|| fieldName.equals(Agent.NAME_OS_VERSION) 
							|| fieldName.equals(Agent.NAME_IP_ADDRESS)) {
						hints.add("Use '*' for wildcard match");
					}
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
