package gitplex.product;

import com.pmease.commons.util.match.WildcardUtils;

public class Test {

	@org.junit.Test
	public void test() {
		System.out.println(WildcardUtils.rangeOfMatch("hehello  world", "he*llo"));
	}
	
}