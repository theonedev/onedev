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
import io.onedev.server.model.support.issue.changedata.IssueFieldChangeData;
import io.onedev.server.model.support.issue.changedata.IssueLinkChangeData;
import io.onedev.server.model.support.issue.changedata.IssueOwnEstimatedTimeChangeData;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.util.ThreadContext;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
			if (timeTrackingSetting != null && timeTrackingSetting.isProjectApplicable(issue.getProject())
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
							syncEstimatedTime(affectedIssue, timeTrackingSetting.getEstimatedTimeField(), timeAggregationLink, BOTH);
							syncSpentTime(affectedIssue, timeTrackingSetting.getSpentTimeField(), timeAggregationLink, BOTH);
						}
					});
				}
			}
		} else if (event.getEntity() instanceof IssueWork) {
			var issueWork = (IssueWork) event.getEntity();
			var issue = issueWork.getIssue();
			var timeTrackingSetting = settingManager.getIssueSetting().getTimeTrackingSetting();
			if (timeTrackingSetting != null && timeTrackingSetting.isProjectApplicable(issue.getProject())) {
				var issueId = issue.getId();
				runAsyncAfterCommit(issueId, () -> {
					var innerIssue = issueManager.load(issueId);
					syncSpentTime(innerIssue, timeTrackingSetting.getSpentTimeField(),
							timeTrackingSetting.getTimeAggregationLink(), BOTH);
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
			var timeTrackingSetting = settingManager.getIssueSetting().getTimeTrackingSetting();
			if (timeTrackingSetting != null && timeTrackingSetting.isProjectApplicable(issue.getProject())) {
				var issueId = issue.getId();
				runAsyncAfterCommit(issueId, () -> {
					var innerIssue = issueManager.load(issueId);
					syncSpentTime(innerIssue, timeTrackingSetting.getSpentTimeField(),
							timeTrackingSetting.getTimeAggregationLink(), BOTH);
				});
			}
		} else if (event.getEntity() instanceof IssueChange) {
			IssueChange issueChange = (IssueChange) event.getEntity();
			var issue = issueChange.getIssue();
			var issueId = issue.getId();
			var timeTrackingSetting = settingManager.getIssueSetting().getTimeTrackingSetting();
			if (timeTrackingSetting != null && timeTrackingSetting.isProjectApplicable(issue.getProject())) {
				var estimatedTimeField = timeTrackingSetting.getEstimatedTimeField();
				var spentTimeField = timeTrackingSetting.getSpentTimeField();
				var timeAggregationLink = timeTrackingSetting.getTimeAggregationLink();
				if (issueChange.getData() instanceof IssueOwnEstimatedTimeChangeData) {
					runAsyncAfterCommit(issueId, () -> {
						var innerIssue = issueManager.load(issueId);
						syncEstimatedTime(innerIssue, timeTrackingSetting.getEstimatedTimeField(), 
								timeAggregationLink, BOTH);
					});
				} else if (issueChange.getData() instanceof IssueFieldChangeData) {
					if (timeAggregationLink != null) {
						var processedIssueIds = processedIssueIdsHolder.get();
						if (!processedIssueIds.contains(issueId)) {
							IssueFieldChangeData changeData = (IssueFieldChangeData) issueChange.getData();

							var fieldNames = new HashSet<String>();
							fieldNames.addAll(changeData.getOldFields().keySet());
							fieldNames.addAll(changeData.getNewFields().keySet());

							for (var fieldName : fieldNames) {
								if (fieldName.equals(estimatedTimeField) || fieldName.equals(spentTimeField)) {
									runAsyncAfterCommit(issueId, () -> {
										var copyOfProcessedIssueIds = new HashSet<>(processedIssueIds);
										copyOfProcessedIssueIds.add(issueId);
										processedIssueIdsHolder.set(copyOfProcessedIssueIds);
										try {
											var innerIssue = issueManager.load(issueId);
											for (var sourceLink : innerIssue.getSourceLinks()) {
												if (sourceLink.getSpec().getName().equals(timeAggregationLink)) {
													var sourceIssue = sourceLink.getSource();
													if (fieldName.equals(estimatedTimeField))
														syncEstimatedTime(sourceIssue, fieldName, timeAggregationLink, TARGET);
													else
														syncSpentTime(sourceIssue, fieldName, timeAggregationLink, TARGET);
												}
											}
											for (var targetLink : innerIssue.getTargetLinks()) {
												if (targetLink.getSpec().getOpposite() != null
														&& targetLink.getSpec().getOpposite().getName().equals(timeAggregationLink)) {
													var targetIssue = targetLink.getTarget();
													if (fieldName.equals(estimatedTimeField))
														syncEstimatedTime(targetIssue, fieldName, timeAggregationLink, SOURCE);
													else
														syncSpentTime(targetIssue, fieldName, timeAggregationLink, SOURCE);
												}
											}
										} finally {
											processedIssueIdsHolder.set(new HashSet<>());
										}
									});
								}
							}
						}
					}
				} else if (issueChange.getData() instanceof IssueLinkChangeData) {
					IssueLinkChangeData changeData = (IssueLinkChangeData) issueChange.getData();
					if (changeData.getLinkName().equals(timeAggregationLink)) {
						runAsyncAfterCommit(issueId, () -> {
							var innerIssue = issueManager.load(issueId);
							if (!changeData.isOpposite()) {
								syncEstimatedTime(innerIssue, estimatedTimeField, changeData.getLinkName(), TARGET);
								syncSpentTime(innerIssue, spentTimeField, changeData.getLinkName(), TARGET);
							} else {
								syncEstimatedTime(innerIssue, estimatedTimeField, changeData.getLinkName(), SOURCE);
								syncSpentTime(innerIssue, spentTimeField, changeData.getLinkName(), SOURCE);
							}
						});
					}
				}
			}
		}
	}

	@Override
	public void syncEstimatedTime(Issue issue, String estimatedTimeField, @Nullable String aggregationLink, 
								  TimeAggregationDirection direction) {
		int totalTime = issue.getOwnEstimatedTime();
		if (aggregationLink != null) {
			if (direction == TARGET || direction == BOTH)
				totalTime += aggregateTargetLinkTimes(issue, estimatedTimeField, aggregationLink);
			if (direction == SOURCE || direction == BOTH)
				totalTime += aggregateSourceLinkTimes(issue, estimatedTimeField, aggregationLink);
		}
		writeTime(issue, estimatedTimeField, totalTime);
	}

	@Override
	public void syncSpentTime(Issue issue, String spentTimeField, @Nullable String aggregationLink, 
							  TimeAggregationDirection direction) {
		int totalTime = issue.getWorks().stream().mapToInt(IssueWork::getMinutes).sum();
		if (aggregationLink != null) {
			if (direction == TARGET || direction == BOTH)
				totalTime += aggregateTargetLinkTimes(issue, spentTimeField, aggregationLink);
			if (direction == SOURCE || direction == BOTH)
				totalTime += aggregateSourceLinkTimes(issue, spentTimeField, aggregationLink);
		}
		writeTime(issue, spentTimeField, totalTime);
	}

	private void writeTime(Issue issue, String fieldName, int time) {
		Map<String, Object> fieldValues = new HashMap<>();
		fieldValues.put(fieldName, time);
		issueChangeManager.changeFields(issue, fieldValues);
	}
	
	@Override
	public int aggregateTargetLinkTimes(Issue issue, String fieldName, String linkName) {
		int totalTime = 0;
		for (var targetLink : issue.getTargetLinks()) {
			if (targetLink.getSpec().getName().equals(linkName)) {
				var time = targetLink.getTarget().getFieldValue(fieldName);
				if (time != null) 
					totalTime += (int) time;
			}
		}
		return totalTime;
	}
	
	@Override
	public int aggregateSourceLinkTimes(Issue issue, String fieldName, String linkName) {
		var totalTime = 0;
		for (var sourceLink : issue.getSourceLinks()) {
			if (sourceLink.getSpec().getOpposite() != null && sourceLink.getSpec().getOpposite().getName().equals(linkName)) {
				var time = sourceLink.getSource().getFieldValue(fieldName);
				if (time != null)
					totalTime += (int) time;
			}
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
