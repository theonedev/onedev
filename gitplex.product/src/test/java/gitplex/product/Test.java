package gitplex.product;

import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import com.pmease.commons.antlr.codeassist.CodeAssist;
import com.pmease.commons.antlr.codeassist.InputStatus;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;
import com.pmease.commons.lang.extractors.java.JavaLexer;
import com.pmease.commons.lang.extractors.java.JavaParser;

public class Test {

	@org.junit.Test
	public void test() {
		JavaLexer lexer = new JavaLexer(new ANTLRInputStream("hello"));
		lexer.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				System.out.println("lexer: " + msg);
			}
			
		});
		JavaParser parser = new JavaParser(new CommonTokenStream(lexer));
		parser.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				System.out.println("parser: " + msg);
			}
			
		});
		parser.compilationUnit();
	}
	
}