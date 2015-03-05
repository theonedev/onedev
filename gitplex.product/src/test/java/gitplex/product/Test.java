package gitplex.product;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.pmease.commons.lang.TokenStream;
import com.pmease.commons.lang.TokenizedLine;
import com.pmease.commons.lang.tokenizers.clike.JavaTokenizer;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		JavaTokenizer java = new JavaTokenizer();		

		List<String> lines = FileUtils.readLines(new File("w:\\temp\\Component.java"));
		for (int i=0; i<100; i++) {
			long time = System.currentTimeMillis();
			List<TokenizedLine> tokenizedLines = java.tokenize(lines);
			TokenStream tokenStream = new TokenStream(tokenizedLines, false);
			System.out.println(tokenStream.nextBalanced(tokenStream.next("{")));
			System.out.println(System.currentTimeMillis()-time);
		}
	}
	
}
