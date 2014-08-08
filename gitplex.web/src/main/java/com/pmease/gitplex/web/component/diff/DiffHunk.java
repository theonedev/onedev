package com.pmease.gitplex.web.component.diff;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.util.diff.DiffChunk;
import com.pmease.commons.util.diff.DiffLine;

@SuppressWarnings("serial")
class DiffHunk implements Serializable {

	private HunkHeader header;
	
	private List<HunkLine> lines;
	
	public DiffHunk(HunkHeader hunkHeader, List<HunkLine> lines) {
		this.header = hunkHeader;
		this.lines = lines;
	}

	public HunkHeader getHeader() {
		return header;
	}

	public List<HunkLine> getLines() {
		return lines;
	}
	
	public static DiffHunk from(DiffChunk chunk) {
		List<HunkLine> hunkLines = new ArrayList<>();
		int oldLineNo = chunk.getStart1(), newLineNo = chunk.getStart2();
		for (DiffLine diffLine: chunk.getDiffLines()) {
			if (diffLine.getAction() == DiffLine.Action.ADD)
				hunkLines.add(new HunkLine(0, newLineNo++, diffLine));
			else if (diffLine.getAction() == DiffLine.Action.DELETE)
				hunkLines.add(new HunkLine(oldLineNo++, 0, diffLine));
			else 
				hunkLines.add(new HunkLine(oldLineNo++, newLineNo++, diffLine));
		}
		return new DiffHunk(new HunkHeader(chunk.getStart1(), oldLineNo, chunk.getStart2(), newLineNo), hunkLines);
	}
	
}
