package gitplex.product;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.pmease.commons.lang.tokenizers.Token;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		List<List<Token>> lines = new ArrayList<>();
		lines.add(Lists.newArrayList(new Token("comment", "hello"), new Token("comment", "world")));
		lines.add(Lists.newArrayList(new Token("keyword", "for"), new Token("keyword", "while")));
		System.out.println(lines);
	}
	
}