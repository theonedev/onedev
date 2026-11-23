package io.onedev.server.model;

import jakarta.persistence.*;

import java.io.Serial;
import java.util.Date;

@Entity
@Table(indexes={
		@Index(columnList="comment_id"), @Index(columnList="user_id")})
public class CodeCommentStatusChange extends AbstractEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	public static final String PROP_COMMENT = "comment";

	public static final String PROP_USER = "user";

	public static final String PROP_DATE = "date";

	@ManyToOne
	@JoinColumn(nullable=false)
	private CodeComment comment;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;
	
	@Column(nullable=false)
	private Date date = new Date();
	
	private boolean resolved;

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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isResolved() {
		return resolved;
	}

	public void setResolved(boolean resolved) {
		this.resolved = resolved;
	}

	public String getAnchor() {
		return getClass().getSimpleName() + "-" + getId();
	}
	
}
