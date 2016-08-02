package com.pmease.gitplex.core.entity.support;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.DepotManager;

public class DepotAndRevision implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String SEPARATOR = ":";

	private final Long depotId;
	
	private final String revision;
	
	public DepotAndRevision(Long depotId, String revision) {
		this.depotId = depotId;
		this.revision = revision;
	}

	public DepotAndRevision(Depot depot, String revision) {
		this.depotId = depot.getId();
		this.revision = revision;
	}
	
	public DepotAndRevision(String depotAndRevision) {
		this(Long.valueOf(StringUtils.substringBefore(depotAndRevision, SEPARATOR)), 
				StringUtils.substringAfter(depotAndRevision, SEPARATOR));
	}
	
	public Long getDepotId() {
		return depotId;
	}

	public String getRevision() {
		return revision;
	}
	
	@Nullable
	public Ref getRef() {
		return getDepot().getRef(getRevision());
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
		return getDepot().getObjectId(normalizeRevision(), mustExist);
	}
	
	public ObjectId getObjectId() {
		return getObjectId(true);
	}
	
	public RevCommit getCommit(boolean mustExist) {
		return getDepot().getRevCommit(getObjectId(mustExist), mustExist);
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
		return getDepot().getFQN() + SEPARATOR + revision;		
	}
	
	public boolean isDefault() {
		return getRevision().equals(getDepot().getDefaultBranch());
	}

	public void delete() {
		getDepot().deleteBranch(getRevision());
	}
	
	public Depot getDepot() {
		return GitPlex.getInstance(DepotManager.class).load(depotId);
	}
	
	protected String normalizeRevision() {
		return revision;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof DepotAndRevision))
			return false;
		if (this == other)
			return true;
		DepotAndRevision otherDepotAndRevision = (DepotAndRevision) other;
		return new EqualsBuilder()
				.append(depotId, otherDepotAndRevision.depotId)
				.append(revision, otherDepotAndRevision.revision)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(depotId).append(revision).toHashCode();
	}
	
	@Override
	public String toString() {
		return depotId + SEPARATOR + revision;
	}

}
