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
	
	private final Map<Integer, Integer> contextSizes = new HashMap<>();
	
	/**
	 * Gets the current or default context size for a given block index.
	 */
	public int getContextSize(int blockIndex) {
		Integer contextSize = contextSizes.get(blockIndex);
		return contextSize != null ? contextSize : WebConstants.DIFF_CONTEXT_SIZE;
	}
	
	/**
	 * Expands the context for a given block index.
	 * @return the new context size after expansion
	 */
	public int expand(int blockIndex) {
		int lastContextSize = getContextSize(blockIndex);
		int newContextSize = lastContextSize + WebConstants.DIFF_EXPAND_SIZE;
		contextSizes.put(blockIndex, newContextSize);
		return newContextSize;
	}
	
	/**
	 * Appends equal lines with context and expanders.
	 * 
	 * @param builder the string builder to append to
	 * @param blockIndex the index of the current equal block
	 * @param lastContextSize the previous context size (0 for initial render)
	 * @param contextSize the current context size to render
	 * @param block the diff block containing equal lines
	 * @param totalBlocks total number of diff blocks
	 * @param callback callback to append individual lines and expanders
	 */
	public void appendEquals(StringBuilder builder, int blockIndex, int lastContextSize, 
			int contextSize, DiffBlock<String> block, int totalBlocks, ExpandCallback callback) {
		
		if (blockIndex == 0) {
			// First block: show last N context lines
			int start = block.getElements().size() - contextSize;
			if (start < 0)
				start = 0;
			else if (start > 0)
				callback.appendExpander(builder, blockIndex, start);
			for (int j = start; j < block.getElements().size() - lastContextSize; j++)
				callback.appendEqual(builder, block, j, lastContextSize);
		} else if (blockIndex == totalBlocks - 1) {
			// Last block: show first N context lines
			int end = block.getElements().size();
			int skipped = 0;
			if (end > contextSize) {
				skipped = end - contextSize;
				end = contextSize;
			}
			for (int j = lastContextSize; j < end; j++)
				callback.appendEqual(builder, block, j, lastContextSize);
			if (skipped != 0)
				callback.appendExpander(builder, blockIndex, skipped);
		} else if (2 * contextSize < block.getElements().size()) {
			// Middle block with large content: show N lines at start, expander, N lines at end
			for (int j = lastContextSize; j < contextSize; j++)
				callback.appendEqual(builder, block, j, lastContextSize);
			callback.appendExpander(builder, blockIndex, block.getElements().size() - 2 * contextSize);
			for (int j = block.getElements().size() - contextSize; j < block.getElements().size() - lastContextSize; j++)
				callback.appendEqual(builder, block, j, lastContextSize);
		} else {
			// Middle block with small content: show all newly expanded lines
			for (int j = lastContextSize; j < block.getElements().size() - lastContextSize; j++)
				callback.appendEqual(builder, block, j, lastContextSize);
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
		 */
		void appendExpander(StringBuilder builder, int blockIndex, int skippedLines);
	}
}

