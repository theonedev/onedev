package gitplex.product;

import com.google.javascript.rhino.head.Context;
import com.pmease.commons.git.AbstractGitTest;

public class Test extends AbstractGitTest {

	@org.junit.Test
	public void test() {
		Context ctx = Context.enter();
		ctx.initStandardObjects();
		
	}
	
}
