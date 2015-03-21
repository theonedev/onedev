package gitplex.product;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import com.pmease.commons.lang.tokenizers.clike.CTokenizer;
import com.pmease.commons.util.FileUtils;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		Collection<File> files = FileUtils.listFiles(new File("w:\\linux"), "**/*.c");
		long time = System.currentTimeMillis();
		int i=0;
		for (File file: files) {
			String content = FileUtils.readFileToString(file);
//			new LangStream(new CLexer(new ANTLRInputStream(content)), TokenFilter.DEFAULT_CHANNEL);
			new CTokenizer().tokenize(content);
			i++;
			if (i % 1000 == 0)
				System.out.println(i);
		}
		System.out.println((System.currentTimeMillis()-time)*1.0/files.size());
	}	
	
}