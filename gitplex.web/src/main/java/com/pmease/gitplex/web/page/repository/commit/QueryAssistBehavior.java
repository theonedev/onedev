package com.pmease.gitplex.web.page.repository.commit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import com.pmease.commons.wicket.behavior.inputassist.ANTLRInputAssistBehavior;

@SuppressWarnings("serial")
public class QueryAssistBehavior extends ANTLRInputAssistBehavior {

	private static class InputMultiplier {
		
		private final String input;
		
		private final int caret;
		
		private final List<Token> tokensBeforeCaret = new ArrayList<>();
		
		private final String textToMatch;
		
		private final int replaceStart;
		
		private final int replaceEnd;
		
		InputMultiplier(@Nullable String input, int caret) {
			if (input == null)
				input = "";
			if (caret > input.length())
				caret = input.length();
			this.input = input;
			this.caret = caret;

			Token tokenAtCaret = null;
			Token tokenAfterCaret = null;
			CommitQueryLexer lexer = new CommitQueryLexer(new ANTLRInputStream(input));
			Token token = lexer.nextToken();
			while (token.getType() != Token.EOF) {
				if (caret>=token.getStartIndex() && caret<=token.getStopIndex()) {
					tokenAtCaret = token;
				} else if (caret > token.getStopIndex()) {
					tokensBeforeCaret.add(token);
				} else if (caret < token.getStartIndex()) {
					tokenAfterCaret = token;
					break;
				}
				token = lexer.nextToken();
			}
			
			Collections.reverse(tokensBeforeCaret);
			
			if (tokenAtCaret != null) {
				textToMatch = tokenAtCaret.getText().substring(0, caret-token.getStartIndex());
				replaceStart = tokenAtCaret.getStartIndex();
				replaceEnd = tokenAtCaret.getStopIndex()+1;
			} else {
				int start;
				if (!tokensBeforeCaret.isEmpty())
					start = tokensBeforeCaret.get(0).getStopIndex() + 1;
				else
					start = 0;
				while (start < input.length() && Character.isWhitespace(input.charAt(start)))
					start++;
				
				int end;
				if (tokenAfterCaret != null) {
					end = tokenAfterCaret.getStartIndex();
					while (end>=0 && Character.isWhitespace(input.charAt(end)))
						end--;
				} else {
					end = input.length();
				}
				
				if (start>caret) {
					textToMatch = "";
					replaceStart = caret;
				} else {
					textToMatch = input.substring(start, caret);
					replaceStart = start;
				}
				
				replaceEnd = Math.max(end, caret);
			}
			
		}

		InputMultiplier insertToken(String tokenText, int caretOffset) {
			String inputBeforeStart = input.substring(0, replaceStart);
			String inputAfterEnd = input.substring(replaceEnd);
			String newInput = inputBeforeStart + tokenText + inputAfterEnd;
			return new InputMultiplier(newInput, replaceStart + caretOffset);
		}

		List<InputMultiplier> multiply() {
			List<InputMultiplier> inputs = new ArrayList<>();
			return inputs;
		}
	}

	@Override
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
	
}
