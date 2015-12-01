package gitplex.product;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.pmease.commons.antlr.codeassist.CodeAssist;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;
import com.pmease.commons.antlr.codeassist.SpecMatch;
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

		String content = FileUtils.readFileToString(new File("w:\\temp\\Test.java"));
		SpecMatch match = codeAssist.getRule("blockStatement").match(codeAssist.lex(content), null, null, new HashMap<String, Integer>());
		System.out.println(match.isMatched());
	}
	
}