package io.onedev.server.product;

import java.net.MalformedURLException;
import java.net.URL;

public class Test {

	@org.junit.Test
	public void test() throws MalformedURLException {
		System.out.println(new URL("ssh://localhost:6611").getHost());
	}

}