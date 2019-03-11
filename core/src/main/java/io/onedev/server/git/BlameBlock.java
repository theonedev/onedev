package io.onedev.server.git;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.onedev.commons.utils.Range;

public class BlameBlock {
	
	private final BlameCommit commit;

	private final List<Range> ranges;
	
	public BlameBlock(BlameCommit commit, List<Range> ranges) {
		this.commit = commit;
		this.ranges = ranges;
	}

	public BlameCommit getCommit() {
		return commit;
	}
	
	/**
	 * Get ranges of this blame block
	 * 
	 * @return
	 * 			list of ranges. Range is 0-indexed and inclusive for from and to
	 */
	public List<Range> getRanges() {
		return ranges;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(commit.getHash()).append(": ");
		for (Range range: ranges) 
			builder.append(range).append(", ");
		
		return StringUtils.stripEnd(builder.toString(), ", ");
	}

}
