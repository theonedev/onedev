package gitplex.product;

import java.io.IOException;
import java.util.List;

import com.pmease.commons.antlr.codeassist.CodeAssist;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;
import com.pmease.commons.lang.extractors.java.JavaLexer;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		CodeAssist codeAssist = new CodeAssist(JavaLexer.class) {

			private static final long serialVersionUID = 1L;

			@Override
			protected List<InputSuggestion> suggest(ParseTree parseTree, Node elementNode, String matchWith) {
				return null;
			}
			
		};
		System.out.println(codeAssist.getRule("statement").matches("a(b.c());"));
		
		long time = System.currentTimeMillis();
		for (int i=0; i<100; i++) {
			codeAssist.getRule("statement").matches("a(b.c());");
		}
		System.out.println(System.currentTimeMillis()-time);
	}
	
}