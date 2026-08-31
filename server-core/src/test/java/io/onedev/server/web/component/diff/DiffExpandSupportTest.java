package io.onedev.server.web.component.diff;

import static io.onedev.server.web.component.diff.DiffExpandSupport.Direction.BOTH;
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
		assertTrue(callback.canExpandUp);
		assertTrue(callback.canExpandDown);

		callback = new Callback();
		var lastContextSizes = contextSizes;
		contextSizes = support.expand(1, 100, 3, UP);
		support.appendEquals(new StringBuilder(), 1, lastContextSizes, contextSizes, block, 3, callback);
		assertEquals(range(3, 18).boxed().collect(toList()), callback.lines);
		assertEquals(79, callback.skippedLines);

		support.expand(1, 100, 3, UP);
		support.expand(1, 100, 3, UP);
		lastContextSizes = support.getContextSizes(1, 100, 3);
		contextSizes = support.expand(1, 100, 3, UP);
		callback = new Callback();
		support.appendEquals(new StringBuilder(), 1, lastContextSizes, contextSizes, block, 3, callback);
		assertEquals(List.of(48, 49), callback.lines);
		assertFalse(callback.canExpandUp);
		assertTrue(callback.canExpandDown);
		assertEquals(47, callback.skippedLines);

		lastContextSizes = contextSizes;
		contextSizes = support.expand(1, 100, 3, DOWN);
		callback = new Callback();
		support.appendEquals(new StringBuilder(), 1, lastContextSizes, contextSizes, block, 3, callback);
		assertEquals(range(82, 97).boxed().collect(toList()), callback.lines);
		assertFalse(callback.canExpandUp);
		assertTrue(callback.canExpandDown);
	}

	@Test
	public void shouldExpandBothSidesAndRemoveExpanderWhenSidesMeet() {
		var support = new DiffExpandSupport();
		var block = newBlock(40);
		support.expand(1, 40, 3, BOTH);
		var lastContextSizes = support.getContextSizes(1, 40, 3);
		var contextSizes = support.expand(1, 40, 3, BOTH);
		var callback = new Callback();

		support.appendEquals(new StringBuilder(), 1, lastContextSizes, contextSizes, block, 3, callback);
		assertEquals(range(18, 22).boxed().collect(toList()), callback.lines);
		assertEquals(-1, callback.skippedLines);
	}

	private static DiffBlock<String> newBlock(int size) {
		return new DiffBlock<>(Operation.EQUAL,
				range(0, size).mapToObj(String::valueOf).collect(toList()), 0, 0);
	}

	private static class Callback implements DiffExpandSupport.ExpandCallback {

		private final List<Integer> lines = new ArrayList<>();

		private int skippedLines = -1;

		private boolean canExpandUp;

		private boolean canExpandDown;

		@Override
		public void appendEqual(StringBuilder builder, DiffBlock<String> block, int lineIndex,
				int lastContextSize) {
			lines.add(lineIndex);
		}

		@Override
		public void appendExpander(StringBuilder builder, int blockIndex, int skippedLines,
				boolean canExpandUp, boolean canExpandDown) {
			this.skippedLines = skippedLines;
			this.canExpandUp = canExpandUp;
			this.canExpandDown = canExpandDown;
		}
	}
}
