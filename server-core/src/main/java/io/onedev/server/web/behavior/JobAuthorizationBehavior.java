package io.onedev.server.web.behavior;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.onedev.commons.codeassist.AntlrUtils;
import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.Element;
import io.onedev.commons.codeassist.parser.ParseExpect;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.job.authorization.JobAuthorization;
import io.onedev.server.job.authorization.JobAuthorizationLexer;
import io.onedev.server.job.authorization.JobAuthorizationParser;
import io.onedev.server.model.Build;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class JobAuthorizationBehavior extends ANTLRAssistBehavior {

	public JobAuthorizationBehavior() {
		super(JobAuthorizationParser.class, "jobAuthorization", false);
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
							List<String> fields = Lists.newArrayList(Build.NAME_PROJECT, Build.NAME_JOB);
							return SuggestionUtils.suggest(fields, matchWith);
						} else if ("criteriaValue".equals(spec.getLabel())) {
							List<Element> operatorElements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
							Preconditions.checkState(operatorElements.size() == 1);
							String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
							int operator = AntlrUtils.getLexerRule(JobAuthorizationLexer.ruleNames, operatorName);							
							if (operator == JobAuthorizationLexer.Is) {
								List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
								Preconditions.checkState(fieldElements.size() == 1);
								String fieldName = JobAuthorization.getValue(fieldElements.get(0).getMatchedText());
								if (fieldName.equals(Build.NAME_PROJECT)) {
									if (!matchWith.contains("*") && !matchWith.contains("?"))
										return SuggestionUtils.suggestProjectPaths(matchWith);
									else
										return null;
								}
							}
						} 
						return null;
					}
					
					@Override
					protected String getFencingDescription() {
						return "value should be quoted";
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
				String fieldName = JobAuthorization.getValue(fieldElements.iterator().next().getMatchedText());
				try {
					JobAuthorization.checkField(fieldName, AntlrUtils.getLexerRule(JobAuthorizationLexer.ruleNames, suggestedLiteral));
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
				hints.add("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>");
			}
		} 
		return hints;
	}
	
}
