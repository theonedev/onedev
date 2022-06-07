package io.onedev.server.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.onedev.server.model.support.CompareContext;

@Entity
@Table(indexes={
		@Index(columnList="o_comment_id"), @Index(columnList="o_user_id")})
public class CodeCommentStatusChange extends AbstractEntity {

	private static final long serialVersionUID = 1L;

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
