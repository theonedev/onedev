package com.pmease.gitplex.core.manager;

import javax.annotation.Nullable;

import com.pmease.commons.bootstrap.Lifecycle;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

public interface RepositoryManager extends Lifecycle {
	
	@Nullable Repository findBy(String ownerName, String repositoryName);
	
	@Nullable Repository findBy(User owner, String repositoryName);

	@Nullable Repository findBy(String repositoryFQN);

	/**
	 * Fork specified repository as specified user.
	 * 
	 * @param repository
	 * 			repository to be forked
	 * @param user
	 * 			user forking the repository
	 * @return
	 * 			newly forked repository. If the repository has already been forked, return the 
	 * 			repository forked previously
	 */
	Repository fork(Repository repository, User user);
	
	void checkSanity();
	
	void checkSanity(Repository repository);
	
	void save(Repository repository);
	
	void delete(Repository repository);
}
