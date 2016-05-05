package gitplex.product;

import com.pmease.commons.util.Range;
import com.pmease.commons.util.match.WildcardUtils;

public class Test {

	@org.junit.Test
	public void test() {
		Range range = WildcardUtils.rangeOfMatch("*hello*r", "hello world");
		System.out.println(range);
	}
	
}