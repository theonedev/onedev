package com.pmease.gitplex.core.util.refmatch;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.pmease.commons.git.GitUtils;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitplex.core.util.refmatch.RefMatchParser.CriteriaContext;
import com.pmease.gitplex.core.util.refmatch.RefMatchParser.MatchContext;

public class RefMatchUtils {
	
	public static MatchContext parse(String refMatch) {
		ANTLRInputStream is = new ANTLRInputStream(refMatch); 
		RefMatchLexer lexer = new RefMatchLexer(is);
		lexer.removeErrorListeners();
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		RefMatchParser parser = new RefMatchParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		return parser.match();
	}
	
	private static String getValue(TerminalNode terminal) {
		String value = terminal.getText().substring(1);
		return value.substring(0, value.length()-1).trim();
	}
	
	public static boolean matches(String refMatch, String refName) {
		MatchContext matchContext = parse(refMatch);
		List<String> includes = new ArrayList<>();
		List<String> excludes = new ArrayList<>();
		for (CriteriaContext criteriaContext: matchContext.criteria()) {
			if (criteriaContext.branchMatch() != null) {
				includes.add(GitUtils.branch2ref(getValue(criteriaContext.branchMatch().Value())));
			} else if (criteriaContext.tagMatch() != null) {
				includes.add(GitUtils.tag2ref(getValue(criteriaContext.tagMatch().Value())));
			} else if (criteriaContext.patternMatch() != null) {
				includes.add(getValue(criteriaContext.patternMatch().Value()));
			} else if (criteriaContext.excludeBranchMatch() != null) {
				excludes.add(GitUtils.branch2ref(getValue(criteriaContext.excludeBranchMatch().Value())));
			} else if (criteriaContext.excludeTagMatch() != null) {
				excludes.add(GitUtils.tag2ref(getValue(criteriaContext.excludeTagMatch().Value())));
			} else if (criteriaContext.excludePatternMatch() != null) {
				excludes.add(getValue(criteriaContext.excludeTagMatch().Value()));
			}
		}

		return WildcardUtils.matchString(includes, excludes, refName);
	}
	
}
