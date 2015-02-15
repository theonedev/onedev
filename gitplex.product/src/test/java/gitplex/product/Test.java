package gitplex.product;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.tokenizer.JavascriptTokenizer;
import com.pmease.commons.util.FileUtils;

public class Test extends AbstractGitTest {

	@org.junit.Test
	public void test() throws IOException {
		List<String> lines = FileUtils.readLines(new File("W:\\commons\\commons.tokenizer\\src\\test\\java\\com\\pmease\\commons\\tokenizer\\testfiles\\test.js"));		
		for (int i=1; i<=100; i++) {
			long time = System.currentTimeMillis();
			new JavascriptTokenizer.JavaScript().tokenize(lines);
			System.out.println(System.currentTimeMillis()-time);
		}
	}
	
}
