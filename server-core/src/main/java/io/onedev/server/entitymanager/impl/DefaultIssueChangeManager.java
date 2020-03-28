package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.util.lang.Objects;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.event.build.BuildFinished;
import io.onedev.server.event.issue.IssueChangeEvent;
import io.onedev.server.event.pullrequest.PullRequestChangeEvent;
import io.onedev.server.event.pullrequest.PullRequestOpened;
import io.onedev.server.git.GitUtils;
import io.onedev.server.issue.TransitionSpec;
import io.onedev.server.issue.transitiontrigger.BranchUpdateTrigger;
import io.onedev.server.issue.transitiontrigger.BuildSuccessfulTrigger;
import io.onedev.server.issue.transitiontrigger.DiscardPullRequest;
import io.onedev.server.issue.transitiontrigger.MergePullRequest;
import io.onedev.server.issue.transitiontrigger.OpenPullRequest;
import io.onedev.server.issue.transitiontrigger.PullRequestTrigger;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.issue.changedata.IssueBatchUpdateData;
import io.onedev.server.model.support.issue.changedata.IssueDescriptionChangeData;
import io.onedev.server.model.support.issue.changedata.IssueFieldChangeData;
import io.onedev.server.model.support.issue.changedata.IssueMilestoneChangeData;
import io.onedev.server.model.support.issue.changedata.IssueStateChangeData;
import io.onedev.server.model.support.issue.changedata.IssueTitleChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReopenData;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.search.entity.issue.IssueCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.util.Input;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.util.ProjectAwareCommit;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;

@Singleton
public class DefaultIssueChangeManager extends AbstractEntityManager<IssueChange>
		implements IssueChangeManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultIssueChangeManager.class);
	
	private static final int MAX_FIXED_ISSUES = 1000;
	
	private final IssueManager issueManager;
	
	private final IssueFieldManager issueFieldManager;
	
	private final TransactionManager transactionManager;
	
	private final ProjectManager projectManager;
	
	private final PullRequestManager pullRequestManager;
	
	private final BuildManager buildManager;
	
	private final ExecutorService executorService;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultIssueChangeManager(Dao dao, TransactionManager transactionManager, 
			IssueManager issueManager,  IssueFieldManager issueFieldManager,
			ProjectManager projectManager, BuildManager buildManager, 
			ExecutorService executorService, PullRequestManager pullRequestManager, 
			ListenerRegistry listenerRegistry) {
		super(dao);
		this.issueManager = issueManager;
		this.issueFieldManager = issueFieldManager;
		this.transactionManager = transactionManager;
		this.projectManager = projectManager;
		this.pullRequestManager = pullRequestManager;
		this.buildManager = buildManager;
		this.executorService = executorService;
		this.listenerRegistry = listenerRegistry;
	}

	@Transactional
	@Override
	public void save(IssueChange change) {
		dao.persist(change);
		listenerRegistry.post(new IssueChangeEvent(change));
	}
	
	@Transactional
	@Override
	public void changeTitle(Issue issue, String title) {
		String prevTitle = issue.getTitle();
		if (!title.equals(prevTitle)) {
			issue.setTitle(title);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueTitleChangeData(prevTitle, issue.getTitle()));
			save(change);
		}
	}

	@Transactional
	@Override
	public void changeDescription(Issue issue, @Nullable String description) {
		String prevDescription = issue.getDescription();
		if (!Objects.equal(description, prevDescription)) {
			issue.setDescription(description);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueDescriptionChangeData(prevDescription, issue.getDescription()));
			save(change);
		}
	}
	
	@Transactional
	@Override
	public void changeMilestone(Issue issue, @Nullable Milestone milestone) {
		Milestone prevMilestone = issue.getMilestone();
		if (!Objects.equal(prevMilestone, milestone)) {
			issue.setMilestone(milestone);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueMilestoneChangeData(prevMilestone, issue.getMilestone()));
			save(change);
		}
	}
	
	@Transactional
	@Override
	public void changeFields(Issue issue, Map<String, Object> fieldValues) {
		Map<String, Input> prevFields = issue.getFieldInputs(); 
		issue.setFieldValues(fieldValues);
		if (!prevFields.equals(issue.getFieldInputs())) {
			issueFieldManager.saveFields(issue);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueFieldChangeData(prevFields, issue.getFieldInputs()));
			save(change);
		}
	}
	
	@Transactional
	@Override
	public void changeState(Issue issue, String state, Map<String, Object> fieldValues, @Nullable String comment) {
		String prevState = issue.getState();
		Map<String, Input> prevFields = issue.getFieldInputs();
		issue.setState(state);

		issue.setFieldValues(fieldValues);
		
		issueFieldManager.saveFields(issue);
		
		IssueChange change = new IssueChange();
		change.setIssue(issue);
		change.setDate(new Date());
		change.setUser(SecurityUtils.getUser());
		change.setData(new IssueStateChangeData(prevState, issue.getState(), 
				prevFields, issue.getFieldInputs(), comment));
		save(change);
	}
	
	@Transactional
	@Override
	public void batchUpdate(Iterator<? extends Issue> issues, @Nullable String state, 
			@Nullable Optional<Milestone> milestone, Map<String, Object> fieldValues, 
			@Nullable String comment) {
		while (issues.hasNext()) {
			Issue issue = issues.next();
			String prevState = issue.getState();
			Milestone prevMilestone = issue.getMilestone();
			Map<String, Input> prevFields = issue.getFieldInputs();
			if (state != null)
				issue.setState(state);
			if (milestone != null)
				issue.setMilestone(milestone.orNull());
			
			issue.setFieldValues(fieldValues);
			issueFieldManager.saveFields(issue);

			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueBatchUpdateData(prevState, issue.getState(), prevMilestone, 
					issue.getMilestone(), prevFields, issue.getFieldInputs(), comment));
			
			save(change);
		}
	}
	
	@Transactional
	@Listen
	public void on(BuildFinished event) {
		Long buildId = event.getBuild().getId();
		Long projectId = event.getBuild().getProject().getId();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				executorService.execute(new Runnable() {

					@Override
					public void run() {
			        	LockUtils.call(getLockKey(projectId), new Callable<Void>() {

							@Override
							public Void call() throws Exception {
								transactionManager.run(new Runnable() {

									@Override
									public void run() {
										try {
											SecurityUtils.bindAsSystem();
											Build build = buildManager.load(buildId);
											for (TransitionSpec transition: build.getProject().getIssueSetting().getTransitionSpecs(true)) {
												if (transition.getTrigger() instanceof BuildSuccessfulTrigger) {
													Project project = build.getProject();
													BuildSuccessfulTrigger trigger = (BuildSuccessfulTrigger) transition.getTrigger();
													String branches = trigger.getBranches();
													ObjectId commitId = ObjectId.fromString(build.getCommitHash());
													if ((trigger.getJobNames() == null || PatternSet.parse(trigger.getJobNames()).matches(new StringMatcher(), build.getJobName())) 
															&& build.getStatus() == Build.Status.SUCCESSFUL
															&& (branches == null || project.isCommitOnBranches(commitId, branches))) {
														IssueQuery query = IssueQuery.parse(project, trigger.getIssueQuery(), true, false, true, false, false);
														List<IssueCriteria> criterias = new ArrayList<>();
														
														List<IssueCriteria> fromStateCriterias = new ArrayList<>();
														for (String fromState: transition.getFromStates()) 
															fromStateCriterias.add(new StateCriteria(fromState));
														
														criterias.add(IssueCriteria.or(fromStateCriterias));
														criterias.add(query.getCriteria());
														query = new IssueQuery(IssueCriteria.and(criterias), new ArrayList<>());
														Build.push(build);
														try {
															for (Issue issue: issueManager.query(project, query, 0, Integer.MAX_VALUE)) {
																issue.removeFields(transition.getRemoveFields());
																changeState(issue, transition.getToState(), new HashMap<>(), null);
															}
														} finally {
															Build.pop();
														}
													}
												}      												
											}
										} catch (Exception e) {
											logger.error("Error changing issue state", e);
										}
									}
								});
								return null;
							}
			        	});
					}
				});
			}
		});
	}
	
	private void on(PullRequest request, Class<? extends PullRequestTrigger> triggerClass) {
		Long requestId = request.getId();
		Long projectId = request.getTargetProject().getId();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				executorService.execute(new Runnable() {

					@Override
					public void run() {
						LockUtils.call(getLockKey(projectId), new Callable<Void>() {

							@Override
							public Void call() throws Exception {
								transactionManager.run(new Runnable() {

									@Override
									public void run() {
										try {
											SecurityUtils.bindAsSystem();
											Matcher matcher = new PathMatcher();
											PullRequest request = pullRequestManager.load(requestId);
											Project project = request.getTargetProject();
											for (TransitionSpec transition: project.getIssueSetting().getTransitionSpecs(true)) {
												if (transition.getTrigger().getClass() == triggerClass) {
													PullRequestTrigger trigger = (PullRequestTrigger) transition.getTrigger();
													if (trigger.getBranches() == null || PatternSet.parse(trigger.getBranches()).matches(matcher, request.getTargetBranch())) {
														IssueQuery query = IssueQuery.parse(project, trigger.getIssueQuery(), true, false, false, true, false);
														List<IssueCriteria> criterias = new ArrayList<>();
														
														List<IssueCriteria> fromStateCriterias = new ArrayList<>();
														for (String fromState: transition.getFromStates()) 
															fromStateCriterias.add(new StateCriteria(fromState));
														
														criterias.add(IssueCriteria.or(fromStateCriterias));
														criterias.add(query.getCriteria());
														query = new IssueQuery(IssueCriteria.and(criterias), new ArrayList<>());
														PullRequest.push(request);
														try {
															for (Issue issue: issueManager.query(project, query, 0, Integer.MAX_VALUE)) {
																issue.removeFields(transition.getRemoveFields());
																changeState(issue, transition.getToState(), new HashMap<>(), null);
															}
														} finally {
															PullRequest.pop();
														}
													}
												}
											}
										} catch (Exception e) {
											logger.error("Error changing issue state", e);
										}
									}
								});
								return null;
							}
							
						});
					}
					
				});
			}
		});
	}
	
	@Transactional
	@Listen
	public void on(PullRequestChangeEvent event) { 
		if (event.getChange().getData() instanceof PullRequestMergeData) 
			on(event.getRequest(), MergePullRequest.class);
		else if (event.getChange().getData() instanceof PullRequestDiscardData) 
			on(event.getRequest(), DiscardPullRequest.class);
		else if (event.getChange().getData() instanceof PullRequestReopenData) 
			on(event.getRequest(), OpenPullRequest.class);
	}
	
	@Transactional
	@Listen
	public void on(PullRequestOpened event) {
		on(event.getRequest(), OpenPullRequest.class);
	}
	
	private String getLockKey(Long projectId) {
		return "change-issue-state-" + projectId;
	}

	@Transactional
	@Listen
	public void on(RefUpdated event) {
		String refName = event.getRefName();
		String branchName = GitUtils.ref2branch(refName);
		ObjectId newCommitId = event.getNewCommitId();
		if (!newCommitId.equals(ObjectId.zeroId()) && branchName != null) {
			/*
			 * Transit states of issues fixed by commits under the new branch. Note that ideally 
			 * we should also transit states of issues fixed by builds under the new branch. 
			 * However that will be very cost. To work around this, when a branch is pushed, new 
			 * builds should be running on that branch in order to get issue states transited 
			 */
			Project project = event.getProject();
			Long projectId = project.getId();
			ObjectId oldCommitId = event.getOldCommitId();
			
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					executorService.execute(new Runnable() {

						@Override
						public void run() {
				        	LockUtils.call(getLockKey(projectId), new Callable<Void>() {

								@Override
								public Void call() throws Exception {
									transactionManager.run(new Runnable() {

										@Override
										public void run() {
											try {
												SecurityUtils.bindAsSystem();
												Project project = projectManager.load(projectId);
												Set<Long> fixedIssueNumbers = new HashSet<>();
												try (RevWalk revWalk = new RevWalk(project.getRepository())) {
													revWalk.markStart(revWalk.lookupCommit(newCommitId));
													if (oldCommitId.equals(ObjectId.zeroId())) {
														/*
														 * In case a new branch is pushed, we only process new commits not in any existing branches
														 * for performance reason. This is reasonable as state of fixed issues by commits in 
														 * existing branches most probably is already transited        
														 */
														for (Ref ref: project.getRepository().getRefDatabase().getRefsByPrefix(Constants.R_HEADS)) {
															if (!ref.getName().equals(refName))
																revWalk.markUninteresting(revWalk.lookupCommit(ref.getObjectId()));
														}
													} else {
														revWalk.markUninteresting(revWalk.lookupCommit(oldCommitId));
													}
													RevCommit commit;
													while ((commit = revWalk.next()) != null) {
														fixedIssueNumbers.addAll(IssueUtils.parseFixedIssueNumbers(commit.getFullMessage()));
														if (fixedIssueNumbers.size() > MAX_FIXED_ISSUES)
															break;
													}
												} 
												for (TransitionSpec transition: project.getIssueSetting().getTransitionSpecs(true)) {
													if (transition.getTrigger() instanceof BranchUpdateTrigger) {
														BranchUpdateTrigger trigger = (BranchUpdateTrigger) transition.getTrigger();
														String branches = trigger.getBranches();
														Matcher matcher = new PathMatcher();
														if (branches == null || PatternSet.parse(branches).matches(matcher, branchName)) {
															IssueQuery query = IssueQuery.parse(project, trigger.getIssueQuery(), 
																	true, false, false, false, true);
															List<IssueCriteria> criterias = new ArrayList<>();
															
															List<IssueCriteria> fromStateCriterias = new ArrayList<>();
															for (String fromState: transition.getFromStates()) 
																fromStateCriterias.add(new StateCriteria(fromState));
															
															criterias.add(IssueCriteria.or(fromStateCriterias));
															criterias.add(query.getCriteria());
															query = new IssueQuery(IssueCriteria.and(criterias), new ArrayList<>());
															ProjectAwareCommit.push(new ProjectAwareCommit(project, newCommitId) {

																private static final long serialVersionUID = 1L;

																@Override
																public Collection<Long> getFixedIssueNumbers() {
																	return fixedIssueNumbers;
																}
																
															});
															try {
																for (Issue issue: issueManager.query(project, query, 0, Integer.MAX_VALUE)) {
																	issue.removeFields(transition.getRemoveFields());
																	changeState(issue, transition.getToState(), new HashMap<>(), null);
																}
															} finally {
																ProjectAwareCommit.pop();
															}
														}
													}
												}
											} catch (Exception e) {
												logger.error("Error changing issue state", e);
											}
										}
									});
									return null;
								}
				        		
				        	});							
						}
						
					});
				}
			});
		}
	}

}
