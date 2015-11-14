package gitplex.product;

import java.io.IOException;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.pmease.gitplex.web.page.repository.commit.CommitQueryLexer;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		CommitQueryLexer lexer = new CommitQueryLexer(new ANTLRInputStream("branch()"));
		final CommonTokenStream tokens = new CommonTokenStream(lexer);
		CommitQueryParser parser = new CommitQueryParser(tokens);
		ParseTree tree = parser.query();
		System.out.println(tree);
	}
	
}