package io.onedev.server.buildspec.job.log.instruction;

import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.TerminalNode;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.buildspec.job.log.instruction.LogInstructionParser.InstructionContext;
import io.onedev.server.model.Build;

@ExtensionPoint
public abstract class LogInstruction {

	public static final String PREFIX = "##onedev";
	
	public abstract String getName();
	
	public abstract void execute(Build build, Map<String, List<String>> params);
	
	public static String getValue(TerminalNode valueNode) {
		return StringUtils.unescape(FenceAware.unfence(valueNode.getText()));
	}
	
	public static InstructionContext parse(String instructionString) {
		CharStream is = CharStreams.fromString(instructionString); 
		LogInstructionLexer lexer = new LogInstructionLexer(is);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				throw new RuntimeException("Malformed log instruction");
			}
			
		});
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		LogInstructionParser parser = new LogInstructionParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		return parser.instruction();
	}
	
}
