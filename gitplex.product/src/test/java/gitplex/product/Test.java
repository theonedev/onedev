package gitplex.product;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.Token;

import com.pmease.commons.antlr.codeassist.CodeAssist;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;
import com.pmease.commons.antlr.codeassist.RuleSpec;
import com.pmease.commons.antlr.codeassist.parse.EarleyParser;
import com.pmease.commons.lang.extractors.java.JavaLexer;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		CodeAssist assist = new CodeAssist(JavaLexer.class) {

			private static final long serialVersionUID = 1L;

			@Override
			protected List<InputSuggestion> suggest(ParseTree parseTree, Node elementNode, String matchWith) {
				return null;
			}
			
		};
		List<Token> tokens = assist.lex("1");
		RuleSpec rule = assist.getRule("compilationUnit");
		EarleyParser parser = new EarleyParser(rule, tokens);
		System.out.println(parser.getMatches().size());
	}
	
}