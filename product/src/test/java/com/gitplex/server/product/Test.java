package com.gitplex.server.product;

import java.nio.file.Paths;

public class Test {

	@org.junit.Test
	public void test() {
		System.out.println(Paths.get("dir1/dir2/readme.md").relativize(Paths.get("dir1/dir3/dir4/1.png")));
	}

}