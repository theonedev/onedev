package io.onedev.server.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.EntityComment;

@Entity
@Table(indexes={
		@Index(columnList="comment_id"), @Index(columnList="user_id"),
		@Index(columnList="pullRequest_id"),
})
public class CodeCommentReply extends EntityComment {
	
	private static final long serialVersionUID = 1L;
	
	public static final String PROP_COMMENT = "comment";
	
	public static final String PROP_COMPARE_CONTEXT = "compareContext";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private CodeComment comment;
	
	@Embedded
	private CompareContext compareContext;
	
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

	@Override
	public AbstractEntity getEntity() {
		return getComment();
	}
	
}
