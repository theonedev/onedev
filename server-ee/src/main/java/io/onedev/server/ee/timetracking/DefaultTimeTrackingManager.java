package io.onedev.server.ee.timetracking;

import com.google.common.collect.Sets;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueWork;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.changedata.IssueLinkChangeData;
import io.onedev.server.model.support.issue.changedata.IssueOwnEstimatedTimeChangeData;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.timetracking.TimeTrackingManager;
import io.onedev.server.web.page.layout.SidebarMenuItem;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

@Singleton
public class DefaultTimeTrackingManager implements TimeTrackingManager {
	
	private final SettingManager settingManager;
	
	private final TransactionManager transactionManager;
	
	private final ExecutorService executorService;
	
	private final IssueManager issueManager;
	
	private final IssueChangeManager issueChangeManager;
	
	private final SubscriptionManager subscriptionManager;
	
	@Inject
	public DefaultTimeTrackingManager(SettingManager settingManager, TransactionManager transactionManager, 
									  IssueManager issueManager, IssueChangeManager issueChangeManager, 
									  ExecutorService executorService, SubscriptionManager subscriptionManager) {
		this.settingManager = settingManager;
		this.transactionManager = transactionManager;
		this.issueManager = issueManager;
		this.issueChangeManager = issueChangeManager;
		this.executorService = executorService;
		this.subscriptionManager = subscriptionManager;
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (subscriptionManager.isSubscriptionActive()) {
			if (event.getEntity() instanceof Issue) {
				Issue issue = (Issue) event.getEntity();
				var timeTrackingSetting = settingManager.getIssueSetting().getTimeTrackingSetting();
				String aggregationLink;
				if (issue.getProject().isTimeTracking()
						&& (aggregationLink = timeTrackingSetting.getAggregationLink()) != null) {
					var affectedIssueIds = new HashSet<Long>();
					for (var issueLink : issue.getTargetLinks()) {
						if (issueLink.getSpec().getOpposite() != null
								&& issueLink.getSpec().getOpposite().getName().equals(aggregationLink)) {
							affectedIssueIds.add(issueLink.getTarget().getId());
						}
					}
					for (var issueLink : issue.getSourceLinks()) {
						if (issueLink.getSpec().getName().equals(aggregationLink))
							affectedIssueIds.add(issueLink.getSource().getId());
					}
					requestToSyncTimes(affectedIssueIds);
				}
			} else if (event.getEntity() instanceof IssueWork) {
				var issue = ((IssueWork) event.getEntity()).getIssue();
				if (issue.getProject().isTimeTracking())
					requestToSyncTimes(Sets.newHashSet(issue.getId()));
			}
		}
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (subscriptionManager.isSubscriptionActive()) {
			if (event.getEntity() instanceof IssueWork) {
				var issue = ((IssueWork) event.getEntity()).getIssue();
				if (issue.getProject().isTimeTracking())
					requestToSyncTimes(Sets.newHashSet(issue.getId()));
			} else if (event.getEntity() instanceof IssueChange) {
				IssueChange change = (IssueChange) event.getEntity();
				var issue = change.getIssue();
				if (issue.getProject().isTimeTracking()) {
					if (change.getData() instanceof IssueOwnEstimatedTimeChangeData) {
						requestToSyncTimes(Sets.newHashSet(issue.getId()));
					} else if (change.getData() instanceof IssueLinkChangeData) {
						IssueLinkChangeData changeData = (IssueLinkChangeData) change.getData();
						var aggregationLink = settingManager.getIssueSetting().getTimeTrackingSetting().getAggregationLink();
						if (changeData.getLinkName().equals(aggregationLink))
							requestToSyncTimes(Sets.newHashSet(issue.getId()));
					}
				}
			}
		}
	}

	@Transactional
	@Override
	public void requestToSyncTimes(Collection<Long> issueIds) {
		if (!issueIds.isEmpty()) {
			transactionManager.runAfterCommit(() -> {
				executorService.execute(() -> {
					var aggregationLink = settingManager.getIssueSetting().getTimeTrackingSetting().getAggregationLink();
					for (var issueId : issueIds)
						syncTimes(issueId, aggregationLink, new HashSet<>());
				});
			});
		}
	}
	
	private void syncTimes(Long issueId, @Nullable String aggregationLink, Set<Long> processedIssueIds) {
		if (processedIssueIds.add(issueId)) {
			var affectedIssueIds = LockUtils.call("time-tracking-" + issueId, () -> transactionManager.call(() -> {
				var issue = issueManager.load(issueId);

				boolean timingChanged = false;

				int totalEstimatedTime = issue.getOwnEstimatedTime();
				if (aggregationLink != null) {
					totalEstimatedTime += aggregateSourceLinkEstimatedTime(issue, aggregationLink);
					totalEstimatedTime += aggregateTargetLinkEstimatedTime(issue, aggregationLink);
				}
				if (issue.getTotalEstimatedTime() != totalEstimatedTime) {
					issueChangeManager.changeTotalEstimatedTime(issue, totalEstimatedTime);
					timingChanged = true;
				}

				int ownSpentTime = issue.getWorks().stream().mapToInt(IssueWork::getMinutes).sum();
				issueChangeManager.changeOwnSpentTime(issue, ownSpentTime);
				
				int totalSpentTime = ownSpentTime;
				if (aggregationLink != null) {
					totalSpentTime += aggregateSourceLinkSpentTime(issue, aggregationLink);
					totalSpentTime += aggregateTargetLinkSpentTime(issue, aggregationLink);
				}
				if (issue.getTotalSpentTime() != totalSpentTime) {
					issueChangeManager.changeTotalSpentTime(issue, totalSpentTime);
					timingChanged = true;
				}

				Set<Long> innerAffectedIssueIds = new HashSet<>();
				if (timingChanged && aggregationLink != null) {
					for (var link : issue.getSourceLinks()) {
						if (link.getSpec().getName().equals(aggregationLink))
							innerAffectedIssueIds.add(link.getSource().getId());
					}
					for (var link : issue.getTargetLinks()) {
						if (link.getSpec().getOpposite() != null
								&& link.getSpec().getOpposite().getName().equals(aggregationLink)) {
							innerAffectedIssueIds.add(link.getTarget().getId());
						}
					}
				}
				return innerAffectedIssueIds;
			}));
			
			for (var affectedIssueId: affectedIssueIds) 
				syncTimes(affectedIssueId, aggregationLink, new HashSet<>(processedIssueIds));
		}
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

	public SidebarMenuItem.Page newTimesheetsMenuItem(Project project) {
		return new SidebarMenuItem.Page(null, "Timesheets",
				TimesheetsPage.class, TimesheetsPage.paramsOf(project, null, null));
	}	
}
