package gitplex.product;

import java.io.File;
import java.io.IOException;

import com.pmease.commons.util.FileUtils;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		System.out.println("hello\fworld");
		FileUtils.writeFile(new File("w:\\temp\\formfeed"), "hello\fworld");
	}
	
}