package io.onedev.server.web.page.project.blob.render.renderers.source;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.model.CodeComment;

class CommentInfo {
	
	CommentInfo(CodeComment comment, PlanarRange range) {
		id = comment.getId();
		updated = !comment.isVisitedAfter(comment.getLastUpdate().getDate());
		this.range = range;
	}
	
	long id;
	
	PlanarRange range;
	
	boolean updated;
	
}