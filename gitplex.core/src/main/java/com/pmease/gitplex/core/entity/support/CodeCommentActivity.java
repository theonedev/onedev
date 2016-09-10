package com.pmease.gitplex.core.entity.support;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeComment.ComparingInfo;

@MappedSuperclass
public abstract class CodeCommentActivity extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private CodeComment comment;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Account user;

	@Column(nullable=false)
	private Date date;
	
	@Embedded
	private CompareContext compareContext;

	@Version
	private long version;
	
	public CodeComment getComment() {
		return comment;
	}

	public void setComment(CodeComment comment) {
		this.comment = comment;
	}

	public Account getUser() {
		return user;
	}

	public void setUser(Account user) {
		this.user = user;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public CompareContext getCompareContext() {
		return compareContext;
	}

	public void setCompareContext(CompareContext compareContext) {
		this.compareContext = compareContext;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
	
	public ComparingInfo getComparingInfo() {
		return new ComparingInfo(getComment().getCommentPos().getCommit(), getCompareContext());
	}

	public String getAnchor() {
		return getClass().getSimpleName() + "-" + getId();
	}
	
	public abstract String getNote();
	
}
