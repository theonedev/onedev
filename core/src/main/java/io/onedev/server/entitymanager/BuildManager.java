package io.onedev.server.entitymanager;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.Build;
import io.onedev.server.model.Configuration;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;

public interface BuildManager extends EntityManager<Build> {
	
	List<Build> query(Project project, String commitHash);
	
	@Nullable
	Build findByCommit(Configuration configuration, String commitHash);
	
	@Nullable
	Build findByVersion(Configuration configuration, String name);

	@Nullable
	Build findByFQN(Project project, String fqn);
	
	List<Build> query(Project project, String term, int count);
	
	List<Build> queryAfter(Project project, Long afterBuildId, int count);

	List<Build> query(Project project, User user, EntityQuery<Build> buildQuery, int firstResult, int maxResults);
	
	int count(Project project, User user, EntityCriteria<Build> buildCriteria);
	
	void cleanupBuilds(Configuration configuration);
	
}
