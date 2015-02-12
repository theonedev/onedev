package gitplex.product;

import java.io.IOException;
import java.util.regex.Pattern;

import com.pmease.commons.git.AbstractGitTest;

public class Test extends AbstractGitTest {

	@org.junit.Test
	public void test() throws IOException {
		System.out.println(Pattern.compile("[^\\s\\w]").matcher("hello+world-+just").replaceAll("\\\\$0"));
	}
	
}
