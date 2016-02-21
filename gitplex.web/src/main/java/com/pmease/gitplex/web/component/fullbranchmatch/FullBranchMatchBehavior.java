package com.pmease.gitplex.web.component.fullbranchmatch;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.pmease.commons.antlr.codeassist.FenceAware;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.ParentedElement;
import com.pmease.commons.antlr.grammar.ElementSpec;
import com.pmease.commons.antlr.grammar.LexerRuleRefElementSpec;
import com.pmease.commons.antlr.parser.Element;
import com.pmease.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.util.fullbranchmatch.FullBranchMatchParser;
import com.pmease.gitplex.web.page.depot.DepotPage;
import com.pmease.gitplex.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class FullBranchMatchBehavior extends ANTLRAssistBehavior {

	private final IModel<Depot> depotModel;
	
	private static final String VALUE_OPEN = "(";
	
	private static final String VALUE_CLOSE = ")";
	
	public FullBranchMatchBehavior(IModel<Depot> depotModel) {
		super(FullBranchMatchParser.class, "match");
		this.depotModel = depotModel;
	}

	@Override
	public void detach(Component component) {
		super.detach(component);
		depotModel.detach();
	}

	@Override
	protected List<InputSuggestion> suggest(final ParentedElement expectedElement, String matchWith, final int count) {
		if (expectedElement.getSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) expectedElement.getSpec();
			if (spec.getRuleName().equals("Value")) {
				return new FenceAware(codeAssist.getGrammar(), VALUE_OPEN, VALUE_CLOSE) {

					@Override
					protected List<InputSuggestion> match(String unfencedMatchWith) {
						int tokenType = expectedElement.getRoot().getLastMatchedToken().getType();
						DepotPage page = (DepotPage) getComponent().getPage();
						Depot depot = page.getDepot();
						if (tokenType == FullBranchMatchParser.DEPOT) {
							return SuggestionUtils.suggestAffinals(depot, unfencedMatchWith, count);
						} else {
							ParentedElement criteriaElement = expectedElement.findParentByRule("criteria");
							List<Element> depotFQNElements = criteriaElement.findChildrenByLabel("depotFQN", true);
							if (!depotFQNElements.isEmpty()) {
								Element depotFQNElement = depotFQNElements.get(0);
								String branchDepotFQN = depotFQNElement.getMatchedText().trim().substring(1);
								branchDepotFQN = branchDepotFQN.substring(0, branchDepotFQN.length()-1);
								Depot branchDepot = GitPlex.getInstance(DepotManager.class).findBy(branchDepotFQN);
								if (branchDepot != null) {
									return SuggestionUtils.suggestBranch(branchDepot, unfencedMatchWith, count);
								} else {
									return new ArrayList<>();
								}
							} else {
								return SuggestionUtils.suggestBranch(depot, unfencedMatchWith, count);
							}
						}
					}

					@Override
					protected String getFencingDescription() {
						return "value needs to be enclosed in brackets";
					}
					
				}.suggest(expectedElement.getSpec(), matchWith);
			}
		} 
		return null;
	}
	
	@Override
	protected InputSuggestion wrapAsSuggestion(ParentedElement expectedElement, 
			String suggestedLiteral, boolean complete) {
		String description;
		switch (suggestedLiteral) {
		case "repository": 
			description = "specify repository of source branches";
			break;
		case "branch":
			ParentedElement criteriaElement = expectedElement.findParentByRule("criteria");
			if (!criteriaElement.findChildrenByRule("depotMatch", true).isEmpty()) {
				description = "";
			} else {
				description = "";
			}
			break;
		default:
			description = null;
		}
		return new InputSuggestion(suggestedLiteral, description, null);
	}
	
	@Override
	protected List<String> getHints(ParentedElement expectedElement, String matchWith) {
		List<String> hints = new ArrayList<>();
		if (expectedElement.getSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) expectedElement.getSpec();
			if (spec.getRuleName().equals("Value") && !matchWith.contains(VALUE_CLOSE)) {
				hints.add("Use * to do wildcard match");
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
