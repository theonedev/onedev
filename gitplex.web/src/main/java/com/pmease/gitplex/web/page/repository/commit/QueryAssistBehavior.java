package com.pmease.gitplex.web.page.repository.commit;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import com.pmease.commons.wicket.behavior.inputassist.InputAssist;
import com.pmease.commons.wicket.behavior.inputassist.InputAssistBehavior;
import com.pmease.commons.wicket.behavior.inputassist.InputError;

@SuppressWarnings("serial")
public class QueryAssistBehavior extends InputAssistBehavior {

	@Override
	protected List<InputError> getErrors(String input) {
		ANTLRInputStream is = new ANTLRInputStream(input); 
		CommitQueryLexer lexer = new CommitQueryLexer(is);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CommitQueryParser parser = new CommitQueryParser(tokens);
		lexer.removeErrorListeners();
		parser.removeErrorListeners();

		LexerErrorListener lexerErrorListener = new LexerErrorListener();
		lexer.addErrorListener(lexerErrorListener);
		
		ParserErrorListener parserErrorListener = new ParserErrorListener();
		parser.addErrorListener(parserErrorListener);
		parser.query();

		List<InputError> errors = new ArrayList<>();
		errors.addAll(lexerErrorListener.errors);
		errors.addAll(parserErrorListener.errors);
		
		return errors;
	}

	@Nullable
	private String getUnrecognizedText(@Nullable String message) {
		String startWith = "token recognition error at: '";
		if (message != null && message.startsWith(startWith) && message.endsWith("'")) {
			message = message.substring(startWith.length());
			return message.substring(0, message.length() - 1);
		} else {
			return null;
		}
	}
	
	private class LexerErrorListener extends BaseErrorListener {

		private List<InputError> errors = new ArrayList<>();
		
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
				String msg, RecognitionException e) {
			String unrecognizedText = getUnrecognizedText(msg);
			if (unrecognizedText != null) 
				errors.add(new InputError(charPositionInLine, charPositionInLine + unrecognizedText.length()));
			else 
				errors.add(new InputError(charPositionInLine, charPositionInLine+1));
		}
		
	}
	
	private class ParserErrorListener extends BaseErrorListener {

		private List<InputError> errors = new ArrayList<>();
		
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
				String msg, RecognitionException e) {
			CommonToken token = (CommonToken) offendingSymbol;
			int end;
			if (token != null && token.getType() != Token.EOF)
				end = charPositionInLine + token.getText().length();
			else
				end = charPositionInLine + 1;
			errors.add(new InputError(charPositionInLine, end));
		}
		
	}

	@Override
	protected List<InputAssist> getAssists(String input, int caret) {
		final List<InputAssist> assists = new ArrayList<>();
		
		String inputBeforeCaret;
		if (caret > input.length())
			inputBeforeCaret = input;
		else
			inputBeforeCaret = input.substring(0, caret);

		ANTLRInputStream is = new ANTLRInputStream(inputBeforeCaret); 
		CommitQueryLexer lexer = new CommitQueryLexer(is);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CommitQueryParser parser = new CommitQueryParser(tokens);
		lexer.removeErrorListeners();
		parser.removeErrorListeners();

		System.out.println("********************************");
		lexer.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				System.out.println("lexer :: symbol: " + offendingSymbol + ", msg: " + msg + ", exception: " + e);
			}
			
		});
		parser.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				Parser parser = (Parser) recognizer;
				System.out.println("expected tokens: " + parser.getExpectedTokens());
				System.out.println("parser :: symbol: " + offendingSymbol + ", msg: " + msg + ", exception: " + e);
			}
			
		});
		
		parser.query();

		return assists;
	}
	
}
