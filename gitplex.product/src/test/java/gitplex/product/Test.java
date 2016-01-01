package gitplex.product;

import java.io.IOException;
import java.nio.file.Paths;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		System.out.println(Paths.get("hello/").toString());
	}
	
}