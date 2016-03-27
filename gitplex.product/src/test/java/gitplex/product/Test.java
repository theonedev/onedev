package gitplex.product;

import java.util.List;

import jersey.repackaged.com.google.common.collect.Lists;

public class Test {

	@org.junit.Test
	public void test() {
		List<String> lists = Lists.newArrayList("a", "c", "b");
		lists.sort(String::compareTo);
		System.out.println(lists);
	}
	
}