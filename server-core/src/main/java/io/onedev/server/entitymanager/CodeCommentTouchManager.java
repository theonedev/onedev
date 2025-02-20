package io.onedev.server.entitymanager;

import java.util.List;

import io.onedev.server.model.CodeCommentTouch;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

public interface CodeCommentTouchManager extends EntityManager<CodeCommentTouch> {
	
	void touch(Project project, Long commentId, boolean newComment);
	
	List<CodeCommentTouch> queryTouchesAfter(Long projectId, Long afterTouchId, int count);
	
}
