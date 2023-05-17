package io.onedev.server.entitymanager;

import io.onedev.server.model.CodeCommentTouch;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

import java.util.List;

public interface CodeCommentTouchManager extends EntityManager<CodeCommentTouch> {
	
	void touch(Project project, Long commentId);

	List<CodeCommentTouch> queryTouchesAfter(Long projectId, Long afterTouchId, int count);
	
}
