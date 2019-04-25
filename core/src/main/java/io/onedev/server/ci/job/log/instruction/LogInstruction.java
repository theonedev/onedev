package io.onedev.server.ci.job.log.instruction;

import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.server.ci.job.log.instruction.LogInstructionParser.InstructionContext;
import io.onedev.server.exception.OneException;
import io.onedev.server.model.Build;

@ExtensionPoint
public abstract class LogInstruction {

	public static final String PREFIX = "##onedev";
	
	public static final String ESCAPE_CHARS = "\\\"";
	
	public abstract String getName();
	
	public abstract void execute(Build build, Map<String, List<String>> params);
	
	public static String unescape(String value) {
		return value.replace("\\'", "'").replace("\\\\", "\\");
	}

	public static String removeQuotes(String value) {
		if (value.startsWith("'"))
			value = value.substring(1);
		if (value.endsWith("'"))
			value = value.substring(0, value.length()-1);
		return value;
	}
	
	public static InstructionContext parse(String instructionString) {
		CharStream is = CharStreams.fromString(instructionString); 
		LogInstructionLexer lexer = new LogInstructionLexer(is);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				throw new OneException("Malformed log instruction");
			}
			
		});
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		LogInstructionParser parser = new LogInstructionParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		return parser.instruction();
	}

	public static String escape(String value) {
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<value.length(); i++) {
			char ch = value.charAt(i);
			if (ESCAPE_CHARS.indexOf(ch) != -1)
				builder.append("\\");
			builder.append(ch);
		}
		return builder.toString();
	}	
	
}
