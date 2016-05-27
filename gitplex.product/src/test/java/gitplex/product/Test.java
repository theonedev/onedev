package gitplex.product;

import java.nio.file.Paths;

public class Test {

	@org.junit.Test
	public void test() {
		long time = System.currentTimeMillis();
		for (int i=0; i<1000000; i++)
		Paths.get("hello/world/just/do/it/and/I/will/help/you/to/get/it/done/" + i);
		System.out.println(System.currentTimeMillis()-time);
	}
	
}