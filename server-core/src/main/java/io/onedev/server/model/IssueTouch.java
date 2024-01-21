package io.onedev.server.model;

import io.onedev.server.model.support.EntityTouch;

import javax.persistence.*;

import static io.onedev.server.model.IssueTouch.*;

@Entity
@Table(indexes={@Index(columnList="o_project_id"), @Index(columnList= PROP_ISSUE_ID)})
public class IssueTouch extends EntityTouch {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_PROJECT = "project";
	
	public static final String PROP_ISSUE_ID = "issueId";

	@ManyToOne(fetch=FetchType.LAZY)
	private Project project;
	
	private Long issueId;

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Long getIssueId() {
		return issueId;
	}

	public void setIssueId(Long issueId) {
		this.issueId = issueId;
	}

	@Override
	public Class<? extends AbstractEntity> getEntityClass() {
		return Issue.class;
	}

	@Override
	public Long getProjectId() {
		return getProject().getId();
	}

	@Override
	public Long getEntityId() {
		return getIssueId();
	}
	
}
