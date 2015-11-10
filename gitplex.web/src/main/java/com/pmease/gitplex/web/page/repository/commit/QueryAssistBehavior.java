package com.pmease.gitplex.web.page.repository.commit;

import java.util.ArrayList;
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
		
		Token token = getLastToken(parser, 1);
		
		
		List<InputAssist> assists = new ArrayList<>();
		
		return assists;
	}
	
	private Token getLastToken(CommitQueryParser parser, int index) {
		TokenStream stream = parser.getTokenStream();
		if (index >= stream.size())
			return null;
		else
			return stream.get(stream.size() - index - 1);
	}

	@Override
	protected List<InputError> getErrors(String input) {
		List<InputError> errors = new ArrayList<>();
		CommitQueryParser parser = buildParser(input, new CollectErrorsLexerErrorListener(errors), 
				new CollectErrorsParserErrorListener(errors));
		parser.query();
		return errors;
	}
	
}
