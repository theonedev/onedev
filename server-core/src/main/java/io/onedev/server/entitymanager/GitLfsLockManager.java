package io.onedev.server.entitymanager;

import io.onedev.server.model.GitLfsLock;
import io.onedev.server.persistence.dao.EntityManager;

public interface GitLfsLockManager extends EntityManager<GitLfsLock> {

	GitLfsLock find(String path);

    void create(GitLfsLock lock);
	
}
