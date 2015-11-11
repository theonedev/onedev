package com.pmease.commons.wicket.behavior.inputassist;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

@SuppressWarnings("serial")
public abstract class ANTLRInputAssistBehavior extends InputAssistBehavior {

	@Override
	protected List<InputAssist> getAssists(String input, final int caret) {
		final AtomicInteger replaceStart = new AtomicInteger(caret);
		final AtomicReference<ParserRuleContext> expectingRule = new AtomicReference<>(null);
		final List<Token> tokensBeforeCaret = new ArrayList<>();
		ParserRuleContext parseResult = parse(input.substring(0, caret), new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				if (caret == recognizer.getInputStream().index()+1
						|| caret == recognizer.getInputStream().index()) {
					replaceStart.set(charPositionInLine);
				}
			}
			
		}, new BaseErrorListener() {

			private int maxTokenIndex = -1;
			
			private int maxDepth = -1;

			private void record(Parser parser) {
				expectingRule.set(parser.getRuleContext());
				tokensBeforeCaret.clear();
				for (int i=parser.getTokenStream().size()-1; i>=0; i--) {
					Token token = parser.getTokenStream().get(i);
					if (token.getType() != Token.EOF)
						tokensBeforeCaret.add(token);
				}
			}
			
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				Parser parser = (Parser) recognizer;
				int tokenIndex = parser.getRuleContext().getStart().getTokenIndex();
				if (tokenIndex > maxTokenIndex) {
					maxTokenIndex = tokenIndex;
					record(parser);
				} else if (tokenIndex == maxTokenIndex && parser.getRuleContext().depth() > maxDepth) {
					maxDepth = parser.getRuleContext().depth();
					record(parser);
				}
			}
			
		});
		int replaceEnd = caret;
		if (expectingRule != null) { 
			for (int i=caret+1; i<input.length(); i++) {
				final AtomicBoolean endOfExpectingRule = new AtomicBoolean(false);
				parse(input.substring(0, i), new BaseErrorListener(), new BaseErrorListener() {

					@Override
					public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
							int charPositionInLine, String msg, RecognitionException e) {
						Parser parser = (Parser) recognizer;
						if (parser.getRuleContext().getStart().getTokenIndex() != expectingRule.get().getStart().getTokenIndex()
								|| parser.getRuleContext().depth() != expectingRule.get().depth()) { 
							endOfExpectingRule.set(true);
						}
					}
					
				});
				
			}
		}
		
		return new ArrayList<>();
	}

	@Override
	protected List<InputError> getErrors(String input) {
		final List<InputError> errors = new ArrayList<>();
		parse(input, new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				errors.add(new InputError(charPositionInLine, recognizer.getInputStream().index()+1));
			}
			
		}, new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				CommonToken token = (CommonToken) offendingSymbol;
				int end;
				if (token != null && token.getType() != Token.EOF)
					end = charPositionInLine + token.getText().length();
				else
					end = charPositionInLine + 1;
				errors.add(new InputError(charPositionInLine, end));
			}
			
		});
		return errors;
	}

	protected abstract ParserRuleContext parse(String input, 
			ANTLRErrorListener lexerErrorListener, ANTLRErrorListener parserErrorListener);
}
