package io.onedev.server.web.behavior;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.onedev.commons.codeassist.AntlrUtils;
import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.Element;
import io.onedev.commons.codeassist.parser.ParseExpect;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.job.match.JobMatch;
import io.onedev.server.job.match.JobMatchParser;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static io.onedev.commons.codeassist.AntlrUtils.getLexerRule;
import static io.onedev.server.job.match.JobMatchLexer.*;

@SuppressWarnings("serial")
public class JobMatchBehavior extends ANTLRAssistBehavior {

	private final boolean withProjectCriteria;
	
	private final boolean withJobCriteria;

	public JobMatchBehavior(boolean withProjectCriteria, boolean withJobCriteria) {
		super(JobMatchParser.class, "jobMatch", false, false);
		this.withProjectCriteria = withProjectCriteria;
		this.withJobCriteria = withJobCriteria;
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
							List<String> fields = new ArrayList<>();
							if (withProjectCriteria)							
								fields.add(Build.NAME_PROJECT);
							if (withJobCriteria)
								fields.add(Build.NAME_JOB);
							return SuggestionUtils.suggest(fields, matchWith);
						} else if ("criteriaValue".equals(spec.getLabel())) {
							List<Element> operatorElements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
							Preconditions.checkState(operatorElements.size() == 1);
							String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
							int operator = getLexerRule(ruleNames, operatorName);							
							if (operator == Is) {
								List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
								Preconditions.checkState(fieldElements.size() == 1);
								String fieldName = JobMatch.getValue(fieldElements.get(0).getMatchedText());
								if (fieldName.equals(Build.NAME_PROJECT)) {
									if (!matchWith.contains("*") && !matchWith.contains("?"))
										return SuggestionUtils.suggestProjectPaths(matchWith);
								} else if (fieldName.equals(Build.NAME_JOB) && Project.get() != null) {
									return SuggestionUtils.suggestJobs(Project.get(), matchWith);
								}
							} else if (operator == OnBranch) {
								if (Project.get() != null)
									return SuggestionUtils.suggestBranches(Project.get(), matchWith);
							} else if (operator == SubmittedByUser) {
								return SuggestionUtils.suggestUsers(matchWith);
							} else if (operator == SubmittedByGroup) {
								return SuggestionUtils.suggestGroups(matchWith);
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
				String fieldName = JobMatch.getValue(fieldElements.iterator().next().getMatchedText());
				try {
					JobMatch.checkField(fieldName, withProjectCriteria, withJobCriteria);
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
					if (fieldName.equals(Build.NAME_PROJECT))
						hints.add("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>");
					else if (fieldName.equals(Build.NAME_JOB))
						hints.add("Use '*' or '?' for wildcard match");						
				} else {
					List<Element> operatorElements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
					Preconditions.checkState(operatorElements.size() == 1);
					String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
					int operator = AntlrUtils.getLexerRule(ruleNames, operatorName);
					if (operator == OnBranch)
						hints.add("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>");
				}
			}
		} 
		return hints;
	}
	
}
