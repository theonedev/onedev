package com.gitplex.server.product;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import com.gitplex.server.rest.RestModule;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		Enumeration<URL> resources = RestModule.class.getClassLoader().getResources("com/gitplex/server/rest");
		while (resources.hasMoreElements()) {
			System.out.println(resources.nextElement());
		}
	}

}