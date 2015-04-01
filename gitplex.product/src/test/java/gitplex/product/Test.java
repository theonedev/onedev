package gitplex.product;

import java.io.IOException;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		System.out.println(Joiner.on(".").join(Lists.newArrayList()));
	}	

}