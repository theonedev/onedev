package com.pmease.gitplex.web.page.repository.commit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;

import com.google.common.primitives.Chars;
import com.pmease.commons.lang.extractors.ExtractToken;
import com.pmease.commons.wicket.behavior.inputassist.InputAssist;
import com.pmease.commons.wicket.behavior.inputassist.InputAssistBehavior;
import com.pmease.commons.wicket.behavior.inputassist.InputError;
import com.pmease.commons.wicket.behavior.inputassist.antlr.CollectErrorsLexerErrorListener;
import com.pmease.commons.wicket.behavior.inputassist.antlr.CollectErrorsParserErrorListener;
import com.pmease.commons.wicket.behavior.inputassist.antlr.FindUnrecognizedLexerErrorListener;

@SuppressWarnings("serial")
public class QueryAssistBehavior extends InputAssistBehavior {

	private CommitQueryParser buildParser(String input, 
			@Nullable ANTLRErrorListener lexerErrorListener,
			@Nullable ANTLRErrorListener parserErrorListener) {
		CommitQueryLexer lexer = new CommitQueryLexer(new ANTLRInputStream(input));
		final CommonTokenStream tokens = new CommonTokenStream(lexer);
		CommitQueryParser parser = new CommitQueryParser(tokens);
		lexer.removeErrorListeners();
		parser.removeErrorListeners();
		if (lexerErrorListener != null)
			lexer.addErrorListener(lexerErrorListener);
		if (parserErrorListener != null)
			parser.addErrorListener(parserErrorListener);
		return parser;
	}

	@Override
	protected List<InputAssist> getAssists(String input, int caret) {
		String inputBeforeCaret;
		if (caret > input.length())
			inputBeforeCaret = input;
		else
			inputBeforeCaret = input.substring(0, caret);
		
		FindUnrecognizedLexerErrorListener lexerErrorListener = 
				new FindUnrecognizedLexerErrorListener(inputBeforeCaret);
		
		CommitQueryParser parser = buildParser(inputBeforeCaret, lexerErrorListener, null);
		parser.query();
		
		CommitQueryLexer lexer = new CommitQueryLexer(new ANTLRInputStream(input));
		List<Token> allTokens = new ArrayList<>();
		Token token = lexer.nextToken();
		while (token.getType() != Token.EOF) {
			allTokens.add(token);
			token = lexer.nextToken();
		}
		
		token = getLastToken(parser, 1);
		if (token != null) {
			
		} else {
			
		}
		
		List<InputAssist> assists = new ArrayList<>();
		
		return assists;
	}
	
	private Token getCaretToken(List<Token> tokens, int caret) {
		for (Token token: tokens) {
			if (caret>=token.getStartIndex() && caret<=token.getStopIndex())
				return token;
		}
		return null;
	}
	
	private Token getLastToken(CommitQueryParser parser, int index) {
		TokenStream stream = parser.getTokenStream();
		if (index >= stream.size())
			return null;
		else
			return stream.get(stream.size() - index - 1);
	}

	private List<InputAssist> getCriteriaAssists(String input, @Nullable String term) {
		List<InputAssist> assists = new ArrayList<>();
		if (term == null)
			term = "";
		
		return assists;
	}

	@Override
	protected List<InputError> getErrors(String input) {
		List<InputError> errors = new ArrayList<>();
		CommitQueryParser parser = buildParser(input, new CollectErrorsLexerErrorListener(errors), 
				new CollectErrorsParserErrorListener(errors));
		parser.query();
		return errors;
	}
	
	private static class InputInfo {
		
		private String input;
		
		private int caret;
		
		private List<Token> tokensBeforeCaret;
		
		private String textToMatch;
		
		private int replaceStart;
		
		private int replaceEnd;
		
		InputInfo(@Nullable String input, int caret) {
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

		InputInfo replaceToken(String tokenText, int caretOffset) {
			
		}
		
	}
}
