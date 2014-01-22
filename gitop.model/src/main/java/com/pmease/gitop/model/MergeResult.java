package com.pmease.gitop.model;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class MergeResult {
	
	@Column(nullable=false)
	private String branchHead;
	
	@Column(nullable=false)
	private String requestHead;
	
	@Column(nullable=false)
	private String mergeBase;
	
	private String mergeHead;
	
	public MergeResult(String branchHead, String requestHead, String mergeBase, @Nullable String mergeHead) {
		this.branchHead = branchHead;
		this.requestHead = requestHead;
		this.mergeHead = mergeHead;
	}

	public String getBranchHead() {
		return branchHead;
	}

	public String getRequestHead() {
		return requestHead;
	}

	@Nullable
	public String getMergeHead() {
		return mergeHead;
	}
	
	public boolean isConflict() {
		return mergeHead == null;
	}
	
	public boolean isFastForward() {
		return requestHead.equals(mergeHead);
	}
	
}