package io.onedev.server.model;

import java.io.Serial;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import io.onedev.server.model.support.EntityComment;

@Entity
@Table(indexes={
		@Index(columnList="comment_id"), @Index(columnList="user_id"),
})
public class CodeCommentReply extends EntityComment {
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	public static final String PROP_COMMENT = "comment";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private CodeComment comment;

	public CodeComment getComment() {
		return comment;
	}

	public void setComment(CodeComment comment) {
		this.comment = comment;
	}

	@Override
	public AbstractEntity getEntity() {
		return getComment();
	}
	
}
