package io.onedev.server.web.component.diff;

import static io.onedev.server.web.component.diff.DiffExpandSupport.Direction.DOWN;
import static io.onedev.server.web.component.diff.DiffExpandSupport.Direction.UP;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import io.onedev.server.util.diff.DiffBlock;
import io.onedev.server.util.diff.DiffMatchPatch.Operation;

public class DiffExpandSupportTest {

	@Test
	public void shouldExpandMiddleBlockIndependently() {
		var support = new DiffExpandSupport();
		var block = newBlock(100);
		var callback = new Callback();
		var contextSizes = support.getContextSizes(1, 100, 3);

		support.appendEquals(new StringBuilder(), 1, new DiffExpandSupport.ContextSizes(0, 0),
				contextSizes, block, 3, callback);
		assertEquals(List.of(0, 1, 2, 97, 98, 99), callback.lines);
		assertEquals(94, callback.skippedLines);
		assertTrue(callback.canExpandDown);
		assertTrue(callback.canExpandUp);

		callback = new Callback();
		var lastContextSizes = contextSizes;
		contextSizes = support.expand(1, 100, 3, DOWN);
		support.appendEquals(new StringBuilder(), 1, lastContextSizes, contextSizes, block, 3, callback);
		assertEquals(range(3, 33).boxed().collect(toList()), callback.lines);
		assertEquals(64, callback.skippedLines);

		// Expanding down keeps both controls available even past the block midpoint
		lastContextSizes = contextSizes;
		contextSizes = support.expand(1, 100, 3, DOWN);
		callback = new Callback();
		support.appendEquals(new StringBuilder(), 1, lastContextSizes, contextSizes, block, 3, callback);
		assertEquals(range(33, 63).boxed().collect(toList()), callback.lines);
		assertEquals(34, callback.skippedLines);
		assertTrue(callback.canExpandDown);
		assertTrue(callback.canExpandUp);

		lastContextSizes = contextSizes;
		contextSizes = support.expand(1, 100, 3, UP);
		callback = new Callback();
		support.appendEquals(new StringBuilder(), 1, lastContextSizes, contextSizes, block, 3, callback);
		assertEquals(range(67, 97).boxed().collect(toList()), callback.lines);
		assertEquals(4, callback.skippedLines);
		assertTrue(callback.canExpandDown);
		assertTrue(callback.canExpandUp);
	}

	@Test
	public void shouldExposeUpControlForFirstBlockAndDownControlForLastBlock() {
		var support = new DiffExpandSupport();

		var first = new Callback();
		support.appendEquals(new StringBuilder(), 0, new DiffExpandSupport.ContextSizes(0, 0),
				support.getContextSizes(0, 40, 3), newBlock(40), 3, first);
		assertFalse(first.canExpandDown);
		assertTrue(first.canExpandUp);

		var last = new Callback();
		support.appendEquals(new StringBuilder(), 2, new DiffExpandSupport.ContextSizes(0, 0),
				support.getContextSizes(2, 40, 3), newBlock(40), 3, last);
		assertTrue(last.canExpandDown);
		assertFalse(last.canExpandUp);
	}

	@Test
	public void shouldRemoveExpanderWhenSidesMeet() {
		var support = new DiffExpandSupport();
		var block = newBlock(40);
		support.expand(1, 40, 3, DOWN);
		var lastContextSizes = support.getContextSizes(1, 40, 3);
		var contextSizes = support.expand(1, 40, 3, DOWN);
		var callback = new Callback();

		support.appendEquals(new StringBuilder(), 1, lastContextSizes, contextSizes, block, 3, callback);
		assertEquals(range(33, 37).boxed().collect(toList()), callback.lines);
		assertEquals(-1, callback.skippedLines);
	}

	private static DiffBlock<String> newBlock(int size) {
		return new DiffBlock<>(Operation.EQUAL,
				range(0, size).mapToObj(String::valueOf).collect(toList()), 0, 0);
	}

	private static class Callback implements DiffExpandSupport.ExpandCallback {

		private final List<Integer> lines = new ArrayList<>();

		private int skippedLines = -1;

		private boolean canExpandDown;

		private boolean canExpandUp;

		@Override
		public void appendEqual(StringBuilder builder, DiffBlock<String> block, int lineIndex,
				int lastContextSize) {
			lines.add(lineIndex);
		}

		@Override
		public void appendExpander(StringBuilder builder, int blockIndex, int skippedLines,
				boolean canExpandDown, boolean canExpandUp) {
			this.skippedLines = skippedLines;
			this.canExpandDown = canExpandDown;
			this.canExpandUp = canExpandUp;
		}
	}
}
