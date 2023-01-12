package io.onedev.server.model;

import javax.persistence.*;

@Entity
@Table(
		indexes={@Index(columnList="o_comment_id"), @Index(columnList="o_user_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_comment_id", "o_user_id"})}
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
