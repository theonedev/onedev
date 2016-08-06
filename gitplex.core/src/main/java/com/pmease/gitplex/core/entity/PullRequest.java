package com.pmease.gitplex.core.entity;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OptimisticLock;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.Preconditions;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.jackson.ExternalView;
import com.pmease.commons.lang.diff.WhitespaceOption;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.support.CloseInfo;
import com.pmease.gitplex.core.entity.support.CompareContext;
import com.pmease.gitplex.core.entity.support.DepotAndBranch;
import com.pmease.gitplex.core.entity.support.IntegrationPreview;
import com.pmease.gitplex.core.entity.support.LastEvent;
import com.pmease.gitplex.core.gatekeeper.checkresult.Blocking;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.checkresult.Failed;
import com.pmease.gitplex.core.gatekeeper.checkresult.Pending;
import com.pmease.gitplex.core.manager.CodeCommentRelationManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.PullRequestReviewManager;
import com.pmease.gitplex.core.manager.VisitInfoManager;
import com.pmease.gitplex.core.security.ObjectPermission;
import com.pmease.gitplex.core.security.SecurityUtils;

@Entity
/*
 * @DynamicUpdate annotation here along with various @OptimisticLock annotations
 * on certain fields tell Hibernate not to perform version check on those fields
 * which can be updated from background thread.
 */
@DynamicUpdate 
@Table(indexes={@Index(columnList="title"), @Index(columnList="uuid"), @Index(columnList="numberStr"), @Index(columnList="noSpaceTitle")},
		uniqueConstraints={@UniqueConstraint(columnNames={"g_targetdepot_id", "number"})})
public class PullRequest extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final int MAX_CODE_COMMENTS = 1000;
	 
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
	
	@Embedded
	private CloseInfo closeInfo;

	@Column(nullable=false)
	private String title;
	
	@Lob
	@Column(length=65535)
	private String description;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Account submitter;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Depot targetDepot;
	
	@Column(nullable=false)
	private String targetBranch;

	@ManyToOne(fetch=FetchType.LAZY)
	private Depot sourceDepot;
	
	@Column(nullable=false)
	private String sourceBranch;
	
	@Column(nullable=false)
	private String baseCommitHash;

	@ManyToOne(fetch=FetchType.LAZY)
	private Account assignee;

	@Embedded
	private LastEvent lastEvent;
	
	@Column(nullable=true)
	private Date lastCodeCommentEventDate;

	// used for number search in markdown editor
	@Column(nullable=false)
	private String numberStr;
	
	// used for title search in markdown editor
	@Column(nullable=false)
	private String noSpaceTitle;
	
	@Version
	private long version;
	
	@OptimisticLock(excluded=true)
	@Embedded
	private IntegrationPreview lastIntegrationPreview;
	
	@Column(nullable=false)
	private Date submitDate = new Date();
	
	@Column(nullable=false)
	private IntegrationStrategy integrationStrategy;
	
	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();
	
	private long number;

	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestUpdate> updates = new ArrayList<>();

	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<CodeCommentRelation> codeCommentRelations = new ArrayList<>();
	
	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestReviewInvitation> reviewInvitations = new ArrayList<>();
	
	@OneToMany(mappedBy="referenced", cascade=CascadeType.REMOVE)
	private Collection<PullRequestReference> referencedBy = new ArrayList<>();
	
	@OneToMany(mappedBy="referencedBy",cascade=CascadeType.REMOVE)
	private Collection<PullRequestReference> referenced = new ArrayList<>();
	
	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestVerification> verifications = new ArrayList<>();

	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestComment> comments = new ArrayList<>();

	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestStatusChange> statusChanges = new ArrayList<>();
	
	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestNotification> notifications = new ArrayList<>();
	
	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<PullRequestWatch> watches = new ArrayList<>();
	
	private transient CheckResult checkResult;

	private transient List<PullRequestUpdate> sortedUpdates;
	
	private transient List<PullRequestUpdate> effectiveUpdates;

	private transient PullRequestUpdate referentialUpdate;
	
	private transient Collection<RevCommit> pendingCommits;
	
	private transient Collection<RevCommit> mergedCommits;
	
	private transient List<PullRequestReview> reviews;
	
	private transient IntegrationPreview integrationPreview;
	
	private transient List<CodeComment> codeComments;
	
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
		noSpaceTitle = StringUtils.deleteWhitespace(title);
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
	 * 			the user submitting the pull request, or <tt>null</tt> if unknown
	 */
	@Nullable
	public Account getSubmitter() {
		return submitter;
	}

	public void setSubmitter(@Nullable Account submitter) {
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
	public Account getAssignee() {
		return assignee;
	}

	public void setAssignee(Account assignee) {
		this.assignee = assignee;
	}

	public Depot getTargetDepot() {
		return targetDepot;
	}
	
	public DepotAndBranch getTarget() {
		return new DepotAndBranch(getTargetDepot(), getTargetBranch());
	}
	
	public void setTarget(DepotAndBranch target) {
		setTargetDepot(target.getDepot());
		setTargetBranch(target.getBranch());
	}
	
	public void setSource(DepotAndBranch source) {
		setSourceDepot(source.getDepot());
		setSourceBranch(source.getBranch());
	}
	
	public void setTargetDepot(Depot targetDepot) {
		this.targetDepot = targetDepot;
	}

	public String getTargetBranch() {
		return targetBranch;
	}

	public void setTargetBranch(String targetBranch) {
		this.targetBranch = targetBranch;
	}

	@Nullable
	public Depot getSourceDepot() {
		return sourceDepot;
	}

	public void setSourceDepot(Depot sourceDepot) {
		this.sourceDepot = sourceDepot;
	}

	public String getSourceBranch() {
		return sourceBranch;
	}

	public void setSourceBranch(String sourceBranch) {
		this.sourceBranch = sourceBranch;
	}

	public DepotAndBranch getSource() {
		return new DepotAndBranch(getSourceDepot(), getSourceBranch());
	}
	
	public String getTargetRef() {
		return GitUtils.branch2ref(getTargetBranch());
	}
	
	public String getSourceRef() {
		return GitUtils.branch2ref(getSourceBranch());
	}
	
	public String getBaseCommitHash() {
		return baseCommitHash;
	}

	public void setBaseCommitHash(String baseCommitHash) {
		this.baseCommitHash = baseCommitHash;
	}
	
	public RevCommit getBaseCommit() {
		return getTargetDepot().getRevCommit(ObjectId.fromString(getBaseCommitHash()));
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

	public Collection<CodeCommentRelation> getCodeCommentRelations() {
		return codeCommentRelations;
	}

	public void setCodeCommentRelations(Collection<CodeCommentRelation> codeCommentRelations) {
		this.codeCommentRelations = codeCommentRelations;
	}

	public Collection<PullRequestReviewInvitation> getReviewInvitations() {
		return reviewInvitations;
	}

	public void setReviewInvitations(Collection<PullRequestReviewInvitation> reviewInvitations) {
		this.reviewInvitations = reviewInvitations;
	}

	public Collection<PullRequestVerification> getVerifications() {
		return verifications;
	}

	public void setVerifications(Collection<PullRequestVerification> verifications) {
		this.verifications = verifications;
	}

	public Collection<PullRequestReference> getReferencedBy() {
		return referencedBy;
	}

	public void setReferencedBy(Collection<PullRequestReference> referencedBy) {
		this.referencedBy = referencedBy;
	}

	public Collection<PullRequestReference> getReferenced() {
		return referenced;
	}

	public void setReferenced(Collection<PullRequestReference> referenced) {
		this.referenced = referenced;
	}

	public Collection<PullRequestComment> getComments() {
		return comments;
	}

	public void setComments(Collection<PullRequestComment> comments) {
		this.comments = comments;
	}

	public Collection<PullRequestStatusChange> getStatusChanges() {
		return statusChanges;
	}

	public void setStatusChanges(Collection<PullRequestStatusChange> statusChanges) {
		this.statusChanges = statusChanges;
	}

	public Collection<PullRequestNotification> getNotifications() {
		return notifications;
	}

	public void setNotifications(Collection<PullRequestNotification> notifications) {
		this.notifications = notifications;
	}

	public Collection<PullRequestWatch> getWatches() {
		return watches;
	}

	public void setWatches(Collection<PullRequestWatch> watches) {
		this.watches = watches;
	}

	public Status getStatus() {
		if (closeInfo != null) {
			if (closeInfo.getCloseStatus() == CloseInfo.Status.INTEGRATED) 
				return Status.INTEGRATED;
			else 
				return Status.DISCARDED;
		} else if (getCheckResult() instanceof Pending || getCheckResult() instanceof Blocking) 
			return Status.PENDING_APPROVAL;
		else if (getCheckResult() instanceof Failed) 
			return Status.PENDING_UPDATE;
		else  
			return Status.PENDING_INTEGRATE;
	}

	@Nullable
	public CloseInfo getCloseInfo() {
		return closeInfo;
	}

	public void setCloseInfo(CloseInfo closeInfo) {
		this.closeInfo = closeInfo;
	}

	public boolean isOpen() {
		return closeInfo == null;
	}
	
	public PullRequestUpdate getReferentialUpdate() {
		if (referentialUpdate != null) 
			return referentialUpdate;
		else 
			return getEffectiveUpdates().get(0);
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
			checkResult = getTargetDepot().getGateKeeper().checkRequest(this);
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
		if (integrationPreview == null)
			integrationPreview = GitPlex.getInstance(PullRequestManager.class).previewIntegration(this);
		return integrationPreview;
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
		Preconditions.checkState(isOpen(), "Can only be called while request is open");
		if (effectiveUpdates == null) {
			effectiveUpdates = new ArrayList<PullRequestUpdate>();

			for (int i=getSortedUpdates().size()-1; i>=0; i--) {
				PullRequestUpdate update = getSortedUpdates().get(i);
				if (!getTargetDepot().isMergedInto(update.getHeadCommitHash(), getTarget().getObjectName()))
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
	
	@JsonView(ExternalView.class)
	public String getBaseRef() {
		Preconditions.checkNotNull(getId());
		return Depot.REFS_GITPLEX + "pulls/" + getNumber() + "/base";
	}

	@JsonView(ExternalView.class)
	public String getIntegrateRef() {
		Preconditions.checkNotNull(getId());
		return Depot.REFS_GITPLEX + "pulls/" + getNumber() + "/integrate";
	}

	/**
	 * Delete refs of this pull request, without touching refs of its updates.
	 */
	public void deleteRefs() {
		GitUtils.deleteRef(getTargetDepot().updateRef(getBaseRef()));
		GitUtils.deleteRef(getTargetDepot().updateRef(getIntegrateRef()));
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
	public void pickReviewers(Collection<Account> candidates, int count) {
		List<Account> pickList = new ArrayList<Account>(candidates);

		/*
		 * users already reviewed since base update should be excluded from
		 * invitation list as their reviews are still valid
		 */
		for (PullRequestReview review: getReferentialUpdate().listReviewsOnwards())
			pickList.remove(review.getUser());

		final Map<Account, Date> firstChoices = new HashMap<>();
		final Map<Account, Date> secondChoices = new HashMap<>();
		
		for (PullRequestReviewInvitation invitation: getReviewInvitations()) {
			if (invitation.isPreferred())
				firstChoices.put(invitation.getUser(), invitation.getDate());
			else
				secondChoices.put(invitation.getUser(), invitation.getDate());
		}
		
		// submitter is not preferred
		if (getSubmitter() != null)
			secondChoices.put(getSubmitter(), new Date());
		
		/* Follow below rules to pick reviewers:
		 * 1. If user is excluded previously, it will be considered last.
		 * 2. If user is already a reviewer, it will be considered first.
		 * 3. Otherwise pick user with least reviews.
		 */
		pickList.sort((user1, user2) -> {
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
		});

		List<Account> picked;
		if (count <= pickList.size())
			picked = pickList.subList(0, count);
		else
			picked = pickList;

		for (Account user: picked) {
			boolean found = false;
			for (PullRequestReviewInvitation invitation: getReviewInvitations()) {
				if (invitation.getUser().equals(user)) {
					invitation.setDate(new Date());
					invitation.setPerferred(true);
					found = true;
				}
			}
			if (!found) {
				PullRequestReviewInvitation invitation = new PullRequestReviewInvitation();
				invitation.setRequest(this);
				invitation.setUser(user);
				getReviewInvitations().add(invitation);
			}
		}

	}
	
	public static class CriterionHelper {
		public static Criterion ofOpen() {
			return Restrictions.isNull("closeInfo");
		}
		
		public static Criterion ofClosed() {
			return Restrictions.isNotNull("closeInfo");
		}
		
		public static Criterion ofTarget(DepotAndBranch target) {
			return Restrictions.and(
					Restrictions.eq("targetDepot", target.getDepot()),
					Restrictions.eq("targetBranch", target.getBranch()));
		}

		public static Criterion ofTargetDepot(Depot target) {
			return Restrictions.eq("targetDepot", target);
		}
		
		public static Criterion ofSource(DepotAndBranch source) {
			return Restrictions.and(
					Restrictions.eq("sourceDepot", source.getDepot()),
					Restrictions.eq("sourceBranch", source.getBranch()));
		}
		
		public static Criterion ofSourceDepot(Depot source) {
			return Restrictions.eq("sourceDepot", source);
		}
		
		public static Criterion ofSubmitter(Account submitter) {
			return Restrictions.eq("submitter", submitter);
		}
		
	}

	public Date getSubmitDate() {
		return submitDate;
	}

	public void setSubmitDate(Date submitDate) {
		this.submitDate = submitDate;
	}

	/**
	 * Get commits pending integration.
	 * 
	 * @return
	 * 			commits pending integration
	 */
	public Collection<RevCommit> getPendingCommits() {
		if (pendingCommits == null) {
			pendingCommits = new HashSet<>();
			Depot depot = getTargetDepot();
			try (RevWalk revWalk = new RevWalk(depot.getRepository())) {
				revWalk.markStart(revWalk.parseCommit(ObjectId.fromString(getLatestUpdate().getHeadCommitHash())));
				revWalk.markUninteresting(revWalk.parseCommit(ObjectId.fromString(getTarget().getObjectName())));
				revWalk.forEach(c->pendingCommits.add(c));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return pendingCommits;
	}

	/**
	 * Merged commits represent commits already merged to target branch since base commit.
	 * 
	 * @return
	 * 			commits already merged to target branch since base commit
	 */
	public Collection<RevCommit> getMergedCommits() {
		if (mergedCommits == null) {
			mergedCommits = new HashSet<>();
			Depot depot = getTargetDepot();
			try (RevWalk revWalk = new RevWalk(depot.getRepository())) {
				revWalk.markStart(revWalk.parseCommit(ObjectId.fromString(getTarget().getObjectName())));
				revWalk.markUninteresting(revWalk.parseCommit(ObjectId.fromString(getBaseCommitHash())));
				revWalk.forEach(c->mergedCommits.add(c));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return mergedCommits;
	}
	
	public List<PullRequestReview> getReviews() {
		if (reviews == null) { 
			if (!isNew())
				reviews = GitPlex.getInstance(PullRequestReviewManager.class).findAll(this);
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
	public List<PullRequestReview> getReviews(PullRequestUpdate update) {
		List<PullRequestReview> reviews = new ArrayList<>();
		for (PullRequestReview review: getReviews()) {
			if (review.getUpdate().equals(update))
				reviews.add(review);
		}
		return reviews;
	}
	
	public boolean isReviewEffective(Account user) {
		for (PullRequestReview review: getReviews()) {
			if (review.getUpdate().equals(getLatestUpdate()) && review.getUser().equals(user)) 
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
	public List<Account> getPotentialReviewers() {
		List<Account> reviewers = new ArrayList<>();
		Set<Account> alreadyInvited = new HashSet<>();
		for (PullRequestReviewInvitation invitation: getReviewInvitations()) {
			if (invitation.isPreferred())
				alreadyInvited.add(invitation.getUser());
		}
		ObjectPermission readPerm = ObjectPermission.ofDepotRead(getTargetDepot());
		for (Account user: GitPlex.getInstance(Dao.class).findAll(Account.class)) {
			if (user.asSubject().isPermitted(readPerm) && !alreadyInvited.contains(user))
				reviewers.add(user);
		}
		return reviewers;
	}

	@Nullable
	public PullRequestWatch getWatch(Account user) {
		for (PullRequestWatch watch: getWatches()) {
			if (watch.getUser().equals(user))
				return watch;
		}
		return null;
	}

	public String getNumberStr() {
		return numberStr;
	}

	public String getNoSpaceTitle() {
		return noSpaceTitle;
	}

	public long getVersion() {
		return version;
	}

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
	}

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
		numberStr = String.valueOf(number);
	}

	public List<CodeComment> getCodeComments() {
		if (codeComments == null) {
			codeComments = GitPlex.getInstance(CodeCommentRelationManager.class).findAllCodeComments(this);
		}
		return codeComments;
	}
	
	@Nullable
	public ComparingInfo getRequestComparingInfo(CodeComment.ComparingInfo commentComparingInfo) {
		List<String> commits = new ArrayList<>();
		commits.add(getBaseCommitHash());
		for (PullRequestUpdate update: getSortedUpdates()) {
			commits.addAll(update.getCommits().stream().map(RevCommit::getName).collect(Collectors.toList()));
		}
		String commit = commentComparingInfo.getCommit();
		CompareContext compareContext = commentComparingInfo.getCompareContext();
		if (commit.equals(compareContext.getCompareCommit())) {
			int index = commits.indexOf(commit);
			if (index == 0) {
				return null;
			} else {
				return new ComparingInfo(commits.get(index-1), commit, 
						compareContext.getWhitespaceOption(), compareContext.getPathFilter());
			}
		} else {
			int commitIndex = commits.indexOf(commit);
			int compareCommitIndex = commits.indexOf(compareContext.getCompareCommit());
			if (commitIndex == -1 || compareCommitIndex == -1) {
				return null;
			} else if (compareContext.isLeftSide()) {
				if (compareCommitIndex < commitIndex) {
					return new ComparingInfo(compareContext.getCompareCommit(), commit, 
							compareContext.getWhitespaceOption(), compareContext.getPathFilter());
				} else {
					return null;
				}
			} else {
				if (commitIndex < compareCommitIndex) {
					return new ComparingInfo(commit, compareContext.getCompareCommit(), 
							compareContext.getWhitespaceOption(), compareContext.getPathFilter());
				} else {
					return null;
				}
			}
		} 
	}
	
	public List<RevCommit> getCommits() {
		List<RevCommit> commits = new ArrayList<>();
		getSortedUpdates().forEach(update->commits.addAll(update.getCommits()));
		return commits;
	}
	
	public String getCommitMessage() {
		String commitMessage = getTitle() + "\n\n";
		if (getDescription() != null)
			commitMessage += getDescription() + "\n\n";
		commitMessage += "This commit is created as result of integrating pull request #" + getNumber();
		return commitMessage;
	}
	
	public LastEvent getLastEvent() {
		return lastEvent;
	}

	public void setLastEvent(LastEvent lastEvent) {
		this.lastEvent = lastEvent;
	}

	public Date getLastCodeCommentEventDate() {
		return lastCodeCommentEventDate;
	}

	public void setLastCodeCommentEventDate(Date lastCodeCommentEventDate) {
		this.lastCodeCommentEventDate = lastCodeCommentEventDate;
	}

	public boolean isVisitedAfter(Date date) {
		Account user = SecurityUtils.getAccount();
		if (user != null) {
			Date visitDate = GitPlex.getInstance(VisitInfoManager.class).getVisitDate(user, this);
			return visitDate != null && visitDate.after(date);
		} else {
			return true;
		}
	}
	
	public boolean isMerged() {
		return getTargetDepot().isMergedInto(
				getLatestUpdate().getHeadCommitHash(), 
				getTarget().getObjectName());
	}
	
	public static class ComparingInfo implements Serializable {
		
		private static final long serialVersionUID = 1L;

		private final String oldCommit; 
		
		private final String newCommit;
		
		private final String pathFilter;
		
		private final WhitespaceOption whitespaceOption;
		
		public ComparingInfo(String oldCommit, String newCommit, WhitespaceOption whitespaceOption, @Nullable String pathFilter) {
			this.oldCommit = oldCommit;
			this.newCommit = newCommit;
			this.whitespaceOption = whitespaceOption;
			this.pathFilter = pathFilter;
		}

		public String getOldCommit() {
			return oldCommit;
		}

		public String getNewCommit() {
			return newCommit;
		}

		public String getPathFilter() {
			return pathFilter;
		}

		public WhitespaceOption getWhitespaceOption() {
			return whitespaceOption;
		}

	}
}
