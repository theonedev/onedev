package com.pmease.gitplex.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.Preconditions;
import com.pmease.commons.git.Change;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.jackson.ExternalView;
import com.pmease.commons.util.LockUtils;
import com.pmease.commons.util.Triple;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.ChangeComments;
import com.pmease.gitplex.core.gatekeeper.checkresult.Blocking;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.checkresult.Failed;
import com.pmease.gitplex.core.gatekeeper.checkresult.Pending;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.ReviewManager;
import com.pmease.gitplex.core.permission.ObjectPermission;

@SuppressWarnings("serial")
@Entity
public class PullRequest extends AbstractEntity {

	public enum CloseStatus {INTEGRATED, DISCARDED};
	
	public enum Status {
		PENDING_APPROVAL("Pending Approval"), 
		PENDING_UPDATE("Pending Update"), PENDING_INTEGRATE("Pending Integration"), 
		INTEGRATED("Integrated"), DISCARDED("Discarded");

		private final String displayName;
		
		Status(String displayName) {
			this.displayName = displayName;
		}
		
		@Override
		public String toString() {
			return displayName;
		}
		
	}
	
	public enum IntegrationStrategy {
		MERGE_ALWAYS("Merge always", 
				"Always create merge commit when integrate into target branch"), 
		MERGE_IF_NECESSARY("Merge if necessary", 
				"Create merge commit only if target branch can not be fast-forwarded to the pull request"), 
		MERGE_WITH_SQUASH("Merge with squash", 
				"Squash all commits in the pull request and then merge with target branch"),
		REBASE_SOURCE_ONTO_TARGET("Rebase source onto target", 
				"Rebase source branch onto target branch and then fast-forward target branch to source branch"), 
		REBASE_TARGET_ONTO_SOURCE("Rebase target onto source", 
				"Rebase target branch onto source branch");

		private final String displayName;
		
		private final String description;
		
		IntegrationStrategy(String displayName, String description) {
			this.displayName = displayName;
			this.description = description;
		}
		
		public String getDisplayName() {
			return displayName;
		}

		public String getDescription() {
			return description;
		}

		@Override
		public String toString() {
			return displayName;
		}

	}
	
	public enum Event {OPENED, REVIEWED, VERIFIED, UPDATED, COMMENTED, INTEGRATED, DISCARDED, REOPENED};
	
	@Column
	private CloseStatus closeStatus;

	@Column(nullable=false)
	private String title;
	
	@Column(length=65535)
	private String description;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User submitter;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Branch target;

	@ManyToOne(fetch=FetchType.LAZY)
	private Branch source;
	
	// record name of source branch so that we can restore it even after source branch is deleted
	private String sourceFQN;
	
	@Column(nullable=false)
	private String baseCommitHash;

	@ManyToOne(fetch=FetchType.LAZY)
	private User assignee;
	
	@Transient
	private Git sandbox;
	
	@Embedded
	private IntegrationPreview lastIntegrationPreview;
	
	@Column(nullable=false)
	private Date createDate = new Date();
	
	@Column(nullable=false)
	private Date lastEventDate = new Date();
	
	@Column(nullable=false)
	private Event lastEvent = Event.OPENED;
	
	@Column(nullable=false)
	private IntegrationStrategy integrationStrategy;

	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestUpdate> updates = new ArrayList<>();

	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<ReviewInvitation> reviewInvitations = new ArrayList<>();
	
	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestVerification> verifications = new ArrayList<>();

	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestComment> comments = new ArrayList<>();

	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestActivity> activities = new ArrayList<>();
	
	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestNotification> notifications = new ArrayList<>();
	
	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestVisit> visits = new ArrayList<>();

	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestWatch> watches = new ArrayList<>();
	
	private transient CheckResult checkResult;

	private transient List<PullRequestUpdate> sortedUpdates;
	
	private transient List<PullRequestUpdate> effectiveUpdates;

	private transient PullRequestUpdate referentialUpdate;
	
	private transient Set<String> pendingCommits;
	
	private transient Collection<Commit> mergedCommits;
	
	private transient Map<ChangeKey, ChangeComments> commentsCache = new HashMap<>();
	
	private transient Collection<PullRequestCommentReply> commentReplies;
	
	private transient List<Review> reviews;
	
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
	 * Get the user responsible for integration of the pull request.
	 * 
	 * @return
	 * 			the user responsible for integration of this pull request, or <tt>null</tt> to have the 
	 * 			system integrate the pull request automatically
	 */
	@Nullable
	public User getAssignee() {
		return assignee;
	}

	public void setAssignee(User assignee) {
		this.assignee = assignee;
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

	@Nullable
	public String getSourceFQN() {
		return sourceFQN;
	}

	public void setSourceFQN(String sourceFQN) {
		this.sourceFQN = sourceFQN;
	}

	public String getBaseCommitHash() {
		return baseCommitHash;
	}

	public void setBaseCommitHash(String baseCommitHash) {
		this.baseCommitHash = baseCommitHash;
	}
	
	public Commit getBaseCommit() {
		return getTarget().getRepository().getCommit(getBaseCommitHash());
	}
	
	public Git git() {
		if (sandbox == null)
			return getTarget().getRepository().git();
		else
			return sandbox;
	}

	public Git getSandbox() {
		return sandbox;
	}

	public void setSandbox(Git sandbox) {
		this.sandbox = sandbox;
	}

	/**
	 * Get unmodifiable collection of updates of this pull request. To add update 
	 * to the pull request, call {@link this#addUpdate(PullRequestUpdate)} instead.
	 * 
	 * @return
	 * 			unmodifiable collection of updates
	 */
	public Collection<PullRequestUpdate> getUpdates() {
		return Collections.unmodifiableCollection(updates);
	}
	
	public void setUpdates(Collection<PullRequestUpdate> updates) {
		this.updates = updates;
		sortedUpdates = null;
		effectiveUpdates = null;
	}

	public void addUpdate(PullRequestUpdate update) {
		updates.add(update);
		sortedUpdates = null;
		effectiveUpdates = null;
	}

	public Collection<ReviewInvitation> getReviewInvitations() {
		return reviewInvitations;
	}

	public void setReviewInvitations(Collection<ReviewInvitation> reviewInvitations) {
		this.reviewInvitations = reviewInvitations;
	}

	public Collection<PullRequestVerification> getVerifications() {
		return verifications;
	}

	public void setVerifications(Collection<PullRequestVerification> verifications) {
		this.verifications = verifications;
	}

	public Collection<PullRequestComment> getComments() {
		return comments;
	}

	public void setComments(Collection<PullRequestComment> comments) {
		this.comments = comments;
	}

	public Collection<PullRequestActivity> getActivities() {
		return activities;
	}

	public void setActivities(Collection<PullRequestActivity> activities) {
		this.activities = activities;
	}

	public Collection<PullRequestNotification> getNotifications() {
		return notifications;
	}

	public void setNotifications(Collection<PullRequestNotification> notifications) {
		this.notifications = notifications;
	}

	public Collection<PullRequestVisit> getVisits() {
		return visits;
	}

	public void setVisits(Collection<PullRequestVisit> visits) {
		this.visits = visits;
	}

	public Collection<PullRequestWatch> getWatches() {
		return watches;
	}

	public void setWatches(Collection<PullRequestWatch> watches) {
		this.watches = watches;
	}

	public Status getStatus() {
		if (closeStatus == CloseStatus.INTEGRATED) 
			return Status.INTEGRATED;
		else if (closeStatus == CloseStatus.DISCARDED) 
			return Status.DISCARDED;
		else if (getCheckResult() instanceof Pending || getCheckResult() instanceof Blocking) 
			return Status.PENDING_APPROVAL;
		else if (getCheckResult() instanceof Failed) 
			return Status.PENDING_UPDATE;
		else  
			return Status.PENDING_INTEGRATE;
	}

	@Nullable
	public CloseStatus getCloseStatus() {
		return closeStatus;
	}

	public void setCloseStatus(@Nullable CloseStatus closeStatus) {
		this.closeStatus = closeStatus;
	}

	public boolean isOpen() {
		return closeStatus == null;
	}
	
	public PullRequestUpdate getReferentialUpdate() {
		if (referentialUpdate != null) {
			return referentialUpdate;
		} else {
			return getEffectiveUpdates().get(0);
		}
	}

	public void setReferentialUpdate(PullRequestUpdate referentiralUpdate) {
		this.referentialUpdate = referentiralUpdate;
	}

	/**
	 * Get gate keeper check result.
	 *  
	 * @return
	 * 			check result of this pull request has not been refreshed yet
	 */
	public CheckResult getCheckResult() {
		if (checkResult == null) 
			checkResult = getTarget().getRepository().getGateKeeper().checkRequest(this);
		return checkResult;
	}
	
	/**
	 * Get last integration preview of this pull request. Note that this method may return an 
	 * out dated integration preview. Refer to {@link this#getIntegrationPreview()}
	 * if you'd like to get an update-to-date integration preview
	 *  
	 * @return
	 * 			integration preview of this pull request, or <tt>null</tt> if integration 
	 * 			preview has not been calculated yet. 
	 */
	@Nullable
	public IntegrationPreview getLastIntegrationPreview() {
		return lastIntegrationPreview;
	}
	
	public void setLastIntegrationPreview(IntegrationPreview lastIntegrationPreview) {
		this.lastIntegrationPreview = lastIntegrationPreview;
	}

	/**
	 * Get effective integration preview of this pull request.
	 * 
	 * @return
	 * 			update to date integration preview of this pull request, or <tt>null</tt> if 
	 * 			the integration preview has not been calculated or out dated. In both cases, 
	 * 			it will trigger a re-calculation, and client should call this method later 
	 * 			to get the calculated result 
	 */
	@JsonView(ExternalView.class)
	@Nullable
	public IntegrationPreview getIntegrationPreview() {
		return GitPlex.getInstance(PullRequestManager.class).previewIntegration(this);
	}
	
	/**
	 * Get list of sorted updates.
	 * 
	 * @return 
	 * 			list of sorted updates ordered by id
	 */
	public List<PullRequestUpdate> getSortedUpdates() {
		if (sortedUpdates == null) {
			Preconditions.checkState(updates.size() >= 1);
			sortedUpdates = new ArrayList<PullRequestUpdate>(updates);
			Collections.sort(sortedUpdates);
		}
		return sortedUpdates;
	}

	/**
	 * Get list of effective updates reversely sorted by id. Update is considered effective if it is 
	 * not ancestor of target branch head.
	 * 
	 * @return 
	 * 			list of effective updates reversely sorted by id
	 */
	public List<PullRequestUpdate> getEffectiveUpdates() {
		if (effectiveUpdates == null) {
			effectiveUpdates = new ArrayList<PullRequestUpdate>();

			Git git = getTarget().getRepository().git();
			for (int i=getSortedUpdates().size()-1; i>=0; i--) {
				PullRequestUpdate update = getSortedUpdates().get(i);
				if (!git.isAncestor(update.getHeadCommitHash(), getTarget().getHeadCommitHash()))
					effectiveUpdates.add(update);
				else 
					break;
			}
			
			Preconditions.checkState(!effectiveUpdates.isEmpty());
		}
		return effectiveUpdates;
	}

	public IntegrationStrategy getIntegrationStrategy() {
		return integrationStrategy;
	}

	public void setIntegrationStrategy(IntegrationStrategy integrationStrategy) {
		this.integrationStrategy = integrationStrategy;
	}

	@JsonView(ExternalView.class)
	public PullRequestUpdate getLatestUpdate() {
		return getSortedUpdates().get(getSortedUpdates().size()-1);
	}
	
	public Collection<String> findTouchedFiles() {
		Git git = getTarget().getRepository().git();
		return git.listChangedFiles(getTarget().getHeadCommitHash(), getLatestUpdate().getHeadCommitHash(), null);
	}

	@JsonView(ExternalView.class)
	public String getBaseRef() {
		Preconditions.checkNotNull(getId());
		return Repository.REFS_GITPLEX + "pulls/" + getId() + "/base";
	}

	@JsonView(ExternalView.class)
	public String getIntegrateRef() {
		Preconditions.checkNotNull(getId());
		return Repository.REFS_GITPLEX + "pulls/" + getId() + "/integrate";
	}

	/**
	 * Delete refs of this pull request, without touching refs of its updates.
	 */
	public void deleteRefs() {
		Git git = getTarget().getRepository().git();
		git.deleteRef(getBaseRef(), null, null);
		git.deleteRef(getIntegrateRef(), null, null);
	}
	
    public <T> T lockAndCall(Callable<T> callable) {
		Preconditions.checkNotNull(getId());
		
    	return LockUtils.call("pull request: " + getId(), callable);
    }
	
	/**
	 * Invite specified number of users in candidates to review this request.
	 * <p>
	 * 
	 * @param candidates 
	 * 			a collection of users to invite users from
	 * @param count 
	 * 			number of users to invite
	 */
	public void pickReviewers(Collection<User> candidates, int count) {
		List<User> pickList = new ArrayList<User>(candidates);

		/*
		 * users already reviewed since base update should be excluded from
		 * invitation list as their reviews are still valid
		 */
		for (Review review: getReferentialUpdate().listReviewsOnwards())
			pickList.remove(review.getReviewer());

		final Map<User, Date> firstChoices = new HashMap<>();
		final Map<User, Date> secondChoices = new HashMap<>();
		
		for (ReviewInvitation invitation: getReviewInvitations()) {
			if (invitation.isPreferred())
				firstChoices.put(invitation.getReviewer(), invitation.getDate());
			else
				secondChoices.put(invitation.getReviewer(), invitation.getDate());
		}
		
		// submitter is not preferred
		secondChoices.put(getSubmitter(), new Date());
		
		/* Follow below rules to pick reviewers:
		 * 1. If user is excluded previously, it will be considered last.
		 * 2. If user is already a reviewer, it will be considered first.
		 * 3. Otherwise pick user with least reviews.
		 */
		Collections.sort(pickList, new Comparator<User>() {

			@Override
			public int compare(User user1, User user2) {
				if (firstChoices.containsKey(user1)) {
					if (firstChoices.containsKey(user2)) 
						return user1.getReviewEffort() - user2.getReviewEffort();
					else
						return -1;
				} else if (firstChoices.containsKey(user2)) {
					return 1;
				} else if (secondChoices.containsKey(user1)) {
					if (secondChoices.containsKey(user2)) 
						return secondChoices.get(user1).compareTo(secondChoices.get(user2));
					else
						return 1;
				} else if (secondChoices.containsKey(user2)) {
					return -1;
				} else {
					return user1.getReviewEffort() - user2.getReviewEffort();
				}
			}
			
		});

		List<User> picked;
		if (count <= pickList.size())
			picked = pickList.subList(0, count);
		else
			picked = pickList;

		for (User user: picked) {
			boolean found = false;
			for (ReviewInvitation invitation: getReviewInvitations()) {
				if (invitation.getReviewer().equals(user)) {
					invitation.setDate(new Date());
					invitation.setPerferred(true);
					found = true;
				}
			}
			if (!found) {
				ReviewInvitation invitation = new ReviewInvitation();
				invitation.setRequest(this);
				invitation.setReviewer(user);
				getReviewInvitations().add(invitation);
			}
		}

	}
	
	public static class CriterionHelper {
		public static Criterion ofOpen() {
			return Restrictions.isNull("closeStatus");
		}
		
		public static Criterion ofClosed() {
			return Restrictions.isNotNull("closeStatus");
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

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getLastEventDate() {
		return lastEventDate;
	}

	public void setLastEventDate(Date lastEventDate) {
		this.lastEventDate = lastEventDate;
	}

	public Event getLastEvent() {
		return lastEvent;
	}

	public void setLastEvent(Event lastEvent) {
		this.lastEvent = lastEvent;
	}

	/**
	 * Get commits pending integration.
	 * 
	 * @return
	 * 			commits pending integration
	 */
	public Set<String> getPendingCommits() {
		if (pendingCommits == null) {
			pendingCommits = new HashSet<>();
			Repository repo = getTarget().getRepository();
			for (Commit commit: repo.git().log(getTarget().getHeadCommitHash(), getLatestUpdate().getHeadCommitHash(), null, 0, 0))
				pendingCommits.add(commit.getHash());
		}
		return pendingCommits;
	}

	public ChangeComments getChangeComments(Change change) {
		ChangeKey key = new ChangeKey(change.getOldRev(), change.getNewRev(), change.getPath());
		ChangeComments comments = commentsCache.get(key);
		if (comments == null) {
			comments = new ChangeComments(this, change);
			commentsCache.put(key, comments);
		}
		return comments;
	}
	
	public List<String> getCommentables() {
		List<String> commentables = new ArrayList<>();
		commentables.add(getBaseCommitHash());
		for (PullRequestUpdate update: getSortedUpdates())
			commentables.add(update.getHeadCommitHash());
		return commentables;
	}
	
	/**
	 * Merged commits represent commits already merged to target branch since base commit.
	 * 
	 * @return
	 * 			commits already merged to target branch since base commit
	 */
	public Collection<Commit> getMergedCommits() {
		if (mergedCommits == null) {
			mergedCommits = new HashSet<>();

			Branch target = getTarget();
			Repository repo = target.getRepository();
			for (Commit commit: repo.git().log(getBaseCommitHash(), target.getHeadCommitHash(), null, 0, 0))
				mergedCommits.add(commit);
		}
		return mergedCommits;
	}
	
	private Collection<PullRequestCommentReply> getCommentReplies() {
		if (commentReplies == null) {
			EntityCriteria<PullRequestCommentReply> criteria = EntityCriteria.of(PullRequestCommentReply.class);
			criteria.createCriteria("comment").add(Restrictions.eq("request", this));
			commentReplies = GitPlex.getInstance(Dao.class).query(criteria);
		}
		return commentReplies;
	}
	
	/**
	 * This method is here for performance consideration. Often we need to load replies of 
	 * different comments in a pull request. And we optimize it to load all replies at once 
	 * for all comments in a pull request to reduce SQL queries. 
	 *  
	 * @param comment
	 * @return
	 */
	public Collection<PullRequestCommentReply> getCommentReplies(PullRequestComment comment) {
		List<PullRequestCommentReply> replies = new ArrayList<>();
		for (PullRequestCommentReply reply: getCommentReplies()) {
			if (reply.getComment().equals(comment))
				replies.add(reply);
		}
		return replies;
	}
	
	public List<Review> getReviews() {
		if (reviews == null) { 
			if (!isNew())
				reviews = GitPlex.getInstance(ReviewManager.class).findBy(this);
			else
				reviews = new ArrayList<>();
		}
		return reviews;
	}
	
	/**
	 * This method is here for performance consideration. Often we need to load reviews of 
	 * different updates in a pull request. And we optimize it to load all reviews at once 
	 * for all updates in a pull request to reduce SQL queries. 
	 * 
	 * @param update
	 * @return
	 */
	public List<Review> getReviews(PullRequestUpdate update) {
		List<Review> reviews = new ArrayList<>();
		for (Review review: getReviews()) {
			if (review.getUpdate().equals(update))
				reviews.add(review);
		}
		return reviews;
	}

	public boolean isReviewEffective(User user) {
		for (Review review: getReviews()) {
			if (review.getUpdate().equals(getLatestUpdate()) && review.getReviewer().equals(user)) 
				return true;
		}
		return false;
	}

	/**
	 * Calculate list of users can be added as reviewers of this pull request.
	 *  
	 * @return
	 * 			list of potential reviewers
	 */
	public List<User> getPotentialReviewers() {
		List<User> reviewers = new ArrayList<>();
		Set<User> alreadyInvited = new HashSet<>();
		for (ReviewInvitation invitation: getReviewInvitations()) {
			if (invitation.isPreferred())
				alreadyInvited.add(invitation.getReviewer());
		}
		ObjectPermission readPerm = ObjectPermission.ofRepositoryRead(getTarget().getRepository());
		for (User user: GitPlex.getInstance(Dao.class).allOf(User.class)) {
			if (user.asSubject().isPermitted(readPerm) && !alreadyInvited.contains(user))
				reviewers.add(user);
		}
		return reviewers;
	}

	@Nullable
	public PullRequestWatch getWatch(User user) {
		for (PullRequestWatch watch: getWatches()) {
			if (watch.getUser().equals(user))
				return watch;
		}
		return null;
	}

	@Nullable
	public PullRequestVisit getVisit(User user) {
		for (PullRequestVisit visit: getVisits()) {
			if (visit.getUser().equals(user))
				return visit;
		}
		return null;
	}
	
	public String getUrl() {
		return GitPlex.getInstance().guessServerUrl() + "/" + getTarget().getRepository().getFQN() 
				+ "/pull_requests/" + getId() + "/overview";
	}
	
	private static class ChangeKey extends Triple<String, String, String> {

		public ChangeKey(String oldCommit, String newCommit, String file) {
			super(oldCommit, newCommit, file);
		}
		
	}
	
}
