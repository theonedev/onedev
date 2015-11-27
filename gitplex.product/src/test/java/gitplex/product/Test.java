package gitplex.product;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Token;

import com.pmease.commons.antlr.codeassist.test.CodeAssistTest3Lexer;

public class Test {

	@org.junit.Test
	public void test() {
		CodeAssistTest3Lexer lexer = new CodeAssistTest3Lexer(new ANTLRInputStream("title: hello world just"));
		Token token = lexer.nextToken();
		while (token.getType() != Token.EOF) {
			System.out.println(token);
			token = lexer.nextToken();
		}
	}
	
}