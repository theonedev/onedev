package com.pmease.gitop.model;

import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

@Embeddable
public class CloseInfo {

	public enum Status {INTEGRATED, DISCARDED};
	
	private Status closeStatus;

	@ManyToOne
	private User closedBy;
	
	private Date closeDate = new Date();
	
	@SuppressWarnings("unused")
	private CloseInfo() {
	}
	
	public CloseInfo(Status closeStatus, @Nullable User closedBy) {
		this.closeStatus = closeStatus;
		this.closedBy = closedBy;
	}
	
	public @Nullable User getClosedBy() {
		return closedBy;
	}

	public Status getCloseStatus() {
		return closeStatus;
	}

	public Date getCloseDate() {
		return closeDate;
	}

}