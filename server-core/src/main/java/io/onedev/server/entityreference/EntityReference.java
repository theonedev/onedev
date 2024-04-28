package io.onedev.server.entityreference;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nullable;
import java.io.Serializable;

public abstract class EntityReference implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Long projectId;

	private final Long number;

	public EntityReference(Project project, Long number) {
		this(project.getId(), number);
	}

	public EntityReference(Long projectId, Long number) {
		this.projectId = projectId;
		this.number = number;
	}
	
	public Project getProject() {
		return OneDev.getInstance(ProjectManager.class).load(projectId);
	}

	public Long getProjectId() {
		return projectId;
	}

	public Long getNumber() {
		return number;
	}

	public static EntityReference of(String type, Project project, Long number) {
		if (type.length() == 0 || type.equalsIgnoreCase(IssueReference.TYPE))
			return new IssueReference(project, number);
		else if (type.equalsIgnoreCase(BuildReference.TYPE))
			return new BuildReference(project, number);
		else
			return new PullRequestReference(project, number);
	}

	private static Long parseReferenceNumber(String numberString) {
		try {
			return Long.valueOf(numberString);
		} catch (NumberFormatException e) {
			throw new ExplicitException("Invalid reference number: " + numberString);
		}
	}

	public static EntityReference of(String type, String referenceString, @Nullable Project currentProject) {
		var projectManager = OneDev.getInstance(ProjectManager.class);
		var index = referenceString.indexOf('#');
		if (index != -1) {
			var projectPath = referenceString.substring(0, index);
			var number = parseReferenceNumber(referenceString.substring(index + 1));
			if (projectPath.length() == 0) {
				if (currentProject != null)
					return EntityReference.of(type, currentProject, number);
				else
					throw new ExplicitException("Reference project not specified: " + referenceString);
			} else {
				var project = projectManager.findByPath(projectPath);
				if (project != null)
					return EntityReference.of(type, project, number);
				else
					throw new ExplicitException("Reference project not found: " + projectPath);
			}
		}
		index = referenceString.indexOf('-');
		if (index != -1) {
			var projectKey = referenceString.substring(0, index);
			var number = parseReferenceNumber(referenceString.substring(index + 1));
			var project = projectManager.findByKey(projectKey);
			if (project != null)
				return EntityReference.of(type, project, number);
			else
				throw new ExplicitException("Reference project not found with key: " + projectKey);
		}
		throw new ExplicitException("Invalid entity reference: " + referenceString);
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof EntityReference))
			return false;
		if (this == other)
			return true;
		var otherReference = (EntityReference) other;
		return new EqualsBuilder()
				.append(getType(), otherReference.getType())
				.append(projectId, otherReference.projectId)
				.append(number, otherReference.number)
				.isEquals();
	}
	
	public abstract String getType();
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getType()).append(projectId).append(number).toHashCode();
	}
	
	@Override
	public String toString() {
		return toString(null);
	}
	
	public String toString(@Nullable Project currentProject) {
		String string;
		if (getProject().getKey() != null) 
			string = getProject().getKey() + "-" + getNumber();
		else if (getProject().equals(currentProject))
			string = "#" + getNumber();
		else
			string = getProject().getPath() + "#" + getNumber();
		return string;
	}	
	
}
