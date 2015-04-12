package gitplex.product;

import java.io.IOException;

public class Test {

	public void print(String s) {
		System.out.println(s);
	}
	
	@org.junit.Test
	public void test() throws IOException {
		System.out.println((int)'\"');
	}	

}