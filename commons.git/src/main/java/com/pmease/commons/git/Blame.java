package com.pmease.commons.git;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Blame {
	
	private final BriefCommit commit;
	
	private final List<String> lines;
	
	public Blame(BriefCommit commit, List<String> lines) {
		this.commit = commit;
		this.lines = new ArrayList<>(lines);
	}

	public BriefCommit getCommit() {
		return commit;
	}
	
	public List<String> getLines() {
		return Collections.unmodifiableList(lines);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(commit);
		buffer.append("\n========================================================================\n");
		
		for (String line: getLines())
			buffer.append(line).append("\n");
		
		return buffer.toString();
	}
	
}
