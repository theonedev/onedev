package io.onedev.server.timetracking;

import io.onedev.commons.utils.LockUtils;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueWork;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.changedata.*;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.util.ThreadContext;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;

import static io.onedev.server.timetracking.LinkAggregation.Direction.*;
import static java.lang.ThreadLocal.withInitial;

@Singleton
public class DefaultTimeTrackingManager implements TimeTrackingManager {

	private static final ThreadLocal<Collection<Long>> processedIssueIdsHolder = withInitial(HashSet::new);
	
	private final SettingManager settingManager;
	
	private final TransactionManager transactionManager;
	
	private final ExecutorService executorService;
	
	private final IssueManager issueManager;
	
	private final IssueChangeManager issueChangeManager;
	
	@Inject
	public DefaultTimeTrackingManager(SettingManager settingManager, TransactionManager transactionManager, 
									  IssueManager issueManager, IssueChangeManager issueChangeManager, 
									  ExecutorService executorService) {
		this.settingManager = settingManager;
		this.transactionManager = transactionManager;
		this.issueManager = issueManager;
		this.issueChangeManager = issueChangeManager;
		this.executorService = executorService;
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Issue) {
			Issue issue = (Issue) event.getEntity();
			var timeTrackingSetting = settingManager.getIssueSetting().getTimeTrackingSetting();
			String aggregationLink;
			if (issue.getProject().isTimeTracking()
					&& (aggregationLink = timeTrackingSetting.getAggregationLink()) != null) {
				var affectedIssueIds = new HashSet<Long>();
				for (var issueLink: issue.getTargetLinks()) {
					if (issueLink.getSpec().getOpposite() != null 
							&& issueLink.getSpec().getOpposite().getName().equals(aggregationLink)) {
						affectedIssueIds.add(issueLink.getTarget().getId());
					}
				}
				for (var issueLink: issue.getSourceLinks()) {
					if (issueLink.getSpec().getName().equals(aggregationLink))
						affectedIssueIds.add(issueLink.getSource().getId());
				}
				for (var affectedIssueId: affectedIssueIds) {
					runAsyncAfterCommit(affectedIssueId, () -> {
						var affectedIssue = issueManager.get(affectedIssueId);
						if (affectedIssue != null) {
							syncTotalEstimatedTime(affectedIssue, new LinkAggregation(aggregationLink, BOTH));
							syncTotalSpentTime(affectedIssue, new LinkAggregation(aggregationLink, BOTH));
						}
					});
				}
			}
		} else if (event.getEntity() instanceof IssueWork) {
			var issueWork = (IssueWork) event.getEntity();
			var issue = issueWork.getIssue();
			if (issue.getProject().isTimeTracking()) {
				var issueId = issue.getId();
				runAsyncAfterCommit(issueId, () -> {
					var innerIssue = issueManager.load(issueId);
					syncOwnSpentTime(innerIssue);
				});
			}
		}
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof IssueWork) {
			var issueWork = (IssueWork) event.getEntity();
			var issue = issueWork.getIssue();
			if (issue.getProject().isTimeTracking()) {
				var issueId = issue.getId();
				runAsyncAfterCommit(issueId, () -> {
					var innerIssue = issueManager.load(issueId);
					syncOwnSpentTime(innerIssue);
				});
			}
		} else if (event.getEntity() instanceof IssueChange) {
			IssueChange issueChange = (IssueChange) event.getEntity();
			var issue = issueChange.getIssue();
			var issueId = issue.getId();
			if (issue.getProject().isTimeTracking()) {
				var timeTrackingSetting = settingManager.getIssueSetting().getTimeTrackingSetting();
				var aggregationLink = timeTrackingSetting.getAggregationLink();
				if (issueChange.getData() instanceof IssueOwnEstimatedTimeChangeData) {
					runAsyncAfterCommit(issueId, () -> {
						var innerIssue = issueManager.load(issueId);
						LinkAggregation linkAggregation = null;
						if (aggregationLink != null)
							linkAggregation = new LinkAggregation(aggregationLink, BOTH);
						syncTotalEstimatedTime(innerIssue, linkAggregation);
					});
				} else if (issueChange.getData() instanceof IssueOwnSpentTimeChangeData) {
					runAsyncAfterCommit(issueId, () -> {
						var innerIssue = issueManager.load(issueId);
						LinkAggregation linkAggregation = null;
						if (aggregationLink != null)
							linkAggregation = new LinkAggregation(aggregationLink, BOTH);
						syncTotalSpentTime(innerIssue, linkAggregation);
					});
				} else if (issueChange.getData() instanceof IssueTotalEstimatedTimeChangeData 
						|| issueChange.getData() instanceof IssueTotalSpentTimeChangeData) {
					if (aggregationLink != null) {
						var processedIssueIds = processedIssueIdsHolder.get();
						if (!processedIssueIds.contains(issueId)) {
							runAsyncAfterCommit(issueId, () -> {
								var copyOfProcessedIssueIds = new HashSet<>(processedIssueIds);
								copyOfProcessedIssueIds.add(issueId);
								processedIssueIdsHolder.set(copyOfProcessedIssueIds);
								try {
									var innerIssue = issueManager.load(issueId);
									var targetLinkAggregation = new LinkAggregation(aggregationLink, TARGET);
									for (var sourceLink : innerIssue.getSourceLinks()) {
										if (sourceLink.getSpec().getName().equals(aggregationLink)) {
											var sourceIssue = sourceLink.getSource();
											if (issueChange.getData() instanceof IssueTotalEstimatedTimeChangeData)
												syncTotalEstimatedTime(sourceIssue, targetLinkAggregation);
											else
												syncTotalSpentTime(sourceIssue, targetLinkAggregation);
										}
									}
									var sourceLinkAggregation = new LinkAggregation(aggregationLink, SOURCE);
									for (var targetLink : innerIssue.getTargetLinks()) {
										if (targetLink.getSpec().getOpposite() != null
												&& targetLink.getSpec().getOpposite().getName().equals(aggregationLink)) {
											var targetIssue = targetLink.getTarget();
											if (issueChange.getData() instanceof IssueTotalEstimatedTimeChangeData)
												syncTotalEstimatedTime(targetIssue, sourceLinkAggregation);
											else
												syncTotalSpentTime(targetIssue, sourceLinkAggregation);
										}
									}
								} finally {
									processedIssueIdsHolder.set(new HashSet<>());
								}
							});
						}
					}
				} else if (issueChange.getData() instanceof IssueLinkChangeData) {
					IssueLinkChangeData changeData = (IssueLinkChangeData) issueChange.getData();
					if (changeData.getLinkName().equals(aggregationLink)) {
						runAsyncAfterCommit(issueId, () -> {
							var innerIssue = issueManager.load(issueId);
							if (!changeData.isOpposite()) {
								var linkAggregation = new LinkAggregation(changeData.getLinkName(), TARGET);
								syncTotalEstimatedTime(innerIssue, linkAggregation);
								syncTotalSpentTime(innerIssue, linkAggregation);
							} else {
								var linkAggregation = new LinkAggregation(changeData.getLinkName(), SOURCE);
								syncTotalEstimatedTime(innerIssue, linkAggregation);
								syncTotalSpentTime(innerIssue, linkAggregation);
							}
						});
					}
				}
			}
		}
	}

	@Override
	public void syncTotalEstimatedTime(Issue issue, @Nullable LinkAggregation linkAggregation) {
		int totalTime = issue.getOwnEstimatedTime();
		if (linkAggregation != null) {
			var linkName = linkAggregation.getLinkName();
			var direction = linkAggregation.getDirection();
			if (direction == TARGET || direction == BOTH)
				totalTime += aggregateTargetLinkEstimatedTime(issue, linkName);
			if (direction == SOURCE || direction == BOTH)
				totalTime += aggregateSourceLinkEstimatedTime(issue, linkName);
		}
		issueChangeManager.changeTotalEstimatedTime(issue, totalTime);
	}

	@Override
	public void syncTotalSpentTime(Issue issue, @Nullable LinkAggregation linkAggregation) {
		int totalTime = issue.getOwnSpentTime();
		if (linkAggregation != null) {
			var linkName = linkAggregation.getLinkName();
			var direction = linkAggregation.getDirection();
			if (direction == TARGET || direction == BOTH)
				totalTime += aggregateTargetLinkSpentTime(issue, linkName);
			if (direction == SOURCE || direction == BOTH)
				totalTime += aggregateSourceLinkSpentTime(issue, linkName);
		}
		issueChangeManager.changeTotalSpentTime(issue, totalTime);
	}

	@Override
	public void syncOwnSpentTime(Issue issue) {
		int ownTime = issue.getWorks().stream().mapToInt(IssueWork::getMinutes).sum();
		issueChangeManager.changeOwnSpentTime(issue, ownTime);
	}

	@Override
	public void syncTimes(Issue issue) {
		var timeTrackingSetting = settingManager.getIssueSetting().getTimeTrackingSetting();
		syncOwnSpentTime(issue);
		LinkAggregation linkAggregation = null;
		if (timeTrackingSetting.getAggregationLink() != null) 
			linkAggregation = new LinkAggregation(timeTrackingSetting.getAggregationLink(), BOTH);
		syncTotalSpentTime(issue, linkAggregation);
		syncTotalEstimatedTime(issue, linkAggregation);
	}

	@Override
	public int aggregateTargetLinkEstimatedTime(Issue issue, String linkName) {
		int totalTime = 0;
		for (var targetLink : issue.getTargetLinks()) {
			if (targetLink.getSpec().getName().equals(linkName)) 
				totalTime += targetLink.getTarget().getTotalEstimatedTime();
		}
		return totalTime;
	}
	
	@Override
	public int aggregateSourceLinkEstimatedTime(Issue issue, String linkName) {
		var totalTime = 0;
		for (var sourceLink : issue.getSourceLinks()) {
			if (sourceLink.getSpec().getOpposite() != null && sourceLink.getSpec().getOpposite().getName().equals(linkName)) 
				totalTime += sourceLink.getSource().getTotalEstimatedTime();
		}
		return totalTime;
	}

	@Override
	public int aggregateTargetLinkSpentTime(Issue issue, String linkName) {
		int totalTime = 0;
		for (var targetLink : issue.getTargetLinks()) {
			if (targetLink.getSpec().getName().equals(linkName))
				totalTime += targetLink.getTarget().getTotalSpentTime();
		}
		return totalTime;
	}

	@Override
	public int aggregateSourceLinkSpentTime(Issue issue, String linkName) {
		var totalTime = 0;
		for (var sourceLink : issue.getSourceLinks()) {
			if (sourceLink.getSpec().getOpposite() != null && sourceLink.getSpec().getOpposite().getName().equals(linkName))
				totalTime += sourceLink.getSource().getTotalSpentTime();
		}
		return totalTime;
	}
	
	private void runAsyncAfterCommit(Long issueId, Runnable runnable) {
		transactionManager.runAfterCommit(() -> {
			executorService.execute(() -> {
				ThreadContext.bind(SecurityUtils.asSubject(User.SYSTEM_ID));
				LockUtils.call("time-tracking-" + issueId, () -> {
					transactionManager.run(runnable);
					return null;
				});
			});
		});
	}
}
