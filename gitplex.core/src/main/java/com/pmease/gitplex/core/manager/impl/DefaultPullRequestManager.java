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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.command.MergeCommand.FastForwardMode;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.PullRequestUpdateManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.IntegrationPolicy;
import com.pmease.gitplex.core.model.IntegrationPreview;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequest.CloseStatus;
import com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy;
import com.pmease.gitplex.core.model.PullRequestAudit;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestOperation;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.User;

@Singleton
public class DefaultPullRequestManager implements PullRequestManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestManager.class);
	
	private final Dao dao;
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	private final BranchManager branchManager;
	
	private final StorageManager storageManager;
	
	private final ExecutorService executorService;
	
	private final UnitOfWork unitOfWork;
	
	private final Map<Long, Future<Void>> integrationFutures = new ConcurrentHashMap<>();
	
	@Inject
	public DefaultPullRequestManager(Dao dao, PullRequestUpdateManager pullRequestUpdateManager, 
			BranchManager branchManager, StorageManager storageManager, 
			ExecutorService executorService, UnitOfWork unitOfWork) {
		this.dao = dao;
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.branchManager = branchManager;
		this.storageManager = storageManager;
		this.executorService = executorService;
		this.unitOfWork = unitOfWork;
	}

	@Sessional
	@Override
	public PullRequest findOpen(Branch target, Branch source) {
		return dao.find(EntityCriteria.of(PullRequest.class)
				.add(ofOpen())
				.add(ofTarget(target))
				.add(ofSource(source))
				.addOrder(Order.desc("id")));
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

	@Transactional
	@Override
 	public void discard(final PullRequest request, final User user, final String comment) {
		PullRequestAudit audit = new PullRequestAudit();
		audit.setRequest(request);
		audit.setDate(new Date());
		audit.setOperation(PullRequestOperation.DISCARD);
		audit.setUser(user);
		
		dao.persist(audit);

		if (comment != null) {
			PullRequestComment requestComment = new PullRequestComment();
			requestComment.setContent(comment);
			requestComment.setDate(audit.getDate());
			requestComment.setRequest(request);
			requestComment.setUser(user);
			dao.persist(requestComment);
		}

		request.setCloseStatus(CloseStatus.DISCARDED);
		request.setUpdateDate(audit.getDate());
		dao.persist(request);
		
		deleteRefsUponClose(request);
	}
	
	@Transactional
	@Override
	public void integrate(PullRequest request, User user, String comment) {
		if (request.getStatus() != PullRequest.Status.PENDING_INTEGRATE)
			throw new IllegalStateException("Gate keeper disallows integration right now.");
	
		IntegrationPreview preview = previewIntegration(request);
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
			if (sourceGit.updateRef(request.getSource().getHeadRef(), 
					integrated, preview.getRequestHead(), 
					"Pull request #" + request.getId())) {
				request.getSource().setHeadCommitHash(integrated);
				request.getSource().setUpdater(user);
				branchManager.save(request.getSource());

				PullRequestUpdate update = new PullRequestUpdate();
				update.setRequest(request);
				update.setDate(new Date());
				update.setUser(user);
				update.setHeadCommitHash(integrated);
				request.getUpdates().add(update);

				pullRequestUpdateManager.save(update);
			} else {
				throw new RuntimeException(String.format(
						"Unable to target branch '%s' due to lock failure.", request.getTarget()));
			}
		}
		if (git.updateRef(request.getTarget().getHeadRef(), integrated, 
				preview.getTargetHead(), "Pull request #" + request.getId())) {
			request.getTarget().setHeadCommitHash(integrated);
			request.getTarget().setUpdater(user);
			branchManager.save(request.getTarget());
			
			PullRequestAudit audit = new PullRequestAudit();
			audit.setRequest(request);
			audit.setDate(new Date());
			audit.setOperation(PullRequestOperation.INTEGRATE);
			audit.setUser(user);
			
			dao.persist(audit);

			if (comment != null) {
				PullRequestComment requestComment = new PullRequestComment();
				requestComment.setContent(comment);
				requestComment.setDate(audit.getDate());
				requestComment.setRequest(request);
				requestComment.setUser(user);
				dao.persist(requestComment);
			}

			request.setCloseStatus(CloseStatus.INTEGRATED);
			request.setUpdateDate(new Date());

			dao.persist(request);

			deleteRefsUponClose(request);
		} else {
			throw new RuntimeException(String.format(
					"Unable to target branch '%s' due to lock failure.", request.getTarget()));
		}
	}
	
	@Sessional
	@Override
	public List<PullRequest> findByCommit(String commit) {
		return dao.query(EntityCriteria.of(PullRequest.class)
				.add(Restrictions.or(
						Restrictions.eq("integrationPreview.requestHead", commit), 
						Restrictions.eq("integrationPreview.integrated", commit))), 0, 0);
	}

	@Transactional
	@Override
	public void send(PullRequest request) {
		dao.persist(request);

		FileUtils.cleanDir(storageManager.getCacheDir(request));
		
		request.git().updateRef(request.getBaseRef(), request.getBaseCommitHash(), null, null);
		
		for (PullRequestUpdate update: request.getUpdates()) {
			update.setDate(new Date(System.currentTimeMillis() + 1000));
			pullRequestUpdateManager.save(update);
		}
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

	private void deleteRefsUponClose(PullRequest request) {
		Git git = request.getTarget().getRepository().git();
		for (PullRequestUpdate update: request.getUpdates()) 
			git.deleteRef(update.getReferentialRef(), null, null);
		git.deleteRef(request.getIntegrateRef(), null, null);
	}

	@Transactional
	@Override
	public void onTargetBranchUpdate(PullRequest request) {
		Git git = request.getTarget().getRepository().git();
		if (git.isAncestor(request.getLatestUpdate().getHeadCommitHash(), request.getTarget().getHeadCommitHash())) {
			PullRequestAudit audit = new PullRequestAudit();
			audit.setRequest(request);
			audit.setOperation(PullRequestOperation.INTEGRATE);
			audit.setDate(new Date());
			audit.setUser(request.getTarget().getUpdater());
			dao.persist(audit);
			
			request.setIntegrationPreview(null);
			request.setCloseStatus(CloseStatus.INTEGRATED);
			request.setUpdateDate(new Date());
			
			dao.persist(request);
		} 
	}

	@Transactional
	@Override
	public void onSourceBranchUpdate(PullRequest request) {
		PullRequestUpdate update = new PullRequestUpdate();
		update.setRequest(request);
		update.setUser(request.getSource().getUpdater());
		update.setDate(new Date());
		update.setHeadCommitHash(request.getSource().getHeadCommitHash());
		
		request.getUpdates().add(update);
		pullRequestUpdateManager.save(update);
	}

	@Transactional
	@Override
	public void onGateKeeperUpdate(PullRequest request) {
		if (request.isAutoIntegrate() && canIntegrate(request)) 
			integrate(request, null, "Integrated automatically by system");
	}

	@Override
	public IntegrationPreview previewIntegration(PullRequest request) {
		IntegrationPreview preview = request.getIntegrationPreview();
		if (!request.isOpen() || preview != null 
				&& preview.getRequestHead().equals(request.getLatestUpdate().getHeadCommitHash())
				&& preview.getTargetHead().equals(request.getTarget().getHeadCommitHash())
				&& preview.getIntegrationStrategy() == request.getIntegrationStrategy()
				&& (preview.getIntegrated() == null || preview.getIntegrated().equals(request.getTarget().getRepository().getRefValue(request.getIntegrateRef())))) {
			return preview;
		} else {
			final Long requestId = request.getId();
			if (!integrationFutures.containsKey(requestId)) {
				integrationFutures.put(request.getId(), executorService.submit(new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						unitOfWork.begin();
						try {
							PullRequest request = dao.load(PullRequest.class, requestId);
							String requestHead = request.getLatestUpdate().getHeadCommitHash();
							String targetHead = request.getTarget().getHeadCommitHash();
							Git git = request.getTarget().getRepository().git();
							IntegrationPreview preview = new IntegrationPreview(request.getTarget().getHeadCommitHash(), 
									request.getLatestUpdate().getHeadCommitHash(), request.getIntegrationStrategy(), null);
							request.setIntegrationPreview(preview);
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
										integrated = tempGit.cherryPick(".." + targetHead);
									} else {
										tempGit.updateRef("HEAD", targetHead, null, null);
										tempGit.reset(null, null);
										if (preview.getIntegrationStrategy() == REBASE_SOURCE_ONTO_TARGET) {
											integrated = tempGit.cherryPick(".." + requestHead);
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
							
							onGateKeeperUpdate(request);
						} catch (Exception e) {
							logger.error("Error previewing integration of pull request #" + requestId, e);
						} finally {
							integrationFutures.remove(requestId);
							unitOfWork.end();
						}
						return null;
					}
					
				}));
			}
			return null;
		}
	}

	@Override
	public boolean canIntegrate(PullRequest request) {
		if (request.getStatus() != PullRequest.Status.PENDING_INTEGRATE) {
			return false;
		} else {
			IntegrationPreview integrationPreview = previewIntegration(request);
			return integrationPreview != null && integrationPreview.getIntegrated() != null;
		}
	}
	
}
