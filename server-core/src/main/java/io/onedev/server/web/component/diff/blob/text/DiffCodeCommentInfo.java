package io.onedev.server.web.component.diff.blob.text;

import java.io.Serializable;

import javax.annotation.Nullable;

import io.onedev.server.model.CodeComment;
import io.onedev.server.web.util.DiffPlanarRange;

public class DiffCodeCommentInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final long id;
	
	@Nullable
	private final DiffPlanarRange range;
	
	private final boolean updated;
	
	public DiffCodeCommentInfo(CodeComment comment, @Nullable DiffPlanarRange range) {
		id = comment.getId();
		updated = !comment.isVisitedAfter(comment.getLastUpdate().getDate());
		this.range = range;
	}

	public long getId() {
		return id;
	}

	@Nullable
	public DiffPlanarRange getRange() {
		return range;
	}

	public boolean isUpdated() {
		return updated;
	}
	
}