package com.pmease.gitplex.core.entity.component;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.OptimisticLock;

import com.pmease.gitplex.core.entity.User;

@Embeddable
public class CloseInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Status {INTEGRATED, DISCARDED};
	
	@OptimisticLock(excluded=true)
	@ManyToOne(fetch=FetchType.LAZY)
	private User closedBy;

	@OptimisticLock(excluded=true)
	private Date closeDate;
	
	@OptimisticLock(excluded=true)
	private Status closeStatus;

	public User getClosedBy() {
		return closedBy;
	}

	public void setClosedBy(User closedBy) {
		this.closedBy = closedBy;
	}

	public Date getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(Date closeDate) {
		this.closeDate = closeDate;
	}

	public Status getCloseStatus() {
		return closeStatus;
	}

	public void setCloseStatus(Status closeStatus) {
		this.closeStatus = closeStatus;
	}
	
}
