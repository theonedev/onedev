package io.onedev.server.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Table(
		indexes={@Index(columnList="o_request_id"), @Index(columnList="o_comment_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_request_id", "o_comment_id"})
})
@Entity
public class CodeCommentRelation extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_REQUEST = "request";
	
	public static final String PROP_COMMENT = "comment";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private CodeComment comment;

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public CodeComment getComment() {
		return comment;
	}

	public void setComment(CodeComment comment) {
		this.comment = comment;
	}
	
}
