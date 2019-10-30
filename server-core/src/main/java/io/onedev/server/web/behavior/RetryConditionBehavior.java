package io.onedev.server.web.behavior;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import io.onedev.commons.codeassist.AntlrUtils;
import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.Element;
import io.onedev.commons.codeassist.parser.ParseExpect;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.server.OneException;
import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.JobAware;
import io.onedev.server.ci.job.retrycondition.RetryCondition;
import io.onedev.server.ci.job.retrycondition.RetryConditionLexer;
import io.onedev.server.ci.job.retrycondition.RetryConditionParser;
import io.onedev.server.util.BuildConstants;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class RetryConditionBehavior extends ANTLRAssistBehavior {

	public RetryConditionBehavior() {
		super(RetryConditionParser.class, "condition", false);
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
							List<String> fields = Lists.newArrayList(
									BuildConstants.FIELD_LOG, 
									BuildConstants.FIELD_ERROR_MESSAGE);
							JobAware jobAware = getComponent().findParent(JobAware.class);
							Job job = jobAware.getJob();
							fields.addAll(job.getParamSpecMap().keySet());
							return SuggestionUtils.suggest(fields, matchWith);
						} else {
							return null;
						}
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
				JobAware jobAware = getComponent().findParent(JobAware.class);
				Job job = jobAware.getJob();
				String fieldName = RetryCondition.getValue(fieldElements.iterator().next().getMatchedText());
				try {
					RetryCondition.checkField(job, fieldName, 
							AntlrUtils.getLexerRule(RetryConditionLexer.ruleNames, suggestedLiteral));
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
				if (unmatched.indexOf('"') == unmatched.lastIndexOf('"')) // only when we input criteria value
					hints.add("A <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java pattern</a> is expected here");
			}
		} 
		return hints;
	}
	
}
