package com.pmease.gitop.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Entity
public class PullRequest extends AbstractEntity {

	public enum Status {
		PENDING_CHECK, PENDING_APPROVAL, PENDING_UPDATE, PENDING_INTEGRATE, INTEGRATED, DECLINED;
	}
	
	@Column(nullable = false)
	private String title;

	private boolean autoMerge;

	@ManyToOne
	private User submitter;
	
	@ManyToOne
	@JoinColumn(nullable = false)
	private Branch target;

	@ManyToOne
	private Branch source;

	@Lob
	private CheckResult checkResult;

	@Column(nullable = false)
	private Status status = Status.PENDING_CHECK;
	
	@Embedded
	private MergeResult mergeResult;

	private transient List<PullRequestUpdate> sortedUpdates;

	private transient List<PullRequestUpdate> effectiveUpdates;

	private transient PullRequestUpdate baseUpdate;
	
	@OneToMany(mappedBy = "request", cascade = CascadeType.REMOVE)
	private Collection<PullRequestUpdate> updates = new ArrayList<PullRequestUpdate>();

	@OneToMany(mappedBy = "request", cascade = CascadeType.REMOVE)
	private Collection<VoteInvitation> voteInvitations = new ArrayList<VoteInvitation>();
	
	@OneToMany(mappedBy = "request", cascade = CascadeType.REMOVE)
	private Collection<PullRequestComment> comments = new ArrayList<PullRequestComment>();

	/**
	 * Get title of this merge request.
	 * 
	 * @return user specified title of this merge request, <tt>null</tt> for
	 *         auto-created merge request.
	 */
	public @Nullable String getTitle() {
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
	 * Get the user submitting the pull request.
	 * 
	 * @return
	 * 			the user submitting the pull request, or <tt>null</tt> if the user 
	 * 			submitting the request is removed.
	 */
	@Nullable
	public User getSubmitter() {
		return submitter;
	}

	public void setSubmitter(@Nullable User submitter) {
		this.submitter = submitter;
	}

	/**
	 * Get target branch of this request.
	 * 
	 * @return
	 * 			target branch of this request
	 */
	public Branch getTarget() {
		return target;
	}

	public void setTarget(@Nullable Branch target) {
		this.target = target;
	}

	/**
	 * Get source branch of this request.
	 * 
	 * @return
	 * 			source branch of this request, or <tt>null</tt> if source branch 
	 * 			is deleted. In case of source branch being deleted, the pull request 
	 * 			may still in opened state for review, but can not be updated with 
	 * 			new commits
	 */
	@Nullable
	public Branch getSource() {
		return source;
	}

	public void setSource(@Nullable Branch source) {
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

	public Collection<PullRequestComment> getComments() {
		return comments;
	}

	public void setComments(Collection<PullRequestComment> comments) {
		this.comments = comments;
	}

	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}

	public boolean isOpen() {
		return status != Status.INTEGRATED && status != Status.DECLINED;
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

	/**
	 * Get gate keeper check result.
	 *  
	 * @return
	 * 			<tt>null</tt> if this pull request has not been refreshed yet
	 */
	public @Nullable CheckResult getCheckResult() {
		return checkResult;
	}
	
	public void setCheckResult(CheckResult checkResult) {
		this.checkResult = checkResult;
	}
	
	/**
	 * Get merge result of this pull request.
	 *  
	 * @return
	 * 			<tt>null</tt> if this pull request has not been refreshed yet
	 */
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

			if (sortedUpdates.size() > 1) {
				Collections.sort(sortedUpdates);
				Collections.reverse(sortedUpdates);
			}
		}
		return sortedUpdates;
	}

	/**
	 * Get list of effective updates in reverse order. Update is considered
	 * effective if it is not ancestor of target branch head.
	 * 
	 * @return 
	 * 			list of effective updates in reverse order.
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

	public String getHeadRef() {
		return Project.REFS_GITOP + "pulls/" + getId() + "/head";
	}
	
	public String getMergeRef() {
		return Project.REFS_GITOP + "pulls/" + getId() + "/merge";
	};

	/**
	 * Delete refs of this pull request, without touching refs of its updates.
	 */
	public void deleteRefs() {
		Git git = getTarget().getProject().code();
		git.deleteRef(getHeadRef());
		git.deleteRef(getMergeRef());
	}
	
	public String getLockName() {
		return "pull request: " + getId();
	}
	
	public static class CriterionHelper {
		public static Criterion ofOpen() {
			return Restrictions.and(
					Restrictions.not(Restrictions.eq("status", PullRequest.Status.INTEGRATED)),
					Restrictions.not(Restrictions.eq("status", PullRequest.Status.DECLINED))
				);
		}
		
		public static Criterion ofClosed() {
			return Restrictions.or(
					Restrictions.eq("status", PullRequest.Status.INTEGRATED),
					Restrictions.eq("status", PullRequest.Status.DECLINED)
				);
		}
		
		public static Criterion ofTarget(Branch target) {
			return Restrictions.eq("target", target);
		}

		public static Criterion ofSource(Branch source) {
			return Restrictions.eq("source", source);
		}
		
		public static Criterion ofSubmitter(User submitter) {
			return Restrictions.eq("submitter", submitter);
		}
		
	}

}
