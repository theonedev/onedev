package com.pmease.gitplex.web.page.repository.commit.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;

import com.pmease.commons.git.command.LogCommand;

public class CommitQuery implements Serializable {

	private static final long serialVersionUID = 1L;

	private final List<QueryCriteria> criterias = new ArrayList<>();
	
	public void applyTo(LogCommand logCommand) {
		for (QueryCriteria criteria: criterias)
			criteria.applyTo(logCommand);
	}
	
	public static CommitQuery of(@Nullable String query) {
		if (StringUtils.isNotBlank(query)) {
			ANTLRInputStream is = new ANTLRInputStream(query);
			CommitQueryLexer lexer = new CommitQueryLexer(is);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			CommitQueryParser parser = new CommitQueryParser(tokens);
			ParseTree tree = parser.query();
			return new CommitQueryBuilder().visit(tree);
		} else {
			return new CommitQuery();
		}
	}
}
