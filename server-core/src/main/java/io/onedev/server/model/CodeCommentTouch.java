package io.onedev.server.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.onedev.server.model.support.EntityTouch;
import static io.onedev.server.model.CodeCommentTouch.*;

@Entity
@Table(indexes={@Index(columnList="o_project_id"), @Index(columnList= PROP_COMMENT_ID)})
public class CodeCommentTouch extends EntityTouch {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_PROJECT = "project";
	
	public static final String PROP_COMMENT_ID = "commentId";

	@ManyToOne(fetch=FetchType.LAZY)
	private Project project;
	
	private Long commentId;

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Long getCommentId() {
		return commentId;
	}

	public void setCommentId(Long commentId) {
		this.commentId = commentId;
	}

	@Override
	public Class<? extends AbstractEntity> getEntityClass() {
		return CodeComment.class;
	}

	@Override
	public Long getProjectId() {
		return getProject().getId();
	}

	@Override
	public Long getEntityId() {
		return getCommentId();
	}
	
}
