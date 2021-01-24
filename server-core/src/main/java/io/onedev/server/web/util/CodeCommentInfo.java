package io.onedev.server.web.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.model.CodeComment;

public class CodeCommentInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final long id;
	
	private final PlanarRange range;
	
	private final boolean updated;
	
	public CodeCommentInfo(CodeComment comment, PlanarRange range) {
		id = comment.getId();
		updated = !comment.isVisitedAfter(comment.getLastUpdate().getDate());
		this.range = range;
	}

	public long getId() {
		return id;
	}

	public PlanarRange getRange() {
		return range;
	}

	public boolean isUpdated() {
		return updated;
	}
	
	public static Map<Integer, List<CodeCommentInfo>> groupByLine(Map<CodeComment, PlanarRange> comments) {
		Map<Integer, List<CodeCommentInfo>> commentsByLine = new HashMap<>();
		
		for (Map.Entry<CodeComment, PlanarRange> entry: comments.entrySet()) {
			CodeComment comment = entry.getKey();
			PlanarRange position = entry.getValue();
			int line = position.getFromRow();
			List<CodeCommentInfo> commentsAtLine = commentsByLine.get(line);
			if (commentsAtLine == null) {
				commentsAtLine = new ArrayList<>();
				commentsByLine.put(line, commentsAtLine);
			}
			CodeCommentInfo commentInfo = new CodeCommentInfo(comment, position);
			commentsAtLine.add(commentInfo);
		}
		for (List<CodeCommentInfo> value: commentsByLine.values()) {
			value.sort((o1, o2)->(int)(o1.getId()-o2.getId()));
		}
		return commentsByLine;
	}
	
}