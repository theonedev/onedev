package com.pmease.gitplex.core.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;

public class RepoAndRevision implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String SEPARATOR = ":";

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
	
	public RepoAndRevision(String repoAndRevision) {
		this(Long.valueOf(StringUtils.substringBefore(repoAndRevision, SEPARATOR)), 
				StringUtils.substringAfter(repoAndRevision, SEPARATOR));
	}
	
	public Long getRepoId() {
		return repoId;
	}

	public String getRevision() {
		return revision;
	}
	
	@Nullable
	public Ref getRef() {
		return getRepository().getRef(getRevision());
	}
	
	@Nullable
	public String getBranch() {
		Ref ref = getRef();
		if (ref != null && ref.getName().startsWith(Constants.R_HEADS))
			return ref.getName().substring(Constants.R_HEADS.length());
		else
			return null;
	}
	
	@Nullable
	public String getTag() {
		Ref ref = getRef();
		if (ref != null && ref.getName().startsWith(Constants.R_TAGS))
			return ref.getName().substring(Constants.R_TAGS.length());
		else
			return null;
	}
	
	@Nullable
	public ObjectId getObjectId(boolean mustExist) {
		return getRepository().getObjectId(normalizeRevision(), mustExist);
	}
	
	public ObjectId getObjectId() {
		return getObjectId(true);
	}
	
	public RevCommit getCommit(boolean mustExist) {
		return getRepository().getRevCommit(getObjectId(mustExist), mustExist);
	}
	
	public RevCommit getCommit() {
		return getCommit(true);
	}

	public String getObjectName(boolean mustExist) {
		ObjectId objectId = getObjectId(mustExist);
		return objectId!=null?objectId.name():null;
	}
	
	public String getObjectName() {
		return getObjectName(true);
	}

	public String getFQN() {
		return getRepository().getRevisionFQN(revision);		
	}
	
	public static void trim(Collection<String> repoAndBranches) {
		Dao dao = GitPlex.getInstance(Dao.class);
		for (Iterator<String> it = repoAndBranches.iterator(); it.hasNext();) {
			RepoAndRevision repoAndRevision = new RepoAndRevision(it.next());
			if (dao.get(Repository.class, repoAndRevision.getRepoId()) == null)
				it.remove();
		}
	}
	
	public boolean isDefault() {
		return getRepository().getDefaultBranch().equals(getRevision());
	}

	public void delete() {
		getRepository().deleteBranch(getRevision());
	}
	
	public Repository getRepository() {
		if (repository == null)
			repository = GitPlex.getInstance(Dao.class).load(Repository.class, repoId);
		return repository;
	}
	
	protected String normalizeRevision() {
		return revision;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof RepoAndRevision))
			return false;
		if (this == other)
			return true;
		RepoAndRevision otherRepoAndRevision = (RepoAndRevision) other;
		return new EqualsBuilder()
				.append(repoId, otherRepoAndRevision.repoId)
				.append(revision, otherRepoAndRevision.revision)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(repoId).append(revision).toHashCode();
	}
	
	@Override
	public String toString() {
		return repoId + SEPARATOR + revision;
	}
	
}
