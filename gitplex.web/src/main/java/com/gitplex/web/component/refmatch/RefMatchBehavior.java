package com.gitplex.web.component.refmatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;

import com.gitplex.core.entity.Depot;
import com.gitplex.commons.antlr.codeassist.FenceAware;
import com.gitplex.commons.antlr.codeassist.InputSuggestion;
import com.gitplex.commons.antlr.codeassist.ParentedElement;
import com.gitplex.commons.antlr.grammar.ElementSpec;
import com.gitplex.commons.antlr.grammar.LexerRuleRefElementSpec;
import com.gitplex.commons.util.Range;
import com.gitplex.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;
import com.gitplex.core.util.includeexclude.IncludeExcludeParser;

@SuppressWarnings("serial")
public class RefMatchBehavior extends ANTLRAssistBehavior {

	private final IModel<Depot> depotModel;
	
	private static final String VALUE_OPEN = "(";
	
	private static final String VALUE_CLOSE = ")";
	
	private static final String ANY_BRANCH = "refs/heads/*";
	
	private static final String ANY_TAG = "refs/tags/*";
	
	public RefMatchBehavior(IModel<Depot> depotModel) {
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
						List<InputSuggestion> suggestions = new ArrayList<>();

						Depot depot = depotModel.getObject();

						String lowerCaseMatchWith = unfencedMatchWith.toLowerCase();
						int numSuggestions = 0;
						Collection<String> suggestedValues = new ArrayList<>();
						suggestedValues.add(ANY_BRANCH);
						suggestedValues.add(ANY_TAG);
						for (Ref ref: depot.getRefs(Constants.R_HEADS).values())
							suggestedValues.add(ref.getName());
						for (Ref ref: depot.getRefs(Constants.R_TAGS).values())
							suggestedValues.add(ref.getName());
						
						for (String suggestedValue: suggestedValues) {
							int index = suggestedValue.toLowerCase().indexOf(lowerCaseMatchWith);
							if (index != -1 && numSuggestions++<count) {
								Range matchRange = new Range(index, index+lowerCaseMatchWith.length());
								if (suggestedValue.equals(ANY_BRANCH)) {
									suggestions.add(new InputSuggestion(suggestedValue, "any branch", matchRange));
								} else if (suggestedValue.equals(ANY_TAG)) {
									suggestions.add(new InputSuggestion(suggestedValue, "any tag", matchRange));
								} else {
									suggestions.add(new InputSuggestion(suggestedValue, matchRange));
								}
							}
						}

						if (numSuggestions++<count 
								&& !unfencedMatchWith.contains(VALUE_CLOSE) 
								&& unfencedMatchWith.length() != 0) {
							String fenced = VALUE_OPEN + unfencedMatchWith + VALUE_CLOSE; 
							Range matchRange = new Range(0, fenced.length());
							suggestions.add(new InputSuggestion(fenced, -1, true, getFencingDescription(), matchRange));
						}
						return suggestions;
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
				hints.add("Use * to match any part of ref");
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
