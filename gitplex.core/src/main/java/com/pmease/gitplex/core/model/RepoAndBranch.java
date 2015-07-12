package com.pmease.gitplex.core.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;

public class RepoAndBranch implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final String SEPARATOR = "-";

	private final Long repoId;
	
	private final String branch;
	
	private transient Repository repository;
	
	public RepoAndBranch(Long repoId, String branch) {
		this.repoId = repoId;
		this.branch = branch;
	}

	public RepoAndBranch(Repository repository, String branch) {
		this.repoId = repository.getId();
		this.branch = branch;
		
		this.repository = repository;
	}
	
	public RepoAndBranch(String id) {
		this(Long.valueOf(StringUtils.substringBefore(id, SEPARATOR)), 
				StringUtils.substringAfter(id, SEPARATOR));
	}
	
	public Long getRepoId() {
		return repoId;
	}

	public String getBranch() {
		return branch;
	}
	
	public static void trim(Collection<RepoAndBranch> repoAndBranches) {
		Dao dao = GitPlex.getInstance(Dao.class);
		for (Iterator<RepoAndBranch> it = repoAndBranches.iterator(); it.hasNext();) {
			if (dao.get(Repository.class, it.next().getRepoId()) == null)
				it.remove();
		}
	}
	
	public Repository getRepository() {
		if (repository == null)
			repository = GitPlex.getInstance(Dao.class).load(Repository.class, repoId);
		return repository;
	}
	
	public String getFQN() {
		return getRepository().getBranchFQN(branch);		
	}

	public String getId() {
		return repoId + SEPARATOR + branch;
	}
	
	@Nullable
	public String getHead(boolean mustExist) {
		ObjectId commitId = getRepository().getObjectId(getBranch(), mustExist);
		return commitId!=null?commitId.name():null;
	}
	
	public String getHead() {
		return getHead(true);
	}
	
	public boolean isDefault() {
		return getRepository().getDefaultBranch().equals(getBranch());
	}

	public void delete() {
		getRepository().deleteBranch(getBranch());
	}
}
