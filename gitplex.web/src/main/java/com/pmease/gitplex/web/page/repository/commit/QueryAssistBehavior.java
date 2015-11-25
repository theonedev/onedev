package com.pmease.gitplex.web.page.repository.commit;

import java.util.List;

import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;
import com.pmease.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;

@SuppressWarnings("serial")
public class QueryAssistBehavior extends ANTLRAssistBehavior {

	public QueryAssistBehavior() {
		super(CommitQueryParser.class, "query");
	}

	@Override
	protected List<InputSuggestion> suggest(ParseTree parseTree, Node elementNode, String matchWith) {
		return null;
	}

}
