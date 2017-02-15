package com.gitplex.server.util.includeexclude;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.gitplex.server.util.includeexclude.IncludeExcludeLexer;
import com.gitplex.server.util.includeexclude.IncludeExcludeParser;
import com.gitplex.server.util.includeexclude.IncludeExcludeParser.CriteriaContext;
import com.gitplex.server.util.includeexclude.IncludeExcludeParser.MatchContext;
import com.gitplex.server.util.match.IncludeExclude;

public class IncludeExcludeUtils {
	
	public static MatchContext parse(String match) {
		ANTLRInputStream is = new ANTLRInputStream(match); 
		IncludeExcludeLexer lexer = new IncludeExcludeLexer(is);
		lexer.removeErrorListeners();
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		IncludeExcludeParser parser = new IncludeExcludeParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		return parser.match();
	}
	
	public static String getValue(TerminalNode terminal) {
		String value = terminal.getText().substring(1);
		return value.substring(0, value.length()-1).trim();
	}
 	
	public static IncludeExclude<String, String> getIncludeExclude(String match) {
		MatchContext matchContext = parse(match);
		List<String> includes = new ArrayList<>();
		List<String> excludes = new ArrayList<>();
		for (CriteriaContext criteriaContext: matchContext.criteria()) {
			if (criteriaContext.includeMatch() != null) {
				includes.add(getValue(criteriaContext.includeMatch().Value()));
			} else if (criteriaContext.excludeMatch() != null) {
				excludes.add(getValue(criteriaContext.excludeMatch().Value()));
			}
		}

		return new IncludeExclude<String, String>(includes, excludes);
	}
	
}
