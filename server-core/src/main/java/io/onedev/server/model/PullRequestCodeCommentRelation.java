package io.onedev.server.model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import io.onedev.server.model.support.CompareContext;

@Entity
@Table(
		indexes={@Index(columnList="o_comment_id"), @Index(columnList="o_request_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_comment_id", "o_request_id"})
})
public class PullRequestCodeCommentRelation extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_REQUEST = "request";
	
	public static final String PROP_COMMENT = "comment";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private CodeComment comment;
	
	@Embedded
	private CompareContext compareContext;

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

	public CompareContext getCompareContext() {
		return compareContext;
	}

	public void setCompareContext(CompareContext compareContext) {
		this.compareContext = compareContext;
	}

}
