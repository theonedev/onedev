package com.pmease.gitplex.web.component.repository;

import java.util.List;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.model.Repository;

public interface RepositoryAware {

	/**
	 * Get repository object.
	 * 
	 * @return
	 * 			repository object
	 */
	Repository getRepository();
	
	/**
	 * Get current revision of the repository.
	 * 
	 * @return
	 * 			current revision of the repository, or default branch if current revision is unknown
	 */
	String getCurrentRevision();
	
	/**
	 * Get current path of the repository.
	 * 
	 * @return
	 * 			current object path of this repository, or <tt>null</tt> if current path is unknown
	 */
	public @Nullable String getCurrentPath();

	/**
	 * Get current object path of the repository as segments. 
	 * 
	 * @return
	 * 			current object path of the repository as segments, or an empty list if current object path is unknown
	 */
	public List<String> getCurrentPathSegments();
	
}
