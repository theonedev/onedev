package io.onedev.server.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.onedev.server.model.support.EntityTouch;
import static io.onedev.server.model.PullRequestTouch.*;

@Entity
@Table(indexes={@Index(columnList="o_project_id"), @Index(columnList= PROP_REQUEST_ID)})
public class PullRequestTouch extends EntityTouch {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_PROJECT = "project";
	
	public static final String PROP_REQUEST_ID = "requestId";

	@ManyToOne(fetch=FetchType.LAZY)
	private Project project;
	
	private Long requestId;

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Long getRequestId() {
		return requestId;
	}

	public void setRequestId(Long requestId) {
		this.requestId = requestId;
	}

	@Override
	public Class<? extends AbstractEntity> getEntityClass() {
		return PullRequest.class;
	}

	@Override
	public Long getProjectId() {
		return getProject().getId();
	}

	@Override
	public Long getEntityId() {
		return getRequestId();
	}
	
}
