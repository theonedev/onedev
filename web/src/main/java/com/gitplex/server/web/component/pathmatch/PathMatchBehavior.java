package com.gitplex.server.web.component.pathmatch;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.gitplex.codeassist.FenceAware;
import com.gitplex.codeassist.InputSuggestion;
import com.gitplex.codeassist.ParentedElement;
import com.gitplex.codeassist.grammar.ElementSpec;
import com.gitplex.codeassist.grammar.LexerRuleRefElementSpec;
import com.gitplex.server.util.includeexclude.IncludeExcludeParser;
import com.gitplex.server.entity.Depot;
import com.gitplex.server.web.behavior.inputassist.ANTLRAssistBehavior;
import com.gitplex.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class PathMatchBehavior extends ANTLRAssistBehavior {

	private final IModel<Depot> depotModel;
	
	private static final String VALUE_OPEN = "(";
	
	private static final String VALUE_CLOSE = ")";
	
	public PathMatchBehavior(IModel<Depot> depotModel) {
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
						return SuggestionUtils.suggestPath(depotModel.getObject(), unfencedMatchWith, count);
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
				hints.add("Use * to match any part of path");
				hints.add("Folder (path ending with '/') matches all files under it");
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
