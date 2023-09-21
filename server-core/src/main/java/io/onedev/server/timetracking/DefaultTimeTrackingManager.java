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
import io.onedev.server.model.support.issue.changedata.IssueLinkChangeData;
import io.onedev.server.model.support.issue.changedata.IssueOwnEstimatedTimeChangeData;
import io.onedev.server.model.support.issue.changedata.IssueTotalEstimatedTimeChangeData;
import io.onedev.server.model.support.issue.changedata.IssueTotalSpentTimeChangeData;
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

import static io.onedev.server.timetracking.TimeAggregationDirection.*;

@Singleton
public class DefaultTimeTrackingManager implements TimeTrackingManager {

	private static final ThreadLocal<Collection<Long>> processedIssueIdsHolder = ThreadLocal.withInitial(HashSet::new);
	
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
			String timeAggregationLink;
			if (issue.getProject().isTimeTracking()
					&& (timeAggregationLink = timeTrackingSetting.getTimeAggregationLink()) != null) {
				var affectedIssueIds = new HashSet<Long>();
				for (var issueLink: issue.getTargetLinks()) {
					if (issueLink.getSpec().getOpposite() != null 
							&& issueLink.getSpec().getOpposite().getName().equals(timeAggregationLink)) {
						affectedIssueIds.add(issueLink.getTarget().getId());
					}
				}
				for (var issueLink: issue.getSourceLinks()) {
					if (issueLink.getSpec().getName().equals(timeAggregationLink))
						affectedIssueIds.add(issueLink.getSource().getId());
				}
				for (var affectedIssueId: affectedIssueIds) {
					runAsyncAfterCommit(affectedIssueId, () -> {
						var affectedIssue = issueManager.get(affectedIssueId);
						if (affectedIssue != null) {
							syncEstimatedTime(affectedIssue, timeAggregationLink, BOTH);
							syncSpentTime(affectedIssue, timeAggregationLink, BOTH);
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
					var timeTrackingSetting = settingManager.getIssueSetting().getTimeTrackingSetting();
					syncSpentTime(innerIssue, timeTrackingSetting.getTimeAggregationLink(), BOTH);
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
					var timeTrackingSetting = settingManager.getIssueSetting().getTimeTrackingSetting();
					syncSpentTime(innerIssue, timeTrackingSetting.getTimeAggregationLink(), BOTH);
				});
			}
		} else if (event.getEntity() instanceof IssueChange) {
			IssueChange issueChange = (IssueChange) event.getEntity();
			var issue = issueChange.getIssue();
			var issueId = issue.getId();
			if (issue.getProject().isTimeTracking()) {
				var timeTrackingSetting = settingManager.getIssueSetting().getTimeTrackingSetting();
				var timeAggregationLink = timeTrackingSetting.getTimeAggregationLink();
				if (issueChange.getData() instanceof IssueOwnEstimatedTimeChangeData) {
					runAsyncAfterCommit(issueId, () -> {
						var innerIssue = issueManager.load(issueId);
						syncEstimatedTime(innerIssue, timeAggregationLink, BOTH);
					});
				} else if (issueChange.getData() instanceof IssueTotalEstimatedTimeChangeData 
						|| issueChange.getData() instanceof IssueTotalSpentTimeChangeData) {
					if (timeAggregationLink != null) {
						var processedIssueIds = processedIssueIdsHolder.get();
						if (!processedIssueIds.contains(issueId)) {
							runAsyncAfterCommit(issueId, () -> {
								var copyOfProcessedIssueIds = new HashSet<>(processedIssueIds);
								copyOfProcessedIssueIds.add(issueId);
								processedIssueIdsHolder.set(copyOfProcessedIssueIds);
								try {
									var innerIssue = issueManager.load(issueId);
									for (var sourceLink : innerIssue.getSourceLinks()) {
										if (sourceLink.getSpec().getName().equals(timeAggregationLink)) {
											var sourceIssue = sourceLink.getSource();
											if (issueChange.getData() instanceof IssueTotalEstimatedTimeChangeData)
												syncEstimatedTime(sourceIssue, timeAggregationLink, TARGET);
											else
												syncSpentTime(sourceIssue, timeAggregationLink, TARGET);
										}
									}
									for (var targetLink : innerIssue.getTargetLinks()) {
										if (targetLink.getSpec().getOpposite() != null
												&& targetLink.getSpec().getOpposite().getName().equals(timeAggregationLink)) {
											var targetIssue = targetLink.getTarget();
											if (issueChange.getData() instanceof IssueTotalEstimatedTimeChangeData)
												syncEstimatedTime(targetIssue, timeAggregationLink, SOURCE);
											else
												syncSpentTime(targetIssue, timeAggregationLink, SOURCE);
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
					if (changeData.getLinkName().equals(timeAggregationLink)) {
						runAsyncAfterCommit(issueId, () -> {
							var innerIssue = issueManager.load(issueId);
							if (!changeData.isOpposite()) {
								syncEstimatedTime(innerIssue, changeData.getLinkName(), TARGET);
								syncSpentTime(innerIssue, changeData.getLinkName(), TARGET);
							} else {
								syncEstimatedTime(innerIssue, changeData.getLinkName(), SOURCE);
								syncSpentTime(innerIssue, changeData.getLinkName(), SOURCE);
							}
						});
					}
				}
			}
		}
	}

	@Override
	public void syncEstimatedTime(Issue issue, @Nullable String aggregationLink, 
								  TimeAggregationDirection direction) {
		int totalTime = issue.getOwnEstimatedTime();
		if (aggregationLink != null) {
			if (direction == TARGET || direction == BOTH)
				totalTime += aggregateTargetLinkEstimatedTime(issue, aggregationLink);
			if (direction == SOURCE || direction == BOTH)
				totalTime += aggregateSourceLinkEstimatedTime(issue, aggregationLink);
		}
		issueChangeManager.changeTotalEstimatedTime(issue, totalTime);
	}

	@Override
	public void syncSpentTime(Issue issue, @Nullable String aggregationLink, 
							  TimeAggregationDirection direction) {
		int totalTime = issue.getWorks().stream().mapToInt(IssueWork::getMinutes).sum();
		if (aggregationLink != null) {
			if (direction == TARGET || direction == BOTH)
				totalTime += aggregateTargetLinkSpentTime(issue, aggregationLink);
			if (direction == SOURCE || direction == BOTH)
				totalTime += aggregateSourceLinkSpentTime(issue, aggregationLink);
		}
		issueChangeManager.changeTotalSpentTime(issue, totalTime);
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
