package gitplex.product;

import java.io.IOException;
import java.util.List;

import com.pmease.commons.antlr.codeassist.CodeAssist;
import com.pmease.commons.antlr.codeassist.InputStatus;
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

		List<InputSuggestion> suggestions = codeAssist.suggest(new InputStatus("a(b.c());"), "blockStatement");
		System.out.println(suggestions);
	}
	
}