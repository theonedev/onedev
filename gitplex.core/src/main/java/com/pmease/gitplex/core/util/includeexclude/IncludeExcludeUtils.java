package com.pmease.gitplex.core.util.includeexclude;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitplex.core.util.includeexclude.IncludeExcludeParser.CriteriaContext;
import com.pmease.gitplex.core.util.includeexclude.IncludeExcludeParser.MatchContext;

public class IncludeExcludeUtils {
	
	public static MatchContext parse(String refMatch) {
		ANTLRInputStream is = new ANTLRInputStream(refMatch); 
		IncludeExcludeLexer lexer = new IncludeExcludeLexer(is);
		lexer.removeErrorListeners();
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		IncludeExcludeParser parser = new IncludeExcludeParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		return parser.match();
	}
	
	private static String getValue(TerminalNode terminal) {
		String value = terminal.getText().substring(1);
		value = value.substring(0, value.length()-1).trim();
		if (value.endsWith("/") && !value.endsWith("*")) {
			value = value + "*";
		}
		return value;
	}
	
	public static boolean matches(String pathMatch, String path) {
		MatchContext matchContext = parse(pathMatch);
		List<String> includes = new ArrayList<>();
		List<String> excludes = new ArrayList<>();
		for (CriteriaContext criteriaContext: matchContext.criteria()) {
			if (criteriaContext.includeMatch() != null) {
				includes.add(getValue(criteriaContext.includeMatch().Value()));
			} else if (criteriaContext.excludeMatch() != null) {
				excludes.add(getValue(criteriaContext.excludeMatch().Value()));
			}
		}

		return WildcardUtils.matchString(includes, excludes, path);
	}
	
}
