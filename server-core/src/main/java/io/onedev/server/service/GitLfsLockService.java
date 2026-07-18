package io.onedev.server.service;

import io.onedev.server.model.GitLfsLock;
import io.onedev.server.model.Project;

public interface GitLfsLockService extends EntityService<GitLfsLock> {

	GitLfsLock find(Project project, String path);

    void create(GitLfsLock lock);
	
}
