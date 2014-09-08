package com.pmease.gitplex.core.manager.impl;

import static com.pmease.gitplex.core.model.IntegrationStrategy.MERGE_ALWAYS;
import static com.pmease.gitplex.core.model.IntegrationStrategy.MERGE_IF_NECESSARY;
import static com.pmease.gitplex.core.model.IntegrationStrategy.REBASE_SOURCE;
import static com.pmease.gitplex.core.model.IntegrationStrategy.REBASE_TARGET;
import static com.pmease.gitplex.core.model.PullRequest.CriterionHelper.ofOpen;
import static com.pmease.gitplex.core.model.PullRequest.CriterionHelper.ofSource;
import static com.pmease.gitplex.core.model.PullRequest.CriterionHelper.ofTarget;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.command.MergeCommand.FastForwardMode;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitplex.core.gatekeeper.checkresult.Approved;
import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.PullRequestUpdateManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.BranchStrategy;
import com.pmease.gitplex.core.model.IntegrationInfo;
import com.pmease.gitplex.core.model.IntegrationSetting;
import com.pmease.gitplex.core.model.IntegrationStrategy;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequest.CloseStatus;
import com.pmease.gitplex.core.model.PullRequestAction;
import com.pmease.gitplex.core.model.PullRequestAudit;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.model.VoteInvitation;

@Singleton
public class DefaultPullRequestManager implements PullRequestManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestManager.class);
	
	private final Dao dao;
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	private final BranchManager branchManager;
	
	@Inject
	public DefaultPullRequestManager(Dao dao, PullRequestUpdateManager pullRequestUpdateManager, 
			BranchManager branchManager) {
		this.dao = dao;
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.branchManager = branchManager;
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
	
	/**
	 * Refresh internal state of this pull request. Pull request should be refreshed when:
	 * 
	 * <li> It is updated 
	 * <li> Head of target branch changes
	 * <li> Some one vote against it
	 * <li> CI system reports completion of build against relevant commits
	 */
	@Transactional
	public void refresh(final PullRequest request) {
		request.lockAndCall(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				Git git = request.getTarget().getRepository().git();
				String branchHead = request.getTarget().getHeadCommit();
				String requestHead = request.getLatestUpdate().getHeadCommit();
				
				String integrateRef = request.getIntegrateRef();
				
				if (git.isAncestor(requestHead, branchHead)) {
					PullRequestAudit audit = new PullRequestAudit();
					audit.setRequest(request);
					audit.setAction(new PullRequestAction.Integrate(
							"Mark as integrated as target branch already contains the head commit."));
					audit.setDate(new Date());
					audit.setUser(request.getTarget().getUpdater());
					dao.persist(audit);
					
					request.setCloseStatus(CloseStatus.INTEGRATED);
					request.setUpdateDate(new Date());
					request.setCheckResult(new Approved("Already integrated."));
					request.setIntegrationInfo(new IntegrationInfo(branchHead, requestHead, branchHead, null, true));
				} else {
					// Update head ref so that it can be pulled by build system
					git.updateRef(request.getHeadRef(), requestHead, null, null);
					
					IntegrationStrategy strategy = getIntegrationStrategy(request);
					
					if (request.getIntegrationInfo() == null 
							|| !branchHead.equals(request.getIntegrationInfo().getBranchHead())
							|| !requestHead.equals(request.getIntegrationInfo().getRequestHead())
							|| strategy != request.getIntegrationInfo().getIntegrationStrategy()
							|| request.getIntegrationInfo().getIntegrationHead() != null 
									&& !request.getIntegrationInfo().getIntegrationHead().equals(git.parseRevision(integrateRef, false))) {
						
						if (strategy == MERGE_IF_NECESSARY && git.isAncestor(branchHead, requestHead)) {
							request.setIntegrationInfo(new IntegrationInfo(branchHead, requestHead, requestHead, strategy, false));
							git.updateRef(integrateRef, requestHead, null, null);
						} else {
							File tempDir = FileUtils.createTempDir();
							try {
								Git tempGit = new Git(tempDir);
								tempGit.clone(git.repoDir().getAbsolutePath(), false, true, true, 
										request.getTarget().getName());
								
								String integrateHead;

								if (strategy == REBASE_TARGET) {
									tempGit.updateRef("HEAD", requestHead, null, null);
									tempGit.reset(null, null);
									integrateHead = tempGit.cherryPick(".." + branchHead);
								} else {
									tempGit.updateRef("HEAD", branchHead, null, null);
									tempGit.reset(null, null);
									if (strategy == REBASE_SOURCE) {
										integrateHead = tempGit.cherryPick(".." + requestHead);
									} else {
										FastForwardMode fastForwardMode;
										if (strategy == MERGE_ALWAYS)
											fastForwardMode = FastForwardMode.NO_FF;
										else 
											fastForwardMode = FastForwardMode.FF;
										integrateHead = tempGit.merge(requestHead, fastForwardMode, null, null, 
												"Merge pull request: " + request.getTitle());
									}
								}
								 
								if (integrateHead != null) {
									request.setIntegrationInfo(new IntegrationInfo(
											branchHead, requestHead, integrateHead, strategy, 
											!tempGit.listChangedFiles(requestHead, integrateHead, null).isEmpty()));
									git.fetch(tempGit, "+HEAD:" + integrateRef);									
								} else {
									request.setIntegrationInfo(new IntegrationInfo(branchHead, requestHead, 
											integrateHead, strategy, false));
									git.deleteRef(integrateRef, null, null);
								}
							} finally {
								FileUtils.deleteDir(tempDir);
							}
						}
					}
					request.setCheckResult(request.getTarget().getRepository().getGateKeeper().checkRequest(request));

					for (VoteInvitation invitation : request.getVoteInvitations()) {
						if (!request.getCheckResult().canVote(invitation.getVoter(), request))
							dao.remove(invitation);
						else if (invitation.getId() == null)
							dao.persist(invitation);
					}
				}
		
				dao.persist(request);
				
				if (request.isAutoIntegrate() && request.canIntegrate())
					integrate(request, null, "Integrated automatically by system.");

				return null;
			}
			
		});
	}

	@Transactional
 	public void discard(final PullRequest request, final User user, final String comment) {
		request.lockAndCall(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				PullRequestAudit audit = new PullRequestAudit();
				audit.setRequest(request);
				audit.setDate(new Date());
				audit.setAction(new PullRequestAction.Discard());
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
				
				return null;
			}
			
		});
	}

	@Transactional
	public boolean integrate(final PullRequest request, final User user, final String comment) {
		Preconditions.checkArgument(request.canIntegrate(), "request can not be integrated.");

		return request.lockAndCall(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				if (request.getIntegrationInfo().getIntegrationStrategy() == REBASE_SOURCE) {
					Git sourceGit = request.getSource().getRepository().git();
					if (sourceGit.updateRef(request.getSource().getHeadRef(), 
							request.getIntegrationInfo().getIntegrationHead(), 
							request.getIntegrationInfo().getRequestHead(), "Rebase for pull request: " +request.getId())) {

						request.getSource().setHeadCommit(request.getIntegrationInfo().getIntegrationHead());
						request.getSource().setUpdater(user);
						branchManager.save(request.getSource());

						PullRequestUpdate update = new PullRequestUpdate();
						update.setRequest(request);
						update.setUser(user);
						update.setHeadCommit(request.getIntegrationInfo().getIntegrationHead());
						request.getUpdates().add(update);

						pullRequestUpdateManager.save(update);
					} else {
						logger.warn("Unable to update source branch '{}' due to lock failure.", request.getSource());
						return false;
					}
				}
				Git git = request.getTarget().getRepository().git();
				if (git.updateRef(request.getTarget().getHeadRef(), 
						request.getIntegrationInfo().getIntegrationHead(), 
						request.getIntegrationInfo().getBranchHead(), 
						comment!=null?comment:"Pull request #" + request.getId())) {
					request.getTarget().setHeadCommit(request.getIntegrationInfo().getIntegrationHead());
					request.getTarget().setUpdater(user);
					branchManager.save(request.getTarget());
					
					PullRequestAudit audit = new PullRequestAudit();
					audit.setRequest(request);
					audit.setDate(new Date());
					audit.setAction(new PullRequestAction.Integrate(null));
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
					
					return true;
				} else {
					logger.warn("Unable to target branch '{}' due to lock failure.", request.getTarget());
					return false;
				}
			}
			
		});
	}
	
	@Sessional
	@Override
	public List<PullRequest> findByCommit(String commit) {
		return dao.query(EntityCriteria.of(PullRequest.class)
				.add(Restrictions.or(
						Restrictions.eq("integrationInfo.requestHead", commit), 
						Restrictions.eq("integrationInfo.integrationHead", commit))), 0, 0);
	}

	@Transactional
	@Override
	public void send(PullRequest request) {
		dao.persist(request);

		request.git().updateRef(request.getBaseRef(), request.getBaseCommit(), null, null);
		
		for (PullRequestUpdate update: request.getUpdates()) {
			update.setDate(new Date(System.currentTimeMillis() + 1000));
			pullRequestUpdateManager.save(update);
		}

		refresh(request);
	}

	private IntegrationStrategy getIntegrationStrategy(PullRequest request) {
		IntegrationSetting integrationSetting = request.getTarget().getRepository().getIntegrationSetting();
		IntegrationStrategy strategy = null;
		for (BranchStrategy branchStrategy: integrationSetting.getBranchStrategies()) {
			if (branchStrategy.getTargetBranches().matches(request.getTarget()) 
					&& branchStrategy.getSourceBranches().matches(request.getSource())) {
				strategy = branchStrategy.getIntegrationStrategy();
				break;
			}
		}
		if (strategy == null) 
			strategy = integrationSetting.getDefaultStrategy();
		return strategy;
	}

	private void deleteRefsUponClose(PullRequest request) {
		Git git = request.getTarget().getRepository().git();
		for (PullRequestUpdate update: request.getUpdates()) {
			git.deleteRef(update.getChangeRef(), null, null);
		}
		git.deleteRef(request.getIntegrateRef(), null, null);
	}
}
