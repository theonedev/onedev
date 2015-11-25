package gitplex.product;

import com.google.common.base.Splitter;

public class Test {

	@org.junit.Test
	public void test() {
		System.out.println(Splitter.on('\n').splitToList("\n").size());
	}
	
}