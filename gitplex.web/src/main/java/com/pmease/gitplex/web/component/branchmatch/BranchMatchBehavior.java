package com.pmease.gitplex.web.component.branchmatch;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.pmease.commons.antlr.codeassist.FenceAware;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.ParentedElement;
import com.pmease.commons.antlr.grammar.ElementSpec;
import com.pmease.commons.antlr.grammar.LexerRuleRefElementSpec;
import com.pmease.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.util.includeexclude.IncludeExcludeParser;
import com.pmease.gitplex.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class BranchMatchBehavior extends ANTLRAssistBehavior {

	private final IModel<Depot> depotModel;
	
	private static final String VALUE_OPEN = "(";
	
	private static final String VALUE_CLOSE = ")";
	
	public BranchMatchBehavior(IModel<Depot> depotModel) {
		super(IncludeExcludeParser.class, "match");
		this.depotModel = depotModel;
	}

	@Override
	public void detach(Component component) {
		super.detach(component);
		depotModel.detach();
	}

	@Override
	protected List<InputSuggestion> suggest(final ParentedElement expectedElement, String matchWith, int count) {
		if (expectedElement.getSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) expectedElement.getSpec();
			if (spec.getRuleName().equals("Value")) {
				return new FenceAware(codeAssist.getGrammar(), VALUE_OPEN, VALUE_CLOSE) {

					@Override
					protected List<InputSuggestion> match(String unfencedMatchWith, int count) {
						return SuggestionUtils.suggestBranch(depotModel.getObject(), unfencedMatchWith, count, 
								null, "any branch");
					}

					@Override
					protected String getFencingDescription() {
						return "value needs to be enclosed in brackets";
					}
					
				}.suggest(expectedElement.getSpec(), matchWith, count);
			}
		} 
		return null;
	}
	
	@Override
	protected List<String> getHints(ParentedElement expectedElement, String matchWith) {
		List<String> hints = new ArrayList<>();
		if (expectedElement.getSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) expectedElement.getSpec();
			if (spec.getRuleName().equals("Value") && !matchWith.contains(VALUE_CLOSE)) {
				hints.add("Use * to match any part of branch");
			}
		} 
		return hints;
	}

	@Override
	protected int getEndOfMatch(ElementSpec spec, String content) {
		if (content.startsWith(VALUE_OPEN+VALUE_CLOSE))
			return 2;
		else
			return super.getEndOfMatch(spec, content);
	}

}
