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
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.TimeTrackingSetting;
import io.onedev.server.model.support.issue.changedata.IssueFieldChangeData;
import io.onedev.server.model.support.issue.changedata.IssueLinkChangeData;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.util.ThreadContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;

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
			if (timeTrackingSetting != null && timeTrackingSetting.isProjectApplicable(issue.getProject())
					&& timeTrackingSetting.getTimeAggregationLink() != null) {
				var affectedIssueIds = new HashSet<Long>();
				for (var issueLink: issue.getTargetLinks()) {
					if (issueLink.getSpec().getOpposite() != null 
							&& issueLink.getSpec().getOpposite().getName().equals(timeTrackingSetting.getTimeAggregationLink())) {
						affectedIssueIds.add(issueLink.getTarget().getId());
					}
				}
				for (var issueLink: issue.getSourceLinks()) {
					if (issueLink.getSpec().getName().equals(timeTrackingSetting.getTimeAggregationLink()))
						affectedIssueIds.add(issueLink.getSource().getId());
				}
				for (var affectedIssueId: affectedIssueIds) {
					trackTime(affectedIssueId, () -> {
						var affectedIssue = issueManager.get(affectedIssueId);
						if (affectedIssue != null)
							syncTimes(affectedIssue, timeTrackingSetting);
					});
				}
			}
		}		
	}
	
	private void syncTimes(Issue issue, TimeTrackingSetting timeTrackingSetting) {
		var linkName = timeTrackingSetting.getTimeAggregationLink();
		aggregateTargetLinkTimes(issue, linkName, timeTrackingSetting.getEstimatedTimeField());
		aggregateTargetLinkTimes(issue, linkName, timeTrackingSetting.getSpentTimeField());
		aggregateSourceLinkTimes(issue, linkName, timeTrackingSetting.getEstimatedTimeField());
		aggregateSourceLinkTimes(issue, linkName, timeTrackingSetting.getSpentTimeField());
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof IssueChange) {
			IssueChange issueChange = (IssueChange) event.getEntity();
			var issue = issueChange.getIssue();
			var issueId = issue.getId();
			var timeTrackingSetting = settingManager.getIssueSetting().getTimeTrackingSetting();
			if (timeTrackingSetting != null && timeTrackingSetting.isProjectApplicable(issue.getProject())
					&& timeTrackingSetting.getTimeAggregationLink() != null) {
				if (issueChange.getData() instanceof IssueFieldChangeData) {
					var processedIssueIds = processedIssueIdsHolder.get();
					if (!processedIssueIds.contains(issueId)) {
						IssueFieldChangeData changeData = (IssueFieldChangeData) issueChange.getData();

						var fieldNames = new HashSet<String>();
						fieldNames.addAll(changeData.getOldFields().keySet());
						fieldNames.addAll(changeData.getNewFields().keySet());

						for (var fieldName: fieldNames) {
							if (fieldName.equals(timeTrackingSetting.getEstimatedTimeField()) || fieldName.equals(timeTrackingSetting.getSpentTimeField())) {
								trackTime(issueId, () -> {
									var copyOfProcessedIssueIds = new HashSet<>(processedIssueIds);
									copyOfProcessedIssueIds.add(issueId);
									processedIssueIdsHolder.set(copyOfProcessedIssueIds);
									try {
										transactionManager.run(() -> {
											var innerIssue = issueManager.load(issueId);
											for (var sourceLink : innerIssue.getSourceLinks()) {
												if (sourceLink.getSpec().getName().equals(timeTrackingSetting.getTimeAggregationLink()))
													aggregateTargetLinkTimes(sourceLink.getSource(), timeTrackingSetting.getTimeAggregationLink(), fieldName);
											}
											for (var targetLink : innerIssue.getTargetLinks()) {
												if (targetLink.getSpec().getOpposite() != null
														&& targetLink.getSpec().getOpposite().getName().equals(timeTrackingSetting.getTimeAggregationLink())) {
													aggregateSourceLinkTimes(targetLink.getTarget(), timeTrackingSetting.getTimeAggregationLink(), fieldName);
												}
											}
										});
									} finally {
										processedIssueIdsHolder.set(new HashSet<>());
									}
								});
							}
						}
					}
				} else if (issueChange.getData() instanceof IssueLinkChangeData) {
					IssueLinkChangeData changeData = (IssueLinkChangeData) issueChange.getData();
					if (changeData.getLinkName().equals(timeTrackingSetting.getTimeAggregationLink())) {
						trackTime(issueId, () -> {
							var innerIssue = issueManager.load(issueId);
							if (!changeData.isOpposite()) {
								aggregateTargetLinkTimes(innerIssue, changeData.getLinkName(), timeTrackingSetting.getEstimatedTimeField());
								aggregateTargetLinkTimes(innerIssue, changeData.getLinkName(), timeTrackingSetting.getSpentTimeField());
							} else {
								aggregateSourceLinkTimes(innerIssue, changeData.getLinkName(), timeTrackingSetting.getEstimatedTimeField());
								aggregateSourceLinkTimes(innerIssue, changeData.getLinkName(), timeTrackingSetting.getSpentTimeField());
							}
						});
					}
				}
			}
		}
	}
	
	private void aggregateTargetLinkTimes(Issue issue, String linkName, String fieldName) {
		var totalMinutes = 0;
		for (var targetLink : issue.getTargetLinks()) {
			if (targetLink.getSpec().getName().equals(linkName)) {
				var minutesOfTargetIssue = targetLink.getTarget().getFieldValue(fieldName);
				if (minutesOfTargetIssue != null) 
					totalMinutes += (int) minutesOfTargetIssue;
			}
		}
		if (totalMinutes != 0) {
			Map<String, Object> fieldValues = new HashMap<>();
			fieldValues.put(fieldName, totalMinutes);
			issueChangeManager.changeFields(issue, fieldValues);
		}
	}

	private void aggregateSourceLinkTimes(Issue issue, String linkName, String fieldName) {
		var totalMinutes = 0;
		for (var sourceLink : issue.getSourceLinks()) {
			if (sourceLink.getSpec().getOpposite() != null && sourceLink.getSpec().getOpposite().getName().equals(linkName)) {
				var minutesOfSourceIssue = sourceLink.getSource().getFieldValue(fieldName);
				if (minutesOfSourceIssue != null)
					totalMinutes += (int) minutesOfSourceIssue;
			}
		}
		if (totalMinutes != 0) {
			Map<String, Object> fieldValues = new HashMap<>();
			fieldValues.put(fieldName, totalMinutes);
			issueChangeManager.changeFields(issue, fieldValues);
		}
	}

	private void trackTime(Long issueId, Runnable runnable) {
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
