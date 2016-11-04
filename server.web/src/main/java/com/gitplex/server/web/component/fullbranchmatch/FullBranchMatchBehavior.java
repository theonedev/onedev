package com.gitplex.server.web.component.fullbranchmatch;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.gitplex.commons.antlr.codeassist.InputSuggestion;
import com.gitplex.commons.antlr.codeassist.ParentedElement;
import com.gitplex.commons.antlr.grammar.LexerRuleRefElementSpec;
import com.gitplex.commons.antlr.parser.Element;
import com.gitplex.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;
import com.gitplex.server.core.util.fullbranchmatch.FullBranchMatchParser;
import com.gitplex.server.core.GitPlex;
import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.manager.DepotManager;
import com.gitplex.server.web.page.depot.DepotPage;
import com.gitplex.server.web.util.SuggestionUtils;

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
						branchDepot = depotManager.find(depot.getAccount(), depotFQN);
					} else {
						branchDepot = depotManager.find(depotFQN);
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
