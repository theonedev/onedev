package io.onedev.server.manager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.TextRange;
import io.onedev.server.persistence.dao.EntityManager;

public interface CodeCommentManager extends EntityManager<CodeComment> {
	
	Collection<CodeComment> findAll(Project project, ObjectId commitId, @Nullable String path);
	
	Collection<CodeComment> findAll(Project project, ObjectId...commitIds);
	
	@Nullable
	CodeComment find(String uuid);
	
	Map<CodeComment, TextRange> findHistory(Project project, ObjectId commitId, String path);
		
	List<CodeComment> findAllAfter(Project project, @Nullable String commentUUID, int count);
	
	void save(CodeComment comment, @Nullable PullRequest request);
	
}