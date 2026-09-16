package io.onedev.server.model;

import jakarta.persistence.*;

@Entity
@Table(
		indexes={@Index(columnList="comment_id"), @Index(columnList="user_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"comment_id", "user_id"})}
)
public class CodeCommentMention extends AbstractEntity {

	public static final String PROP_USER = "user";
	
	public static final String PROP_COMMENT = "comment";
	
	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(nullable=false)
	private CodeComment comment;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	public CodeComment getComment() {
		return comment;
	}

	public void setComment(CodeComment comment) {
		this.comment = comment;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
}
