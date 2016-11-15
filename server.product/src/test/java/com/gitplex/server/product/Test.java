package com.gitplex.server.product;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		Matcher matcher = Pattern.compile("\\d+\\.\\d+").matcher("1.0-EAP");
		matcher.find();
		System.out.println(matcher.group());
	}

}