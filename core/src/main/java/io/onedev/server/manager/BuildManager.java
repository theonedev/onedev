package io.onedev.server.manager;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.Build;
import io.onedev.server.model.Configuration;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

public interface BuildManager extends EntityManager<Build> {
	
	List<Build> query(Project project, String commit);
	
	@Nullable
	Build findByCommit(Configuration configuration, String commit);
	
	@Nullable
	Build findByName(Configuration configuration, String name);

	@Nullable
	Build findByFQN(Project project, String fqn);
	
	List<Build> query(Project project, String term, int count);
	
	@Nullable
	Build findPrevious(Build build);

	@Nullable
	Build findByUUID(String uuid);
	
	List<Build> queryAfter(Project project, @Nullable String afterCommentUUID, int count);
	
}
