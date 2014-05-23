package com.pmease.gitop.model;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.persistence.Embeddable;

@SuppressWarnings("serial")
@Embeddable
public class MergeInfo implements Serializable {
	
	private String branchHead;
	
	private String requestHead;
	
	private String mergeBase;
	
	private String mergeHead;
	
	@SuppressWarnings("unused")
	private MergeInfo() {
	}
	
	public MergeInfo(String branchHead, String requestHead, String mergeBase, @Nullable String mergeHead) {
		this.branchHead = branchHead;
		this.requestHead = requestHead;
		this.mergeBase = mergeBase;
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
	
	public String getMergeBase() {
		return mergeBase;
	}

}