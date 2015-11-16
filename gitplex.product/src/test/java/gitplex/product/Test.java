package gitplex.product;

import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import com.pmease.commons.antlr.ANTLRv4Lexer;
import com.pmease.commons.antlr.ANTLRv4Parser;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		InputStream is = ANTLRv4Parser.class.getResourceAsStream("ANTLRv4Lexer.g4");
		ANTLRv4Lexer lexer = new ANTLRv4Lexer(new ANTLRInputStream(is));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ANTLRv4Parser parser = new ANTLRv4Parser(tokens);
		System.out.println(parser.grammarSpec().rules().ruleSpec().size());
	}
	
}