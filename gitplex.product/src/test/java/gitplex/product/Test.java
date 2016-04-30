package gitplex.product;

import java.util.concurrent.SynchronousQueue;

public class Test {

	@org.junit.Test
	public void test() throws InterruptedException {
		SynchronousQueue<String> queue = new SynchronousQueue<>();
		queue.take();
	}
}