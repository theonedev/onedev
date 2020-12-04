package io.onedev.server.util;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;

public class ProjectAndRevision implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String SEPARATOR = ":";

	private final Long projectId;
	
	private final String revision;
	
	public ProjectAndRevision(Long projectId, String revision) {
		this.projectId = projectId;
		if (StringUtils.isNotBlank(revision))
			this.revision = revision;
		else
			this.revision = null;
	}

	public ProjectAndRevision(Project project, String revision) {
		this(project.getId(), revision);
	}
	
	public ProjectAndRevision(String projectAndRevision) {
		this(Long.valueOf(StringUtils.substringBefore(projectAndRevision, SEPARATOR)), 
				StringUtils.substringAfter(projectAndRevision, SEPARATOR));
	}
	
	public Long getProjectId() {
		return projectId;
	}

	public String getRevision() {
		return revision;
	}
	
	@Nullable
	public String getBranch() {
		Ref branchRef = getProject().getBranchRef(getRevision());
		if (branchRef != null)
			return GitUtils.ref2branch(branchRef.getName());
		else
			return null;
	}
	
	@Nullable
	public String getTag() {
		Ref tagRef = getProject().getTagRef(getRevision());
		if (tagRef != null)
			return GitUtils.ref2tag(tagRef.getName());
		else
			return null;
	}
	
	@Nullable
	public ObjectId getObjectId(boolean mustExist) {
		return getProject().getObjectId(normalizeRevision(), mustExist);
	}
	
	public ObjectId getObjectId() {
		return getObjectId(true);
	}
	
	public RevCommit getCommit(boolean mustExist) {
		return getProject().getRevCommit(getObjectId(mustExist), mustExist);
	}
	
	public RevCommit getCommit() {
		return getCommit(true);
	}

	@Nullable
	public String getObjectName(boolean mustExist) {
		ObjectId objectId = getObjectId(mustExist);
		return objectId!=null?objectId.name():null;
	}
	
	public String getObjectName() {
		return getObjectName(true);
	}

	public String getFQN() {
		return getProject().getName() + SEPARATOR + revision;		
	}
	
	public boolean isDefault() {
		return getRevision().equals(getProject().getDefaultBranch());
	}

	public Project getProject() {
		return OneDev.getInstance(ProjectManager.class).load(projectId);
	}
	
	protected String normalizeRevision() {
		return revision;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ProjectAndRevision))
			return false;
		if (this == other)
			return true;
		ProjectAndRevision otherProjectAndRevision = (ProjectAndRevision) other;
		return new EqualsBuilder()
				.append(projectId, otherProjectAndRevision.projectId)
				.append(revision, otherProjectAndRevision.revision)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(projectId).append(revision).toHashCode();
	}
	
	@Override
	public String toString() {
		return projectId + SEPARATOR + revision;
	}

}
