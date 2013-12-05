package com.pmease.gitop.model;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class MergePrediction {
	
	@Column(nullable=false)
	private String branchHead;
	
	@Column(nullable=false)
	private String requestHead;
	
	private String merged;
	
	public MergePrediction(String branchHead, String requestHead, @Nullable String merged) {
		this.branchHead = branchHead;
		this.requestHead = requestHead;
		this.merged = merged;
	}

	public String getBranchHead() {
		return branchHead;
	}

	public String getRequestHead() {
		return requestHead;
	}

	@Nullable
	public String getMerged() {
		return merged;
	}
	
	public boolean isConflict() {
		return merged == null;
	}
	
	public boolean isFastForward() {
		return requestHead.equals(merged);
	}
	
}