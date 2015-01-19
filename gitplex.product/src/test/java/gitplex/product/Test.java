package gitplex.product;

import java.io.File;

public class Test {

	@org.junit.Test
	public void test() {
		int size = 0;
		for (int i=1; i<10000; i++) {
			boolean b = new File("w:\\temp\\avatars\\" + i).exists();
			size += (b?1:0);
		}
		System.out.println(size);
	}

}
