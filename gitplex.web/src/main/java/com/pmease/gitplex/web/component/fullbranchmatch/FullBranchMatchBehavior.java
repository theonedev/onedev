package com.pmease.gitplex.web.component.fullbranchmatch;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.ParentedElement;
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
		if (expectedElement.getState() != null) {
			String ruleName = expectedElement.getState().getRuleSpec().getName();
			if (ruleName.equals("branchMatch")) {
				DepotPage page = (DepotPage) getComponent().getPage();
				Depot depot = page.getDepot();
				ParentedElement criteriaElement = expectedElement.findParentByRule("criteria");
				List<Element> children = criteriaElement.findChildrenByRule("fullDepotMatch", true);
				if (children.isEmpty()) {
					return SuggestionUtils.suggestBranch(depot, matchWith, count, 
							"branch in current repository", "any branch in current repository");
				} else {
					String depotFQN = children.get(0).getMatchedText().trim();
					Depot branchDepot;
					DepotManager depotManager = GitPlex.getInstance(DepotManager.class);
					if (depotFQN.indexOf("/") == -1) {
						branchDepot = depotManager.findBy(depot.getUser(), depotFQN);
					} else {
						branchDepot = depotManager.findBy(depotFQN);
					}
					if (branchDepot != null) {
						return SuggestionUtils.suggestBranch(branchDepot, matchWith, count, 
								"branch in selected repository", "any branch in selected repository");
					} else {
						return new ArrayList<>();
					}
				}
			} else if (ruleName.equals("fullDepotMatch")) {
				List<InputSuggestion> suggestions = new ArrayList<>();
				DepotPage page = (DepotPage) getComponent().getPage();
				Depot depot = page.getDepot();
				suggestions.addAll(SuggestionUtils.suggestAffinals(depot, matchWith, count, 
						"select branch in this repository", "any repository"));
				return suggestions;
			} 
		} 
		return null;
	}
	
	@Override
	protected List<String> getHints(ParentedElement expectedElement, String matchWith) {
		List<String> hints = new ArrayList<>();
		if (expectedElement.getSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) expectedElement.getSpec();
			if (spec.getRuleName().equals("Value")) {
				hints.add("Use * to do wildcard match");
			}
		} 
		return hints;
	}

}
