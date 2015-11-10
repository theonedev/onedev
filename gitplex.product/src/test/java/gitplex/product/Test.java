package gitplex.product;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Token;

import com.pmease.gitplex.web.page.repository.commit.CommitQueryLexer;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		CommitQueryLexer lexer = new CommitQueryLexer(new ANTLRInputStream("branch tag"));
		lexer.removeErrorListeners();
		List<Token> allTokens = new ArrayList<>();
		Token token = lexer.nextToken();
		while (token.getType() != Token.EOF) {
			System.out.println(token);
			allTokens.add(token);
			token = lexer.nextToken();
		}
		
	}
	
}