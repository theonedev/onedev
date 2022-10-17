package io.onedev.server.model.support.pullrequest;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import io.onedev.server.model.PullRequest;

@SuppressWarnings("serial")
@Embeddable
public class MergePreview implements Serializable {
	
	public static final String COLUMN_TARGET_HEAD_COMMIT_HASH = "PREVIEW_TARGET_HEAD";
	
	public static final String COLUMN_HEAD_COMMIT_HASH = "PREVIEW_HEAD";
	
	public static final String COLUMN_MERGE_STRATEGY = "PREVIEW_MERGE_STRATEGY";
	
	public static final String COLUMN_MERGE_COMMIT_HASH = "PREVIEW_MERGE_COMMIT";
	
	public static final String PROP_MERGED_COMMIT_HASH = "mergeCommitHash";
	
	public static final String PROP_HEAD_COMMIT_HASH = "headCommitHash";
	
	@Column(name=COLUMN_TARGET_HEAD_COMMIT_HASH)
	private String targetHeadCommitHash;
	
	@Column(name=COLUMN_HEAD_COMMIT_HASH)
	private String headCommitHash;
	
	@Column(name=COLUMN_MERGE_STRATEGY)
	private MergeStrategy mergeStrategy;
	
	@Column(name=COLUMN_MERGE_COMMIT_HASH)
	private String mergeCommitHash;
	
	@SuppressWarnings("unused")
	private MergePreview() {
	}
	
	public MergePreview(String targetHeadCommitHash, String headCommitHash, 
			MergeStrategy mergeStrategy, @Nullable String mergeCommitHash) {
		this.targetHeadCommitHash = targetHeadCommitHash;
		this.headCommitHash = headCommitHash;
		this.mergeStrategy = mergeStrategy;
		this.mergeCommitHash = mergeCommitHash;
	}

	public String getTargetHeadCommitHash() {
		return targetHeadCommitHash;
	}

	public String getHeadCommitHash() {
		return headCommitHash;
	}

	public MergeStrategy getMergeStrategy() {
		return mergeStrategy;
	}

	public void setTargetHeadCommitHash(String targetHeadCommitHash) {
		this.targetHeadCommitHash = targetHeadCommitHash;
	}

	public void setHeadCommitHash(String headCommitHash) {
		this.headCommitHash = headCommitHash;
	}

	public void setIntegrationStrategy(MergeStrategy mergeStrategy) {
		this.mergeStrategy = mergeStrategy;
	}

	/**
	 * Merge commit hash 
	 * 
	 * @return
	 * 			null if there are conflicts
	 */
	@Nullable
	public String getMergeCommitHash() {
		return mergeCommitHash;
	}

	public void setMergeCommitHash(String mergeCommitHash) {
		this.mergeCommitHash = mergeCommitHash;
	}

	public boolean isUpToDate(PullRequest request) {
		return getHeadCommitHash().equals(request.getLatestUpdate().getHeadCommitHash())
				&& getTargetHeadCommitHash().equals(request.getTarget().getObjectName(false))
				&& getMergeStrategy() == request.getMergeStrategy();
	}

}