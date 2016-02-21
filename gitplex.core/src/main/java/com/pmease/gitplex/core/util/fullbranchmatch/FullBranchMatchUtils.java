package com.pmease.gitplex.core.util.fullbranchmatch;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.pmease.commons.util.match.IncludeExclude;
import com.pmease.commons.util.match.RuleMatcher;
import com.pmease.commons.util.match.WildcardUtils;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.DepotAndBranch;
import com.pmease.gitplex.core.util.fullbranchmatch.FullBranchMatchParser.CriteriaContext;
import com.pmease.gitplex.core.util.fullbranchmatch.FullBranchMatchParser.FullBranchMatchContext;
import com.pmease.gitplex.core.util.fullbranchmatch.FullBranchMatchParser.MatchContext;

public class FullBranchMatchUtils {
	
	public static MatchContext parse(String match) {
		ANTLRInputStream is = new ANTLRInputStream(match); 
		FullBranchMatchLexer lexer = new FullBranchMatchLexer(is);
		lexer.removeErrorListeners();
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		FullBranchMatchParser parser = new FullBranchMatchParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		return parser.match();
	}
	
	private static String getValue(TerminalNode terminal) {
		String value = terminal.getText().substring(1);
		return value.substring(0, value.length()-1).trim();
	}
	
	public static boolean matches(String match, final Depot currentDepot, DepotAndBranch depotAndBranch) {
		MatchContext matchContext = parse(match);
		List<FullBranchMatchContext> includes = new ArrayList<>();
		List<FullBranchMatchContext> excludes = new ArrayList<>();
		for (CriteriaContext criteriaContext: matchContext.criteria()) {
			if (criteriaContext.includeMatch() != null) {
				includes.add(criteriaContext.includeMatch().fullBranchMatch());
			} else if (criteriaContext.excludeMatch() != null) {
				excludes.add(criteriaContext.excludeMatch().fullBranchMatch());
			}
		}

		RuleMatcher<FullBranchMatchContext, DepotAndBranch> matcher = new RuleMatcher<FullBranchMatchContext, DepotAndBranch>() {
			
			@Override
			public boolean matches(FullBranchMatchContext rule, DepotAndBranch value) {
				if (rule.accountMatch() != null) {
					String accountPattern = getValue(rule.accountMatch().Value());
					String depotPattern = getValue(rule.depotMatch().Value());
					String branchPattern = getValue(rule.branchMatch().Value());
					return WildcardUtils.matchString(accountPattern, value.getDepot().getUser().getName())
							&& WildcardUtils.matchString(depotPattern, value.getDepot().getName())
							&& WildcardUtils.matchString(branchPattern, value.getBranch());
				} else if (rule.depotMatch() != null) {
					String depotPattern = getValue(rule.depotMatch().Value());
					String branchPattern = getValue(rule.branchMatch().Value());
					return value.getDepot().getUser().equals(currentDepot.getUser())
							&& WildcardUtils.matchString(depotPattern, value.getDepot().getName())
							&& WildcardUtils.matchString(branchPattern, value.getBranch());
				} else {
					String branchPattern = getValue(rule.branchMatch().Value());
					return value.getDepot().equals(currentDepot)
							&& WildcardUtils.matchString(branchPattern, value.getBranch());
				}
			}
			
		};
		return new IncludeExclude<FullBranchMatchContext, DepotAndBranch>(includes, excludes).matches(matcher, depotAndBranch);
	}
	
}
