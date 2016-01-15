package com.pmease.gitplex.core.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;

public class RepoAndRevision implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final String SEPARATOR = "-";

	private final Long repoId;
	
	private final String revision;
	
	private transient Repository repository;
	
	public RepoAndRevision(Long repoId, String revision) {
		this.repoId = repoId;
		this.revision = revision;
	}

	public RepoAndRevision(Repository repository, String revision) {
		this.repoId = repository.getId();
		this.revision = revision;
		
		this.repository = repository;
	}
	
	public RepoAndRevision(String id) {
		this(Long.valueOf(StringUtils.substringBefore(id, SEPARATOR)), 
				StringUtils.substringAfter(id, SEPARATOR));
	}
	
	public Long getRepoId() {
		return repoId;
	}

	public String getRevision() {
		return revision;
	}
	
	public static void trim(Collection<? extends RepoAndRevision> repoAndBranches) {
		Dao dao = GitPlex.getInstance(Dao.class);
		for (Iterator<? extends RepoAndRevision> it = repoAndBranches.iterator(); it.hasNext();) {
			if (dao.get(Repository.class, it.next().getRepoId()) == null)
				it.remove();
		}
	}
	
	public Repository getRepository() {
		if (repository == null)
			repository = GitPlex.getInstance(Dao.class).load(Repository.class, repoId);
		return repository;
	}
	
	public String getId() {
		return repoId + SEPARATOR + revision;
	}
	
}
