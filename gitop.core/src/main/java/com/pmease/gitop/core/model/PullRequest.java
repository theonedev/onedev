package com.pmease.gitop.core.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.pmease.commons.util.FileUtils;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.gatekeeper.GateKeeper;
import com.pmease.gitop.core.gatekeeper.checkresult.Blocked;
import com.pmease.gitop.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.core.gatekeeper.checkresult.Pending;
import com.pmease.gitop.core.gatekeeper.checkresult.Rejected;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.VoteInvitationManager;

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
	
	public MergeResult getMergeResult() {
		return mergeResult;
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

			Git git = getTarget().getProject().getCodeRepo();
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
		Git git = getTarget().getProject().getCodeRepo();
		return git.listChangedFiles(getTarget().getHeadCommit(), getLatestUpdate().getHeadCommit());
	}

	/**
	 * Invite specified number of users in candicates to vote for this request.
	 * <p>
	 * 
	 * @param candidates a collection of users to invite users from
	 * @param count number of users to invite
	 */
	public void inviteToVote(Collection<User> candidates, int count) {
		Collection<User> copyOfCandidates = new HashSet<User>(candidates);

		// submitter is not allowed to vote for this request
		copyOfCandidates.remove(getSubmitter());

		/*
		 * users already voted since base update should be excluded from
		 * invitation list as their votes are still valid
		 */
		for (Vote vote : getBaseUpdate().listVotesOnwards()) {
			copyOfCandidates.remove(vote.getVoter());
		}

		Set<User> invited = new HashSet<User>();
		for (VoteInvitation each : getVoteInvitations())
			invited.add(each.getVoter());

		invited.retainAll(copyOfCandidates);

		for (int i = 0; i < count - invited.size(); i++) {
			User selected = Collections.min(copyOfCandidates, new Comparator<User>() {

				@Override
				public int compare(User user1, User user2) {
					return user1.getVotes().size() - user2.getVotes().size();
				}

			});

			copyOfCandidates.remove(selected);

			VoteInvitation invitation = new VoteInvitation();
			invitation.setRequest(this);
			invitation.setVoter(selected);
			invitation.getRequest().getVoteInvitations().add(invitation);
			invitation.getVoter().getVoteInvitations().add(invitation);

			Gitop.getInstance(VoteInvitationManager.class).save(invitation);
		}

	}
	
	private String getMergeRef() {
		return "refs/gitop/pulls/" + getId() + "/merge";
	}
	
	/**
	 * Refresh internal state of this pull request. Pull request should be refreshed when:
	 * <li> It is updated 
	 * <li> Head of target branch changes
	 * <li> Some one vote against it
	 * <li> CI system reports completion of build against relevant commits 
	 */
	public void refresh() {
		Git git = getTarget().getProject().getCodeRepo();
		String branchHead = getTarget().getHeadCommit();
		String requestHead = getLatestUpdate().getHeadCommit();
				
		if (git.isAncestor(requestHead, branchHead)) {
			status = Status.MERGED;
		} else {
			if (git.isAncestor(branchHead, requestHead)) {
				mergeResult = new MergeResult(branchHead, requestHead, requestHead);
				git.updateRef(getMergeRef(), requestHead, null, null);
			} else {
				if (mergeResult != null 
						&& (!mergeResult.getBranchHead().equals(branchHead) 
								|| !mergeResult.getRequestHead().equals(requestHead))) {
					 // Commits for merging have been changed since last merge, we have to
					 // re-merge 
					mergeResult = null;
				}
				if (mergeResult != null && mergeResult.getMerged() != null 
						&& !mergeResult.getMerged().equals(git.resolveRef(getMergeRef(), false))) {
					 // Commits for merging have not been changed since last merge, but recorded 
					 // merge is incorrect in repository, so we have to re-merge 
					mergeResult = null;
				}
				if (mergeResult == null) {
					File tempDir = FileUtils.createTempDir();
					try {
						Git tempGit = new Git(tempDir);
						
						 // Branch name here is not significant, we just use an existing branch
						 // in cloned repository to hold mergeBase, so that we can merge with 
						 // previousUpdate 
						String branchName = getTarget().getName();
						tempGit.clone(git.repoDir().getAbsolutePath(), false, true, true, branchName);
						tempGit.updateRef("HEAD", requestHead, null, null);
						tempGit.reset(null, null);
						if (tempGit.merge(branchHead, null, null, null)) {
							git.fetch(tempGit.repoDir().getAbsolutePath(), "+HEAD:" + getMergeRef());
							mergeResult = new MergeResult(branchHead, requestHead, git.resolveRef(getMergeRef(), true));
						} else {
							mergeResult = new MergeResult(branchHead, requestHead, null);
						}
					} finally {
						FileUtils.deleteDir(tempDir);
					}
				}
			}
			
			GateKeeper gateKeeper = getTarget().getProject().getGateKeeper();
			checkResult = gateKeeper.check(this);
	
			VoteInvitationManager voteInvitationManager =
					Gitop.getInstance(VoteInvitationManager.class);
			for (VoteInvitation invitation : getVoteInvitations()) {
				if (!checkResult.canVote(invitation.getVoter(), this))
					voteInvitationManager.delete(invitation);
			}
	
			Preconditions.checkNotNull(mergeResult);
			
			if (checkResult instanceof Pending || checkResult instanceof Blocked) {
				status = Status.PENDING_APPROVAL;
			} else if (checkResult instanceof Rejected) {
				status = Status.PENDING_UPDATE;
			} else if (mergeResult.getMerged() == null) {
				status = Status.PENDING_UPDATE;
			} else {
				status = Status.PENDING_MERGE;
			}
		}

		Gitop.getInstance(PullRequestManager.class).save(this);
	}

	public void decline() {
		status = Status.DECLINED;
		Gitop.getInstance(PullRequestManager.class).save(this);
	}
	
	public boolean merge() {
		refresh();

		if (status == Status.PENDING_MERGE) {
			Git git = getTarget().getProject().getCodeRepo();
			git.updateRef(getTarget().getHeadRef(), mergeResult.getMerged(), 
					getTarget().getHeadCommit(), "merge pull request");
			status = Status.MERGED;
			Gitop.getInstance(PullRequestManager.class).save(this);
			return true;
		} else {
			return false;
		}
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
