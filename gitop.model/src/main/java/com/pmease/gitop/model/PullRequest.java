package com.pmease.gitop.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Entity
public class PullRequest extends AbstractEntity {

	public enum Status {
		PENDING_CHECK, PENDING_APPROVAL, PENDING_UPDATE, 
		PENDING_MERGE, MERGED, DECLINED;
	}

	private String title;

	private boolean autoMerge;

	private boolean autoCreated;

	@ManyToOne
	@JoinColumn(nullable = false)
	private User submitter;

	@ManyToOne
	@JoinColumn(nullable = false)
	private Branch target;

	@ManyToOne
	@JoinColumn(nullable = false)
	private Branch source;

	@Lob
	private CheckResult checkResult;

	@Column(nullable = false)
	private Status status = Status.PENDING_CHECK;
	
	@Lob
	private MergeResult mergeResult;

	private transient List<PullRequestUpdate> sortedUpdates;

	private transient List<PullRequestUpdate> effectiveUpdates;

	private transient PullRequestUpdate baseUpdate;
	
	@OneToMany(mappedBy = "request", cascade = CascadeType.REMOVE)
	private Collection<PullRequestUpdate> updates = new ArrayList<PullRequestUpdate>();

	@OneToMany(mappedBy = "request", cascade = CascadeType.REMOVE)
	private Collection<VoteInvitation> voteInvitations = new ArrayList<VoteInvitation>();

	/**
	 * Get title of this merge request.
	 * 
	 * @return user specified title of this merge request, <tt>null</tt> for
	 *         auto-created merge request.
	 */
	public @Nullable
	String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isAutoMerge() {
		return autoMerge;
	}

	public void setAutoMerge(boolean autoMerge) {
		this.autoMerge = autoMerge;
	}
	
	/**
	 * Get title of this merge request, and use title of latest merge request
	 * update for auto-created merge request.
	 * 
	 * @return title of this merge request, or title of latest merge request
	 *         update for auto-created merge request.
	 */
	public String findTitle() {
		if (getTitle() != null)
			return getTitle();
		else
			return getLatestUpdate().getSubject();
	}

	public boolean isAutoCreated() {
		return autoCreated;
	}

	public void setAutoCreated(boolean autoCreated) {
		this.autoCreated = autoCreated;
	}

	public User getSubmitter() {
		return submitter;
	}

	public void setSubmitter(User submitter) {
		this.submitter = submitter;
	}

	public Branch getTarget() {
		return target;
	}

	public void setTarget(Branch target) {
		this.target = target;
	}

	public Branch getSource() {
		return source;
	}

	public void setSource(Branch source) {
		this.source = source;
	}

	public Collection<PullRequestUpdate> getUpdates() {
		return updates;
	}

	public void setUpdates(Collection<PullRequestUpdate> updates) {
		this.updates = updates;
	}

	public Collection<VoteInvitation> getVoteInvitations() {
		return voteInvitations;
	}

	public void setVoteInvitations(Collection<VoteInvitation> voteInvitations) {
		this.voteInvitations = voteInvitations;
	}

	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}

	public boolean isOpen() {
		return status != Status.MERGED && status != Status.DECLINED;
	}
	
	public PullRequestUpdate getBaseUpdate() {
		if (baseUpdate != null) {
			return baseUpdate;
		} else {
			return getEffectiveUpdates().iterator().next();
		}
	}

	public void setBaseUpdate(PullRequestUpdate baseUpdate) {
		this.baseUpdate = baseUpdate;
	}

	public CheckResult getCheckResult() {
		return checkResult;
	}
	
	public void setCheckResult(CheckResult checkResult) {
		this.checkResult = checkResult;
	}
	
	public MergeResult getMergeResult() {
		return mergeResult;
	}
	
	public void setMergeResult(MergeResult mergeResult) {
		this.mergeResult = mergeResult;
	}

	/**
	 * Get list of sorted updates.
	 * <p>
	 * 
	 * @return list of sorted updates ordered by update id reversely
	 */
	public List<PullRequestUpdate> getSortedUpdates() {
		if (sortedUpdates == null) {
			Preconditions.checkState(!getUpdates().isEmpty());
			sortedUpdates = new ArrayList<PullRequestUpdate>(getUpdates());

			Collections.sort(sortedUpdates);
			Collections.reverse(sortedUpdates);
		}
		return sortedUpdates;
	}

	/**
	 * Get list of effective updates in reverse order. Update is considered
	 * effective if it is not ancestor of target branch head.
	 * 
	 * @return list of effective updates in reverse order.
	 */
	public List<PullRequestUpdate> getEffectiveUpdates() {
		if (effectiveUpdates == null) {
			effectiveUpdates = new ArrayList<PullRequestUpdate>();

			Git git = getTarget().getProject().code();
			for (PullRequestUpdate update : getSortedUpdates()) {
				if (!git.isAncestor(update.getHeadCommit(), getTarget().getHeadCommit())) {
					effectiveUpdates.add(update);
				} else {
					break;
				}
			}
			
			Preconditions.checkState(!effectiveUpdates.isEmpty());
		}
		return effectiveUpdates;
	}

	public PullRequestUpdate getLatestUpdate() {
		return getSortedUpdates().iterator().next();
	}
	
	public Collection<String> findTouchedFiles() {
		Git git = getTarget().getProject().code();
		return git.listChangedFiles(getTarget().getHeadCommit(), getLatestUpdate().getHeadCommit());
	}

	public String getMergeRef() {
		return "refs/gitop/pulls/" + getId() + "/merge";
	}
	
	public static class MergeResult implements Serializable {
		
		private final String branchHead;
		
		private final String requestHead;
		
		private final String merged;

		public MergeResult(final String branchHead, final String requestHead, final String merged) {
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

		public String getMerged() {
			return merged;
		}
		
		public boolean isConflict() {
			return merged == null;
		}
		
		public boolean isFastForward() {
			return requestHead.equals(merged);
		}
		
	};

}
