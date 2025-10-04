package io.onedev.server.service;

import java.util.List;

import io.onedev.server.model.CodeCommentTouch;
import io.onedev.server.model.Project;

public interface CodeCommentTouchService extends EntityService<CodeCommentTouch> {
	
	void touch(Project project, Long commentId, boolean newComment);
	
	List<CodeCommentTouch> queryTouchesAfter(Long projectId, Long afterTouchId, int count);
	
}
