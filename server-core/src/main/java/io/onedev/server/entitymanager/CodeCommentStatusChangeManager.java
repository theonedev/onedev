package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface CodeCommentStatusChangeManager extends EntityManager<CodeCommentStatusChange> {

	void create(CodeCommentStatusChange change, @Nullable String note);
	
	void create(Collection<CodeCommentStatusChange> changes, @Nullable String note);
	
	List<CodeCommentStatusChange> query(User creator, Date fromDate, Date toDate);

}
