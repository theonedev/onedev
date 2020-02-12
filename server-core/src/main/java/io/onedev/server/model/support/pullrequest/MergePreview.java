package io.onedev.server.model.support.pullrequest;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;

import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;

@SuppressWarnings("serial")
@Embeddable
public class MergePreview implements Serializable {
	
	public static final String COLUMN_TARGET_HEAD = "PREVIEW_TARGET_HEAD";
	
	public static final String COLUMN_REQUEST_HEAD = "PREVIEW_REQUEST_HEAD";
	
	public static final String COLUMN_MERGE_STRATEGY = "PREVIEW_MERGE_STRATEGY";
	
	public static final String COLUMN_MERGED = "PREVIEW_MERGED";
	
	public static final String PROP_MERGED = "merged";
	
	public static final String PROP_REQUEST_HEAD = "requestHead";
	
	@Column(name=COLUMN_TARGET_HEAD)
	private String targetHead;
	
	@Column(name=COLUMN_REQUEST_HEAD)
	private String requestHead;
	
	@Column(name=COLUMN_MERGE_STRATEGY)
	private MergeStrategy mergeStrategy;
	
	@Column(name=COLUMN_MERGED)
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

	public boolean isUpToDate(PullRequest request) {
		return getRequestHead().equals(request.getHeadCommitHash())
				&& getTargetHead().equals(request.getTarget().getObjectName())
				&& getMergeStrategy() == request.getMergeStrategy();
	}

	public void syncRef(PullRequest request) {
		Project project = request.getTargetProject();
		ObjectId mergedId = getMerged()!=null? ObjectId.fromString(getMerged()): null;
		RefUpdate refUpdate = GitUtils.getRefUpdate(project.getRepository(), request.getMergeRef());
		if (mergedId != null && !mergedId.equals((project.getObjectId(request.getMergeRef(), false)))) {
			refUpdate.setNewObjectId(mergedId);
			GitUtils.updateRef(refUpdate);
		} else if (merged == null && project.getObjectId(request.getMergeRef(), false) != null) {
			GitUtils.deleteRef(refUpdate);
		}		
	}
	
}