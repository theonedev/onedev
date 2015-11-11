package com.pmease.gitplex.web.page.repository.commit;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import com.pmease.commons.wicket.behavior.inputassist.ANTLRAssistBehavior;
import com.pmease.commons.wicket.behavior.inputassist.InputAssist;

@SuppressWarnings("serial")
public class QueryAssistBehavior extends ANTLRAssistBehavior {

	protected ParserRuleContext parse(String input, ANTLRErrorListener lexerErrorListener,
			ANTLRErrorListener parserErrorListener) {
		CommitQueryLexer lexer = new CommitQueryLexer(new ANTLRInputStream(input));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CommitQueryParser parser = new CommitQueryParser(tokens);
		lexer.removeErrorListeners();
		parser.removeErrorListeners();
		lexer.addErrorListener(lexerErrorListener);
		parser.addErrorListener(parserErrorListener);
		return parser.query();
	}

	@Override
	protected List<InputAssist> getAssists(String input, int caret, ParserRuleContext ruleBeforeCaret,
			List<Token> tokensBeforeCaret, int replaceStart, int replaceEnd) {
		System.out.println(replaceEnd);
		return new ArrayList<>();
	}
	
}
