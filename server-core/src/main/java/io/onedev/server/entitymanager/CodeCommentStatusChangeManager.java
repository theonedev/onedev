package io.onedev.server.entitymanager;

import java.util.Collection;

import javax.annotation.Nullable;

import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.persistence.dao.EntityManager;

public interface CodeCommentStatusChangeManager extends EntityManager<CodeCommentStatusChange> {

	void save(CodeCommentStatusChange change, @Nullable String note);
	
	void save(Collection<CodeCommentStatusChange> changes, @Nullable String note);
	
}
