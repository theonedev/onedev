package io.onedev.server.web.component.diff;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import io.onedev.server.util.diff.DiffBlock;
import io.onedev.server.web.WebConstants;

/**
 * Utility class to handle diff expansion logic that is common between
 * TextDiffPanel and BlobTextDiffPanel.
 */
public class DiffExpandSupport implements Serializable {
	
	private final Map<Integer, ContextSizes> contextSizes = new HashMap<>();

	/**
	 * Direction of the arrow clicked by user. DOWN continues downwards from the previous hunk, 
	 * that is, it grows the top context of the gap. UP continues upwards from the next hunk, 
	 * that is, it grows the bottom context of the gap.
	 */
	public enum Direction {
		DOWN, UP;

		public static Direction fromString(String value) {
			for (Direction direction : values()) {
				if (direction.name().equalsIgnoreCase(value))
					return direction;
			}
			return DOWN;
		}
	}

	public static class ContextSizes implements Serializable {

		private final int top;

		private final int bottom;

		public ContextSizes(int top, int bottom) {
			this.top = top;
			this.bottom = bottom;
		}

		public int getTop() {
			return top;
		}

		public int getBottom() {
			return bottom;
		}
	}
	
	/**
	 * Gets the current or default context size for a given block index.
	 */
	public ContextSizes getContextSizes(int blockIndex, int blockSize, int totalBlocks) {
		ContextSizes contextSizes = this.contextSizes.get(blockIndex);
		if (contextSizes != null)
			return contextSizes;
		if (blockIndex == 0)
			return new ContextSizes(0, Math.min(WebConstants.DIFF_CONTEXT_SIZE, blockSize));
		else if (blockIndex == totalBlocks - 1)
			return new ContextSizes(Math.min(WebConstants.DIFF_CONTEXT_SIZE, blockSize), 0);
		else
			return new ContextSizes(
					Math.min(WebConstants.DIFF_CONTEXT_SIZE, (blockSize + 1) / 2),
					Math.min(WebConstants.DIFF_CONTEXT_SIZE, blockSize / 2));
	}
	
	/**
	 * Expands the context for a given block index.
	 * @return the new context size after expansion
	 */
	public ContextSizes expand(int blockIndex, int blockSize, int totalBlocks, Direction direction) {
		ContextSizes lastContextSizes = getContextSizes(blockIndex, blockSize, totalBlocks);
		int top = lastContextSizes.getTop();
		int bottom = lastContextSizes.getBottom();
		if (blockIndex == 0) {
			bottom = Math.min(blockSize, bottom + WebConstants.DIFF_EXPAND_SIZE);
		} else if (blockIndex == totalBlocks - 1) {
			top = Math.min(blockSize, top + WebConstants.DIFF_EXPAND_SIZE);
		} else if (direction == Direction.DOWN) {
			top = Math.min(blockSize - bottom, top + WebConstants.DIFF_EXPAND_SIZE);
		} else {
			bottom = Math.min(blockSize - top, bottom + WebConstants.DIFF_EXPAND_SIZE);
		}
		ContextSizes newContextSizes = new ContextSizes(top, bottom);
		contextSizes.put(blockIndex, newContextSizes);
		return newContextSizes;
	}
	
	/**
	 * Appends equal lines with context and expanders.
	 * 
	 * @param builder the string builder to append to
	 * @param blockIndex the index of the current equal block
	 * @param lastContextSizes the previous top and bottom context sizes
	 * @param contextSizes the current top and bottom context sizes to render
	 * @param block the diff block containing equal lines
	 * @param totalBlocks total number of diff blocks
	 * @param callback callback to append individual lines and expanders
	 */
	public void appendEquals(StringBuilder builder, int blockIndex, ContextSizes lastContextSizes,
			ContextSizes contextSizes, DiffBlock<String> block, int totalBlocks, ExpandCallback callback) {
		
		if (blockIndex == 0) {
			// First block: show last N context lines
			int start = block.getElements().size() - contextSizes.getBottom();
			if (start < 0)
				start = 0;
			else if (start > 0)
				callback.appendExpander(builder, blockIndex, start, false, true);
			for (int j = start; j < block.getElements().size() - lastContextSizes.getBottom(); j++)
				callback.appendEqual(builder, block, j, lastContextSizes.getBottom());
		} else if (blockIndex == totalBlocks - 1) {
			// Last block: show first N context lines
			int end = block.getElements().size();
			int skipped = 0;
			if (end > contextSizes.getTop()) {
				skipped = end - contextSizes.getTop();
				end = contextSizes.getTop();
			}
			for (int j = lastContextSizes.getTop(); j < end; j++)
				callback.appendEqual(builder, block, j, lastContextSizes.getTop());
			if (skipped != 0)
				callback.appendExpander(builder, blockIndex, skipped, true, false);
		} else {
			// Middle block: top and bottom contexts grow independently until they meet
			for (int j = lastContextSizes.getTop(); j < contextSizes.getTop(); j++)
				callback.appendEqual(builder, block, j, lastContextSizes.getTop());
			int skipped = block.getElements().size() - contextSizes.getTop() - contextSizes.getBottom();
			if (skipped > 0)
				callback.appendExpander(builder, blockIndex, skipped, true, true);
			for (int j = block.getElements().size() - contextSizes.getBottom();
					j < block.getElements().size() - lastContextSizes.getBottom(); j++) {
				callback.appendEqual(builder, block, j, lastContextSizes.getBottom());
			}
		}
	}
	
	/**
	 * Callback interface for appending lines and expanders.
	 */
	public interface ExpandCallback {
		/**
		 * Appends an equal (unchanged) line.
		 * 
		 * @param builder the string builder
		 * @param block the diff block
		 * @param lineIndex the line index within the block
		 * @param lastContextSize the previous context size (0 means original, >0 means expanded)
		 */
		void appendEqual(StringBuilder builder, DiffBlock<String> block, int lineIndex, int lastContextSize);
		
		/**
		 * Appends an expander row.
		 * 
		 * @param builder the string builder
		 * @param blockIndex the block index
		 * @param skippedLines the number of skipped lines
		 * @param canExpandDown true to show a down arrow revealing more lines below the previous hunk
		 * @param canExpandUp true to show an up arrow revealing more lines above the next hunk
		 */
		void appendExpander(StringBuilder builder, int blockIndex, int skippedLines,
				boolean canExpandDown, boolean canExpandUp);
	}
}
