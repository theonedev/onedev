package gitplex.product;

import java.io.IOException;
import java.util.BitSet;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRErrorStrategy;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import com.pmease.gitplex.product.CommitLexer;
import com.pmease.gitplex.product.CommitParser;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		ANTLRInputStream is = new ANTLRInputStream("c");
		CommitLexer lexer = new CommitLexer(is);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new ANTLRErrorListener() {
			
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
					String msg, RecognitionException e) {
				System.out.println("lexer syntax error: " + e);
			}
			
			@Override
			public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction,
					ATNConfigSet configs) {
			}
			
			@Override
			public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
					BitSet conflictingAlts, ATNConfigSet configs) {
			}
			
			@Override
			public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact,
					BitSet ambigAlts, ATNConfigSet configs) {
			}
			
		});
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		final CommitParser parser = new CommitParser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(new ANTLRErrorListener() {
			
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
					String msg, RecognitionException e) {
				System.out.println("paser syntax error: " + msg);
			}
			
			@Override
			public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction,
					ATNConfigSet configs) {
			}
			
			@Override
			public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
					BitSet conflictingAlts, ATNConfigSet configs) {
			}
			
			@Override
			public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact,
					BitSet ambigAlts, ATNConfigSet configs) {
			}
			
		});
		parser.setErrorHandler(new ANTLRErrorStrategy() {
			
			@Override
			public void sync(Parser recognizer) throws RecognitionException {
			}
			
			@Override
			public void reset(Parser recognizer) {
			}
			
			@Override
			public void reportMatch(Parser recognizer) {
			}
			
			@Override
			public void reportError(Parser recognizer, RecognitionException e) {
			}
			
			@Override
			public Token recoverInline(Parser recognizer) throws RecognitionException {
				return null;
			}
			
			@Override
			public void recover(Parser recognizer, RecognitionException e) throws RecognitionException {
			}
			
			@Override
			public boolean inErrorRecoveryMode(Parser recognizer) {
				return false;
			}
		});
		parser.query();
	}
}