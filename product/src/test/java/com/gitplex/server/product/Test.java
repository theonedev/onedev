package com.gitplex.server.product;

import java.nio.file.Paths;

public class Test {

	@org.junit.Test
	public void test() {
		System.out.println(Paths.get("/hello/world").resolveSibling("../just").normalize());
	}

}