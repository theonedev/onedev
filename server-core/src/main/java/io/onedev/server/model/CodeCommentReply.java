package io.onedev.server.model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.EntityComment;

@Entity
@Table(indexes={
		@Index(columnList="o_comment_id"), @Index(columnList="o_user_id"), 
		@Index(columnList="o_pullRequest_id"),
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
