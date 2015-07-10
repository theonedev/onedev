package gitplex.product;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		System.out.println(StringUtils.substringBefore("hello", "."));
	}
	
}