package gitplex.product;

import java.io.IOException;

import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		String markdown = ""
				+ "* hello\n"
				+ "  * item1\n"
				+ "* world";
		System.out.println(new PegDownProcessor(Extensions.ALL_WITH_OPTIONALS).markdownToHtml(markdown));
	}
}