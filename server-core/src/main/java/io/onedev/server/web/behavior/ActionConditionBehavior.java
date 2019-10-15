package io.onedev.server.web.behavior;

import java.util.ArrayList;
import java.util.List;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.JobAware;
import io.onedev.server.ci.job.action.condition.ActionConditionParser;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.model.Project;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class ActionConditionBehavior extends ANTLRAssistBehavior {

	public ActionConditionBehavior() {
		super(ActionConditionParser.class, "condition", false);
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
							JobAware jobAware = getComponent().findParent(JobAware.class);
							Job job = jobAware.getJob();
							return SuggestionUtils.suggest(new ArrayList<>(job.getParamSpecMap().keySet()), matchWith);
						} else if ("criteriaValue".equals(spec.getLabel())) {
							List<String> branchNames = new ArrayList<>();
							for (RefInfo refInfo: Project.get().getBranches())
								branchNames.add(GitUtils.ref2branch(refInfo.getRef().getName()));
							return SuggestionUtils.suggest(branchNames, matchWith);
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
	protected List<String> getHints(TerminalExpect terminalExpect) {
		List<String> hints = new ArrayList<>();
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
			if ("criteriaValue".equals(spec.getLabel())) {
				String unmatched = terminalExpect.getUnmatchedText();
				if (unmatched.indexOf('"') == unmatched.lastIndexOf('"')) // only when we input criteria value
					hints.add("Use * for wildcard match");
			}
		} 
		return hints;
	}
	
}
