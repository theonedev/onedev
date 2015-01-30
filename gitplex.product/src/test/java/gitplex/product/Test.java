package gitplex.product;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

	@org.junit.Test
	public void test() {
		Pattern pattern = Pattern.compile("(?<=(^|\\s+))\\:([^\\s\\:]+)\\:(?=($|\\s+))");
		Matcher matcher = pattern.matcher(":abc: :b:");
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			String name = matcher.group(2);
			matcher.appendReplacement(buffer, "*" + name + "*");
		}
		matcher.appendTail(buffer);
		System.out.println(buffer);
	}
	
}
