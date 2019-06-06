package io.onedev.server.web.behavior.inputassist;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;

import com.google.common.base.Optional;

import io.onedev.commons.codeassist.AntlrUtils;
import io.onedev.commons.codeassist.CodeAssist;
import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputStatus;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.parser.ParseExpect;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.StringUtils;

@SuppressWarnings("serial")
public abstract class ANTLRAssistBehavior extends InputAssistBehavior {

	protected final Class<? extends Lexer> lexerClass;
	
	protected final Class<? extends Parser> parserClass;
	
	private transient Constructor<? extends Lexer> lexerCtor;
	
	private transient Constructor<? extends Parser> parserCtor;
	
	private transient Method ruleMethod;
	
	protected final CodeAssist codeAssist;
	
	protected final String ruleName;
	
	public ANTLRAssistBehavior(Class<? extends Parser> parserClass, String ruleName, boolean findAllPaths) {
		this(parserClass, AntlrUtils.getLexerClass(parserClass), ruleName, findAllPaths);
	}
	
	public ANTLRAssistBehavior(Class<? extends Parser> parserClass, 
			Class<? extends Lexer> lexerClass, String ruleName, boolean findAllPaths) {
		this(parserClass, lexerClass, 
				new String[]{AntlrUtils.getDefaultGrammarFile(lexerClass)}, 
				AntlrUtils.getDefaultTokenFile(lexerClass), ruleName, findAllPaths);
	}
	
	public ANTLRAssistBehavior(Class<? extends Parser> parserClass, Class<? extends Lexer> lexerClass, 
			String grammarFiles[], String tokenFile, String ruleName, boolean findAllPaths) {
		this.lexerClass = lexerClass;
		this.parserClass = parserClass;
		
		codeAssist = new CodeAssist(lexerClass, grammarFiles, tokenFile, findAllPaths) {

			@Override
			protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
				return ANTLRAssistBehavior.this.suggest(terminalExpect);
			}

			@Override
			protected List<String> getHints(TerminalExpect terminalExpect) {
				return ANTLRAssistBehavior.this.getHints(terminalExpect);
			}

			@Override
			protected Optional<String> describe(TerminalExpect terminalExpect, String suggestedLiteral) {
				return ANTLRAssistBehavior.this.describe(terminalExpect, suggestedLiteral);
			}

		};
		this.ruleName = ruleName;
	}
	
	protected void validate(String value) {
	}
	
	@Override
	protected List<InputCompletion> getSuggestions(InputStatus inputStatus) {
		return codeAssist.suggest(inputStatus, ruleName);
	}
	
	private Constructor<? extends Lexer> getLexerCtor() {
		if (lexerCtor == null) {
			try {
				lexerCtor = lexerClass.getConstructor(CharStream.class);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
			
		}
		return lexerCtor;
	}

	private Constructor<? extends Parser> getParserCtor() {
		if (parserCtor == null) {
			try {
				parserCtor = parserClass.getConstructor(TokenStream.class);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
			
		}
		return parserCtor;
	}
	
	private Method getRuleMethod() {
		if (ruleMethod == null) {
			try {
				ruleMethod = parserClass.getDeclaredMethod(ruleName);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		return ruleMethod;
	}
	
	@Override
	protected List<LinearRange> getErrors(final String inputContent) {
		final List<LinearRange> errors = new ArrayList<>();
		
		Lexer lexer;
		try {
			lexer = getLexerCtor().newInstance(CharStreams.fromString(inputContent));
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				int charIndex = getCharIndex(inputContent, line-1, charPositionInLine);
				errors.add(new LinearRange(charIndex, recognizer.getInputStream().index()));
			}
			
		});
		
		Parser parser;
		try {
			parser = getParserCtor().newInstance(new CommonTokenStream(lexer));
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		parser.removeErrorListeners();
		parser.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				int fromIndex = getCharIndex(inputContent, line-1, charPositionInLine);
				CommonToken token = (CommonToken) offendingSymbol;
				int toIndex;
				if (token != null && token.getType() != Token.EOF)
					toIndex = fromIndex + token.getText().length() - 1;
				else
					toIndex = fromIndex;
				errors.add(new LinearRange(fromIndex, toIndex));
			}
			
		});
		try {
			getRuleMethod().invoke(parser);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return errors;
	}
	
	@Override
	protected int getAnchor(String content) {
		List<Token> tokens = codeAssist.getGrammar().lex(content);
		if (tokens.isEmpty()) {
			return 0;
		} else {
			Token lastToken = tokens.get(tokens.size()-1);
			String contentAfterLastToken = content.substring(lastToken.getStopIndex()+1);
			if (contentAfterLastToken.length() > 0) {
				contentAfterLastToken = StringUtils.trimStart(contentAfterLastToken);
				return content.length() - contentAfterLastToken.length();
			} else {
				return lastToken.getStartIndex();
			}
		}
	}

	protected abstract List<InputSuggestion> suggest(TerminalExpect terminalExpect);
	
	@Override
	protected final List<String> getHints(InputStatus inputStatus) {
		return codeAssist.getHints(inputStatus, ruleName);
	}

	protected List<String> getHints(TerminalExpect terminalExpect) {
		return new ArrayList<>();
	}
	
	/**
	 * Describe suggested literal
	 * 
	 * @param expectedElement
	 * 			element of the literal
	 * @param suggestedLiteral
	 * 			suggested literal
	 * @return
	 * 			an optional containing description of the literal, or <tt>null</tt> to suppress the suggestion
	 */
	@Nullable
	protected Optional<String> describe(ParseExpect parseExpect, String suggestedLiteral) {
		if (StringUtils.isNotBlank(suggestedLiteral)) 
			return Optional.absent();
		else 
			return Optional.of("space");
	}
	
}
