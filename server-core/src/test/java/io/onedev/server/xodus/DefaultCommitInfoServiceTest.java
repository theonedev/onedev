package io.onedev.server.xodus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import io.onedev.server.git.command.FileChange;
import io.onedev.server.util.patternset.PatternSet;

public class DefaultCommitInfoServiceTest {

	@Test
	public void shouldCountTextLinesAndIgnoreBinaryContent() throws Exception {
		assertEquals(0, countLines(""));
		assertEquals(1, countLines("first"));
		assertEquals(1, countLines("first\n"));
		assertEquals(2, countLines("first\nsecond"));
		assertEquals(-1, DefaultCommitInfoService.countLines(
				new ByteArrayInputStream(new byte[] {'a', 0, 'b'})));
	}

	@Test
	public void shouldApplyLineChangesToLatestStats() {
		var lineStats = new HashMap<String, Integer>();
		lineStats.put("Java", 10);
		lineStats.put("Python", 3);

		DefaultCommitInfoService.updateLineStats(lineStats, List.of(
				new FileChange(null, "src/Main.java", 5, 2),
				new FileChange(null, "src/old.py", 0, 3),
				new FileChange(null, "README.txt", 20, 0)), PatternSet.parse("**"));

		assertEquals(13, lineStats.get("Java").intValue());
		assertFalse(lineStats.containsKey("Python"));
		assertEquals(1, lineStats.size());
	}

	private int countLines(String content) throws Exception {
		return DefaultCommitInfoService.countLines(
				new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
	}

}
