package com.gitplex.core.util.fullbranchmatch;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ValidationException;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.StringUtils;

import com.gitplex.core.entity.Depot;
import com.gitplex.core.entity.support.DepotAndBranch;
import com.gitplex.commons.util.match.IncludeExclude;
import com.gitplex.commons.util.match.Matcher;
import com.gitplex.commons.util.match.WildcardUtils;
import com.gitplex.core.util.fullbranchmatch.FullBranchMatchLexer;
import com.gitplex.core.util.fullbranchmatch.FullBranchMatchParser;
import com.gitplex.core.util.fullbranchmatch.FullBranchMatchParser.CriteriaContext;
import com.gitplex.core.util.fullbranchmatch.FullBranchMatchParser.FullBranchMatchContext;
import com.gitplex.core.util.fullbranchmatch.FullBranchMatchParser.MatchContext;

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

		Matcher<FullBranchMatchContext, DepotAndBranch> matcher = new Matcher<FullBranchMatchContext, DepotAndBranch>() {
			
			@Override
			public boolean matches(FullBranchMatchContext rule, DepotAndBranch value) {
				if (rule.fullDepotMatch() != null) {
					String fullDepotMatch = rule.fullDepotMatch().getText().trim();
					String branchMatch = rule.branchMatch().getText().trim();
					String accountMatch = StringUtils.substringBeforeLast(fullDepotMatch, Depot.FQN_SEPARATOR).trim();
					String depotMatch = StringUtils.substringAfterLast(fullDepotMatch, Depot.FQN_SEPARATOR).trim();
					return WildcardUtils.matchString(accountMatch, value.getDepot().getAccount().getName())
							&& WildcardUtils.matchString(depotMatch, value.getDepot().getName())
							&& WildcardUtils.matchString(branchMatch, value.getBranch());
				} else {
					String branchPattern = rule.branchMatch().Value().getText().trim();
					return value.getDepot().equals(currentDepot)
							&& WildcardUtils.matchString(branchPattern, value.getBranch());
				}
			}
			
		};
		return new IncludeExclude<FullBranchMatchContext, DepotAndBranch>(includes, excludes).matches(matcher, depotAndBranch);
	}

	public static void validate(String match) {
		for (CriteriaContext criteriaContext: parse(match).criteria()) {
			if (criteriaContext.includeMatch() != null) { 
				if (criteriaContext.includeMatch().fullBranchMatch().fullDepotMatch() != null) {
					String fullDepotMatch = criteriaContext.includeMatch().fullBranchMatch().fullDepotMatch().getText();
					if (fullDepotMatch.indexOf("/") == -1)
						throw new ValidationException("Depot match '" + fullDepotMatch + "' should contain '/'");
				}
			} else if (criteriaContext.excludeMatch() != null) {
				if (criteriaContext.excludeMatch().fullBranchMatch().fullDepotMatch() != null) {
					String fullDepotMatch = criteriaContext.excludeMatch().fullBranchMatch().fullDepotMatch().getText();
					if (fullDepotMatch.indexOf("/") == -1)
						throw new ValidationException("Depot match '" + fullDepotMatch + "' should contain '/'");
				}
			}
		}
	}
	
}
