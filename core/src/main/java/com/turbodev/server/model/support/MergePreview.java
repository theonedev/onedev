package com.turbodev.server.model.support;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.eclipse.jgit.lib.ObjectId;

import com.turbodev.server.model.PullRequest;

@SuppressWarnings("serial")
@Embeddable
public class MergePreview implements Serializable {
	
	@Column(name="PREVIEW_TARGET_HEAD")
	private String targetHead;
	
	@Column(name="PREVIEW_REQUEST_HEAD")
	private String requestHead;
	
	@Column(name="PREVIEW_MERGE_STRATEGY")
	private MergeStrategy mergeStrategy;
	
	@Column(name="PREVIEW_MERGED")
	private String merged;
	
	@SuppressWarnings("unused")
	private MergePreview() {
	}
	
	public MergePreview(String targetHead, String requestHead, MergeStrategy mergeStrategy, @Nullable String merged) {
		this.targetHead = targetHead;
		this.requestHead = requestHead;
		this.mergeStrategy = mergeStrategy;
		this.merged = merged;
	}

	public String getTargetHead() {
		return targetHead;
	}

	public String getRequestHead() {
		return requestHead;
	}

	public MergeStrategy getMergeStrategy() {
		return mergeStrategy;
	}

	public void setTargetHead(String targetHead) {
		this.targetHead = targetHead;
	}

	public void setRequestHead(String requestHead) {
		this.requestHead = requestHead;
	}

	public void setIntegrationStrategy(MergeStrategy mergeStrategy) {
		this.mergeStrategy = mergeStrategy;
	}

	/**
	 * Integrated commit hash 
	 * 
	 * @return
	 * 			null if there are conflicts
	 */
	@Nullable
	public String getMerged() {
		return merged;
	}

	public void setMerged(String merged) {
		this.merged = merged;
	}

	public boolean isObsolete(PullRequest request) {
		if (getRequestHead().equals(request.getHeadCommitHash())
				&& getTargetHead().equals(request.getTarget().getObjectName())
				&& getMergeStrategy() == request.getMergeStrategy()
				&& (getMerged() == null || ObjectId.fromString(getMerged()).equals((request.getTargetProject().getObjectId(request.getMergeRef(), false))))) {
			return false;
		} else {
			return true;
		}
	}
	
}