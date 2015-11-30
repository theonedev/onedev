package gitplex.product;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Token;

import com.pmease.gitplex.web.page.test.PostQueryLexer;

public class Test {

	@org.junit.Test
	public void test() {
		PostQueryLexer lexer = new PostQueryLexer(new ANTLRInputStream("hello world title:value"));
		Token token = lexer.nextToken();
		while (token.getType() != Token.EOF) {
			System.out.println(token);
			token = lexer.nextToken();
		}
	}
	
}