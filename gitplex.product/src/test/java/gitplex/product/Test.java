package gitplex.product;

import java.io.IOException;
import java.util.UUID;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		for (int i=0; i<10; i++)
			System.out.println(UUID.randomUUID().toString());
	}
	
}