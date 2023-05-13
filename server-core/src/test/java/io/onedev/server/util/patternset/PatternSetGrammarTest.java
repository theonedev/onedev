package io.onedev.server.util.patternset;

import com.google.common.collect.Sets;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PatternSetGrammarTest {

	@Test
	public void test() {
		PatternSet expected = new PatternSet(Sets.newHashSet("file1", "file2", "file4 with space", "-file5", "file7 with \""), 
				Sets.newHashSet("file3", "-file6")); 
		PatternSet actual = PatternSet.parse("file1 file2 -file3 \"file4 with space\" \"-file5\" -\"-file6\" \"file7 with \\\"\"");
		assertEquals(expected.getIncludes(), actual.getIncludes());
		assertEquals(expected.getExcludes(), actual.getExcludes());
	}

}
