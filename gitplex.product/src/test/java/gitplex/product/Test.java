package gitplex.product;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.Token;

import com.pmease.commons.antlr.Grammar;
import com.pmease.commons.antlr.grammar.RuleSpec;
import com.pmease.commons.antlr.parser.EarleyParser;
import com.pmease.commons.lang.extractors.java.JavaLexer;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		Grammar grammar = new Grammar(JavaLexer.class);
		List<Token> tokens = grammar.lex("1");
		RuleSpec rule = grammar.getRule("compilationUnit");
		EarleyParser parser = new EarleyParser(rule, tokens);
		System.out.println(parser.matches());
	}
	
}