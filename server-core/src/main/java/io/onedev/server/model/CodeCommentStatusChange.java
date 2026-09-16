package io.onedev.server.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import io.onedev.server.model.support.CompareContext;

@Entity
@Table(indexes={
		@Index(columnList="comment_id"), @Index(columnList="user_id"),
		@Index(columnList="pullRequest_id")})
public class CodeCommentStatusChange extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_COMMENT = "comment";

	public static final String PROP_USER = "user";

	public static final String PROP_DATE = "date";

	public static final String PROP_COMPARE_CONTEXT = "compareContext";
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private CodeComment comment;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;
	
	@Column(nullable=false)
	private Date date = new Date();
	
	private boolean resolved;
	
	@Embedded
	private CompareContext compareContext;
	
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

	public CompareContext getCompareContext() {
		return compareContext;
	}

	public void setCompareContext(CompareContext compareContext) {
		this.compareContext = compareContext;
	}

	public String getAnchor() {
		return getClass().getSimpleName() + "-" + getId();
	}
	
}
