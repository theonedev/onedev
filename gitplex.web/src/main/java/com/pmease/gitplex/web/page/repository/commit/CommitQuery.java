package com.pmease.gitplex.web.page.repository.commit;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;

import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.QueryContext;

public class CommitQuery {
	
	private final String query;
	
	public CommitQuery(String query) {
		this.query = query;
	}
	
	public QueryContext parse() {
		ANTLRInputStream is = new ANTLRInputStream(query);
		CommitQueryLexer lexer = new CommitQueryLexer(is);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CommitQueryParser parser = new CommitQueryParser(tokens);
		parser.setErrorHandler(new BailErrorStrategy());
		return parser.query();
	}
	
}
