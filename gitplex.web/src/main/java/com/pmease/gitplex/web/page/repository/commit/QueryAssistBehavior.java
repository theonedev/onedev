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
	protected List<InputAssist> getInsertions(ParserRuleContext ruleBeforeCaret,
			List<Token> tokensBeforeCaret, String textToMatch) {
		List<InputAssist> insertions = new ArrayList<>();
		
		if (ruleBeforeCaret.getRuleIndex() == CommitQueryParser.RULE_query && ruleBeforeCaret.getStop() != null) { 
			// previous input is a valid query
			suggestCriterias(insertions, textToMatch);
		}
		
		return insertions;
	}
	
	private void suggestCriteria(List<InputAssist> insertions, String criteriaName, String textToMatch) {
		if (criteriaName.startsWith(textToMatch))
			insertions.add(new InputAssist(criteriaName + "()", criteriaName.length()+1));
	}

	private void suggestRevisionCriterias(List<InputAssist> insertions, String textToMatch) {
		suggestCriteria(insertions, "branch", textToMatch);
		suggestCriteria(insertions, "tag", textToMatch);
		suggestCriteria(insertions, "id", textToMatch);
	}
	
	private void suggestCriterias(List<InputAssist> insertions, String textToMatch) {
		suggestRevisionCriterias(insertions, textToMatch);
		suggestCriteria(insertions, "path", textToMatch);
		suggestCriteria(insertions, "before", textToMatch);
		suggestCriteria(insertions, "after", textToMatch);
		suggestCriteria(insertions, "author", textToMatch);
		suggestCriteria(insertions, "committer", textToMatch);
		suggestCriteria(insertions, "message", textToMatch);
		
	}
	
}
