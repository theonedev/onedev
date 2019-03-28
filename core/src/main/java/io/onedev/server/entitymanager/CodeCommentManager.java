package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.TextRange;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;

public interface CodeCommentManager extends EntityManager<CodeComment> {
	
	Collection<CodeComment> query(Project project, ObjectId commitId, @Nullable String path);
	
	Collection<CodeComment> query(Project project, ObjectId...commitIds);
	
	Map<CodeComment, TextRange> findHistory(Project project, ObjectId commitId, String path);
		
	List<CodeComment> queryAfter(Project project, @Nullable Long afterCommentId, int count);
	
	void create(CodeComment comment, @Nullable PullRequest request);

	List<CodeComment> query(Project project, @Nullable PullRequest request, User user,
			EntityQuery<CodeComment> commentQuery, int firstResult, int maxResults);
	
	int count(Project project, @Nullable PullRequest request, User user, EntityCriteria<CodeComment> commentCriteria);
	
	void delete(User user, CodeComment comment);
	
	void update(User user, CodeComment comment);
}