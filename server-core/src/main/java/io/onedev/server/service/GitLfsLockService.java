package io.onedev.server.service;

import io.onedev.server.model.GitLfsLock;

public interface GitLfsLockService extends EntityService<GitLfsLock> {

	GitLfsLock find(String path);

    void create(GitLfsLock lock);
	
}
