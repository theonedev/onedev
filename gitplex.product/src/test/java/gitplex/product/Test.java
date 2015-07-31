package gitplex.product;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.pmease.commons.lang.tokenizers.CmToken;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		List<List<CmToken>> lines = new ArrayList<>();
		lines.add(Lists.newArrayList(new CmToken("comment", "hello"), new CmToken("comment", "world")));
		lines.add(Lists.newArrayList(new CmToken("keyword", "for"), new CmToken("keyword", "while")));
		System.out.println(lines);
	}
	
}