package com.pmease.gitplex.core.manager.impl;

import static com.pmease.gitplex.core.model.PullRequest.CriterionHelper.ofOpen;
import static com.pmease.gitplex.core.model.PullRequest.CriterionHelper.ofSource;
import static com.pmease.gitplex.core.model.PullRequest.CriterionHelper.ofTarget;
import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.MERGE_ALWAYS;
import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.MERGE_IF_NECESSARY;
import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.MERGE_WITH_SQUASH;
import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.REBASE_SOURCE_ONTO_TARGET;
import static com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy.REBASE_TARGET_ONTO_SOURCE;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.command.MergeCommand.FastForwardMode;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.extensionpoint.PullRequestListener;
import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.NotificationManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.PullRequestUpdateManager;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.manager.ReviewInvitationManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.IntegrationPolicy;
import com.pmease.gitplex.core.model.IntegrationPreview;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequest.CloseStatus;
import com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy;
import com.pmease.gitplex.core.model.PullRequestActivity;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.ReviewInvitation;
import com.pmease.gitplex.core.model.User;

@Singleton
public class DefaultPullRequestManager implements PullRequestManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestManager.class);
	
	private final Dao dao;
	
	private final ConfigManager configManager;
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	private final BranchManager branchManager;
	
	private final StorageManager storageManager;
	
	private final UnitOfWork unitOfWork;
	
	private final Set<PullRequestListener> pullRequestListeners;
	
	private final ReviewInvitationManager reviewInvitationManager;
	
	private final NotificationManager notificationManager;
	
	private final Set<Long> integrationPreviewRequestIds = new ConcurrentHashSet<>();

	private ThreadPoolExecutor integrationPreviewExecutor;

	@SuppressWarnings("serial")
	private final BlockingDeque<Runnable> integrationPreviewQueue = new LinkedBlockingDeque<Runnable>() {

		@Override
		public boolean offer(Runnable e) {
			return super.offerFirst(e);
		}
		
	};

	@Inject
	public DefaultPullRequestManager(Dao dao, PullRequestUpdateManager pullRequestUpdateManager, 
			BranchManager branchManager, StorageManager storageManager, 
			ReviewInvitationManager reviewInvitationManager, 
			NotificationManager notificationManager, ConfigManager configManager,
			UnitOfWork unitOfWork, Set<PullRequestListener> pullRequestListeners) {
		this.dao = dao;
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.branchManager = branchManager;
		this.storageManager = storageManager;
		this.reviewInvitationManager = reviewInvitationManager;
		this.notificationManager = notificationManager;
		this.unitOfWork = unitOfWork;
		this.configManager = configManager;
		this.pullRequestListeners = pullRequestListeners;
	}

	@Sessional
	@Override
	public PullRequest findOpen(Branch target, Branch source) {
		return dao.find(EntityCriteria.of(PullRequest.class)
				.add(ofTarget(target))
				.add(ofSource(source))
				.add(ofOpen()));
	}

	@Transactional
	@Override
	public void delete(PullRequest request) {
		deleteRefs(request);
		
		dao.remove(request);
	}

	@Sessional
	@Override
	public void deleteRefs(PullRequest request) {
		for (PullRequestUpdate update : request.getUpdates())
			update.deleteRefs();
		
		request.deleteRefs();
	}

	@Sessional
	@Override
	public Collection<PullRequest> findOpen(Branch sourceOrTarget) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofOpen());
		criteria.add(Restrictions.or(ofSource(sourceOrTarget), ofTarget(sourceOrTarget)));
		return dao.query(criteria);
	}

	@Transactional
	@Override
	public void restoreSource(PullRequest request) {
		Preconditions.checkState(request.getSourceFQN() != null && request.getSource() == null && !request.isOpen());

		String repositoryFQN = Branch.getRepositoryFQNByFQN(request.getSourceFQN());
		Repository repository = GitPlex.getInstance(RepositoryManager.class).findBy(repositoryFQN);
		if (repository == null)
			throw new RuntimeException("Unable to find repository: " + repositoryFQN);
		String branchName = Branch.getNameByFQN(request.getSourceFQN());
		Branch branch = GitPlex.getInstance(BranchManager.class).findBy(repository, branchName);
		if (branch == null) {
			branch = new Branch();
			branch.setRepository(repository);
			branch.setName(branchName);
			branch.setHeadCommitHash(request.getLatestUpdate().getHeadCommitHash());
			branchManager.save(branch);
			repository.git().createBranch(branch.getName(), branch.getHeadCommitHash());
		}
		
		request.setSource(branch);
		dao.persist(request);
	}

	@Transactional
	@Override
	public void reopen(PullRequest request, User user, String comment) {
		Preconditions.checkState(!request.isOpen());
		
		request.setCloseStatus(null);
		dao.persist(request);
		
		PullRequestActivity activity = new PullRequestActivity();
		activity.setRequest(request);
		activity.setDate(new DateTime().minusSeconds(1).toDate());
		activity.setAction(PullRequestActivity.Action.REOPEN);
		activity.setUser(user);
		
		dao.persist(activity);

		if (comment != null) {
			PullRequestComment requestComment = new PullRequestComment();
			requestComment.setContent(comment);
			requestComment.setDate(activity.getDate());
			requestComment.setRequest(request);
			requestComment.setUser(user);
			dao.persist(requestComment);
		}

		onSourceBranchUpdate(request);
	}

	@Transactional
	@Override
 	public void discard(PullRequest request, final User user, final String comment) {
		PullRequestActivity activity = new PullRequestActivity();
		activity.setRequest(request);
		activity.setDate(new Date());
		activity.setAction(PullRequestActivity.Action.DISCARD);
		activity.setUser(user);
		
		dao.persist(activity);

		if (comment != null) {
			PullRequestComment requestComment = new PullRequestComment();
			requestComment.setContent(comment);
			requestComment.setDate(activity.getDate());
			requestComment.setRequest(request);
			requestComment.setUser(user);
			dao.persist(requestComment);
		}

		request.setCloseStatus(CloseStatus.DISCARDED);
		request.setUpdateDate(activity.getDate());
		dao.persist(request);
		
		for (PullRequestListener listener: pullRequestListeners)
			listener.onDiscarded(request);
	}
	
	@Transactional
	@Override
	public void integrate(PullRequest request, User user, String comment) {
		if (request.getStatus() != PullRequest.Status.PENDING_INTEGRATE)
			throw new IllegalStateException("Gate keeper disallows integration right now.");
	
		IntegrationPreview preview = request.getIntegrationPreview();
		if (preview == null)
			throw new IllegalStateException("Integration preview has not been calculated yet.");

		String integrated = preview.getIntegrated();
		if (integrated == null)
			throw new IllegalStateException("There are integration conflicts.");

		Git git = request.getTarget().getRepository().git();
		IntegrationStrategy strategy = request.getIntegrationStrategy();
		if ((strategy == MERGE_ALWAYS || strategy == MERGE_IF_NECESSARY || strategy == MERGE_WITH_SQUASH) 
				&& !preview.getIntegrated().equals(preview.getRequestHead()) && comment != null) {
			File tempDir = FileUtils.createTempDir();
			try {
				Git tempGit = new Git(tempDir);
				tempGit.clone(git.repoDir().getAbsolutePath(), false, true, true, request.getTarget().getName());
				tempGit.updateRef("HEAD", preview.getIntegrated(), null, null);
				tempGit.reset(null, null);
				
				tempGit.commit(comment, false, true);
				integrated = tempGit.parseRevision("HEAD", true);
				git.fetch(tempGit, "+HEAD:" + request.getIntegrateRef());									
				comment = null;
			} finally {
				FileUtils.deleteDir(tempDir);
			}
		}
		if (strategy == REBASE_SOURCE_ONTO_TARGET || strategy == MERGE_WITH_SQUASH) {
			Git sourceGit = request.getSource().getRepository().git();
			sourceGit.updateRef(request.getSource().getHeadRef(), integrated, preview.getRequestHead(), 
					"Pull request #" + request.getId());
			request.getSource().setHeadCommitHash(integrated);
			branchManager.save(request.getSource());
		}
		git.updateRef(request.getTarget().getHeadRef(), integrated, 
				preview.getTargetHead(), "Pull request #" + request.getId());
		request.getTarget().setHeadCommitHash(integrated);
		branchManager.save(request.getTarget());
		
		PullRequestActivity activity = new PullRequestActivity();
		activity.setRequest(request);
		activity.setDate(new Date());
		activity.setAction(PullRequestActivity.Action.INTEGRATE);
		activity.setUser(user);
		
		dao.persist(activity);

		if (comment != null) {
			PullRequestComment requestComment = new PullRequestComment();
			requestComment.setContent(comment);
			requestComment.setDate(activity.getDate());
			requestComment.setRequest(request);
			requestComment.setUser(user);
			dao.persist(requestComment);
		}

		request.setCloseStatus(CloseStatus.INTEGRATED);
		request.setUpdateDate(new Date());

		dao.persist(request);

		for (PullRequestListener listener: pullRequestListeners)
			listener.onIntegrated(request);
	}
	
	@Transactional
	@Override
	public void open(final PullRequest request, final Object listenerData) {
		dao.persist(request);

		FileUtils.cleanDir(storageManager.getCacheDir(request));
		
		request.git().updateRef(request.getBaseRef(), request.getBaseCommitHash(), null, null);
		
		for (PullRequestUpdate update: request.getUpdates()) {
			update.setDate(new Date(System.currentTimeMillis() + 1000));
			pullRequestUpdateManager.save(update);
		}

		for (ReviewInvitation invitation: request.getReviewInvitations())
			reviewInvitationManager.save(invitation);

		for (PullRequestListener listener: pullRequestListeners)
			listener.onOpened(request);
		
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				IntegrationPreviewTask task = new IntegrationPreviewTask(request.getId());
				integrationPreviewExecutor.remove(task);
				integrationPreviewExecutor.execute(task);
			}
			
		});
	}

	@Override
	public List<IntegrationStrategy> getApplicableIntegrationStrategies(PullRequest request) {
		List<IntegrationStrategy> strategies = null;
		for (IntegrationPolicy policy: request.getTarget().getRepository().getIntegrationPolicies()) {
			if (policy.getTargetBranches().matches(request.getTarget()) 
					&& policy.getSourceBranches().matches(request.getSource())) {
				strategies = policy.getIntegrationStrategies();
				break;
			}
		}
		if (strategies == null) 
			strategies = Lists.newArrayList(IntegrationStrategy.MERGE_ALWAYS);
		return strategies;
	}

	@Transactional
	@Override
	public void onAssigneeChange(PullRequest request) {
		dao.persist(request);
		for (PullRequestListener listener: pullRequestListeners)
			listener.onAssigned(request);
	}
	
	@Transactional
	@Override
	public void onTargetBranchUpdate(PullRequest request) {
		closeIfMerged(request);
		if (request.isOpen()) {
			IntegrationPreviewTask task = new IntegrationPreviewTask(request.getId());
			integrationPreviewExecutor.remove(task);
			integrationPreviewExecutor.execute(task);
		}
	}

	@Transactional
	private void closeIfMerged(PullRequest request) {
		Git git = request.getTarget().getRepository().git();
		if (git.isAncestor(request.getLatestUpdate().getHeadCommitHash(), request.getTarget().getHeadCommitHash())) {
			PullRequestActivity activity = new PullRequestActivity();
			activity.setRequest(request);
			activity.setAction(PullRequestActivity.Action.INTEGRATE);
			activity.setDate(new Date());
			dao.persist(activity);
			
			request.setLastIntegrationPreview(null);
			request.setCloseStatus(CloseStatus.INTEGRATED);
			request.setUpdateDate(new Date());
			
			dao.persist(request);
			
			for (PullRequestListener listener: pullRequestListeners)
				listener.onIntegrated(request);
		} 
	}

	@Transactional
	@Override
	public void onSourceBranchUpdate(PullRequest request) {
		if (request.getLatestUpdate().getHeadCommitHash().equals(request.getSource().getHeadCommitHash()))
			return;
		
		PullRequestUpdate update = new PullRequestUpdate();
		update.setRequest(request);
		update.setDate(new Date());
		update.setHeadCommitHash(request.getSource().getHeadCommitHash());
		
		request.getUpdates().add(update);
		pullRequestUpdateManager.save(update);

		final Long requestId = request.getId();
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				unitOfWork.asyncCall(new Runnable() {

					@Override
					public void run() {
						check(dao.load(PullRequest.class, requestId));
					}
					
				});
			}
			
		});
	}

	/**
	 * This method might take some time and is not key to pull request logic (even if we 
	 * did not call it, we can always call it later), so normally should be called in an 
	 * executor
	 */
	@Transactional
	@Override
	public void check(PullRequest request) {
		Date now = new Date();
		if (request.isOpen()) {
			closeIfMerged(request);
			if (request.isOpen()) {
				if (request.getStatus() == PullRequest.Status.PENDING_UPDATE) {
					notificationManager.notifyUpdate(request, false);
				} else if (request.getStatus() == PullRequest.Status.PENDING_INTEGRATE) {
					IntegrationPreview integrationPreview = request.getIntegrationPreview();
					if (integrationPreview != null) {
						if (integrationPreview.getIntegrated() != null) {
							if (request.getAssignee() != null)
								notificationManager.notifyIntegration(request);
							else 
								integrate(request, null, "Integrated automatically by system");
						}
					}
				} else if (request.getStatus() == PullRequest.Status.PENDING_APPROVAL) {
					notificationManager.pendingApproval(request);
					for (ReviewInvitation invitation: request.getReviewInvitations()) { 
						if (!invitation.getDate().before(now))
							reviewInvitationManager.save(invitation);
					}
				}
			}
		}
	}

	@Override
	public boolean canIntegrate(PullRequest request) {
		if (request.getStatus() != PullRequest.Status.PENDING_INTEGRATE) {
			return false;
		} else {
			IntegrationPreview integrationPreview = request.getIntegrationPreview();
			return integrationPreview != null && integrationPreview.getIntegrated() != null;
		}
	}

	@Sessional
	@Override
	public Collection<PullRequest> findOpenTo(Branch target, Repository source) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(Restrictions.eq("target", target));
		criteria.add(Restrictions.isNull("closeStatus"));
		criteria.createCriteria("source").add(Restrictions.eq("repository", source));
		return dao.query(criteria);
	}
	
	@Sessional
	@Override
	public Collection<PullRequest> findOpenFrom(Branch source, Repository target) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(Restrictions.eq("source", source));
		criteria.add(Restrictions.isNull("closeStatus"));
		criteria.createCriteria("target").add(Restrictions.eq("repository", target));
		return dao.query(criteria);
	}

	private int getIntegrationPreviewWorkers() {
		Integer workers = configManager.getQosSetting().getIntegrationPreviewWorkers();
		if (workers == null)
			workers = Runtime.getRuntime().availableProcessors();
		return workers;
	}

	@Override
	public void systemSettingChanged() {
	}

	@Override
	public void mailSettingChanged() {
	}

	@Override
	public void qosSettingChanged() {
		if (integrationPreviewExecutor != null) {
			int integrationPreviewWorkers = getIntegrationPreviewWorkers();
			integrationPreviewExecutor.setCorePoolSize(integrationPreviewWorkers);
			integrationPreviewExecutor.setMaximumPoolSize(integrationPreviewWorkers);
		}
	}

	@Override
	public void start() {
		int previewWorkers = getIntegrationPreviewWorkers();
		integrationPreviewExecutor = new ThreadPoolExecutor(previewWorkers, previewWorkers, 
				0L, TimeUnit.MILLISECONDS, integrationPreviewQueue);
	}

	@Override
	public void stop() {
		if (integrationPreviewExecutor != null)
			integrationPreviewExecutor.shutdown();
	}

	@Override
	public IntegrationPreview previewIntegration(PullRequest request) {
		IntegrationPreview preview = request.getLastIntegrationPreview();
		if (request.isOpen() && (preview == null || preview.isObsolete(request))) {
			IntegrationPreviewTask task = new IntegrationPreviewTask(request.getId());
			integrationPreviewExecutor.remove(task);
			integrationPreviewExecutor.execute(task);
			return null;
		} else {
			return preview;
		}
	}
	
	private class IntegrationPreviewTask implements Runnable {

		private final Long requestId;
		
		public IntegrationPreviewTask(Long requestId) {
			this.requestId = requestId;
		}
		
		@Override
		public void run() {
			unitOfWork.begin();
			try {
				if (!integrationPreviewRequestIds.contains(requestId)) {
					integrationPreviewRequestIds.add(requestId);
					try {
						PullRequest request = dao.load(PullRequest.class, requestId);
						IntegrationPreview preview = request.getLastIntegrationPreview();
						if (request.isOpen() && (preview == null || preview.isObsolete(request))) {
							String requestHead = request.getLatestUpdate().getHeadCommitHash();
							String targetHead = request.getTarget().getHeadCommitHash();
							Git git = request.getTarget().getRepository().git();
							preview = new IntegrationPreview(request.getTarget().getHeadCommitHash(), 
									request.getLatestUpdate().getHeadCommitHash(), request.getIntegrationStrategy(), null);
							request.setLastIntegrationPreview(preview);
							String integrateRef = request.getIntegrateRef();
							if (preview.getIntegrationStrategy() == MERGE_IF_NECESSARY && git.isAncestor(targetHead, requestHead)
									|| preview.getIntegrationStrategy() == MERGE_WITH_SQUASH && git.isAncestor(targetHead, requestHead)
											&& git.log(targetHead, requestHead, null, 0, 0).size() == 1) {
								preview.setIntegrated(requestHead);
								git.updateRef(integrateRef, requestHead, null, null);
							} else {
								File tempDir = FileUtils.createTempDir();
								try {
									Git tempGit = new Git(tempDir);
									tempGit.clone(git.repoDir().getAbsolutePath(), false, true, true, 
											request.getTarget().getName());
									
									String integrated;

									if (preview.getIntegrationStrategy() == REBASE_TARGET_ONTO_SOURCE) {
										tempGit.updateRef("HEAD", requestHead, null, null);
										tempGit.reset(null, null);
										List<String> cherries = tempGit.listCherries("HEAD", targetHead);
										integrated = tempGit.cherryPick(cherries.toArray(new String[cherries.size()]));
									} else {
										tempGit.updateRef("HEAD", targetHead, null, null);
										tempGit.reset(null, null);
										if (preview.getIntegrationStrategy() == REBASE_SOURCE_ONTO_TARGET) {
											List<String> cherries = tempGit.listCherries("HEAD", requestHead);
											integrated = tempGit.cherryPick(cherries.toArray(new String[cherries.size()]));
										} else if (preview.getIntegrationStrategy() == MERGE_WITH_SQUASH) {
											String commitMessage = request.getTitle() + "\n\n";
											if (request.getDescription() != null)
												commitMessage += request.getDescription() + "\n\n";
											commitMessage += "(squashed commit of pull request #" + request.getId() + ")\n";
											integrated = tempGit.squash(requestHead, null, null, commitMessage);
										} else {
											FastForwardMode fastForwardMode;
											if (preview.getIntegrationStrategy() == MERGE_ALWAYS)
												fastForwardMode = FastForwardMode.NO_FF;
											else 
												fastForwardMode = FastForwardMode.FF;
											String commitMessage = "Merge pull request #" + request.getId() 
													+ "\n\n" + request.getTitle() + "\n";
											integrated = tempGit.merge(requestHead, fastForwardMode, null, null, commitMessage);
										}
									}
									 
									if (integrated != null) {
										preview.setIntegrated(integrated);
										git.fetch(tempGit, "+HEAD:" + integrateRef);									
									} else {
										git.deleteRef(integrateRef, null, null);
									}
								} finally {
									FileUtils.deleteDir(tempDir);
								}
							}
							dao.persist(request);

							if (request.getStatus() == PullRequest.Status.PENDING_INTEGRATE 
									&& preview.getIntegrated() != null) {
								if (request.getAssignee() != null)
									notificationManager.notifyIntegration(request);
								else 
									integrate(request, null, "Integrated automatically by system");
							}
							
							for (PullRequestListener listener: pullRequestListeners)
								listener.onIntegrationPreviewCalculated(request);
						}
					} finally {
						integrationPreviewRequestIds.remove(requestId);
					}
				}
			} catch (Exception e) {
				logger.error("Error calculating integration preview of pull request #" + requestId, e);
			} finally {
				unitOfWork.end();
			}
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof IntegrationPreviewTask))
				return false;
			if (this == other)
				return true;
			IntegrationPreviewTask otherRunnable = (IntegrationPreviewTask) other;
			return new EqualsBuilder().append(requestId, otherRunnable.requestId).isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37).append(requestId).toHashCode();
		}
		
	}

}
