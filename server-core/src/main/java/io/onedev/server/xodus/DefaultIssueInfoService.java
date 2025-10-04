package io.onedev.server.xodus;

import static io.onedev.server.util.DateUtils.toLocalDate;
import static java.lang.Long.valueOf;

import java.io.File;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.service.IssueChangeService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.ActiveServerChanged;
import io.onedev.server.event.project.ProjectDeleted;
import io.onedev.server.event.project.issue.IssueChanged;
import io.onedev.server.event.project.issue.IssueDeleted;
import io.onedev.server.event.project.issue.IssueOpened;
import io.onedev.server.event.project.issue.IssuesCopied;
import io.onedev.server.event.project.issue.IssuesDeleted;
import io.onedev.server.event.project.issue.IssuesImported;
import io.onedev.server.event.project.issue.IssuesMoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.support.issue.changedata.IssueBatchUpdateData;
import io.onedev.server.model.support.issue.changedata.IssueOwnSpentTimeChangeData;
import io.onedev.server.model.support.issue.changedata.IssueStateChangeData;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.util.concurrent.BatchWorkExecutionService;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;

@Singleton
public class DefaultIssueInfoService extends AbstractEnvironmentService
		implements IssueInfoService, Serializable {

	private static final int INFO_VERSION = 4;
	
	private static final int BATCH_SIZE = 5000;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultIssueInfoService.class);
	
	private static final String DEFAULT_STORE = "default";
	
	private static final String STATE_HISTORY_STORE = "stateHistory";

	private static final String SPENT_TIME_HISTORY_STORE = "spentTimeHistory";
	
	private static final ByteIterable LAST_ISSUE_KEY = new StringByteIterable("lastIssue");

	private static final ByteIterable LAST_ISSUE_CHANGE_KEY = new StringByteIterable("lastIssueChange");
	
	private static final int UPDATE_PRIORITY = 100;

	private static final int CHECK_PRIORITY = 200;
	
	private final BatchWorkExecutionService batchWorkExecutionService;
	
	private final IssueService issueService;
	
	private final IssueChangeService issueChangeService;
	
	private final ProjectService projectService;
	
	@Inject
	public DefaultIssueInfoService(IssueService issueService, IssueChangeService issueChangeService,
								   BatchWorkExecutionService batchWorkExecutionService, ProjectService projectService) {
		this.issueService = issueService;
		this.issueChangeService = issueChangeService;
		this.batchWorkExecutionService = batchWorkExecutionService;
		this.projectService = projectService;
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(IssueInfoService.class);
	}
	
	private BatchWorker getBatchWorker(Long projectId) {
		return new BatchWorker("project-" + projectId + "-collect-issue-info") {

			@Override
			public void doWorks(List<Prioritized> works) {
				// do the work batch by batch to avoid consuming too much memory
				while (collect(projectId));
			}
			
		};
	}
	
	@Sessional
	protected boolean collect(Long projectId) {
		var projectPath = projectService.findFacadeById(projectId).getPath();
		logger.debug("Collecting issue info (project: {})...", projectPath);
		
		Environment env = getEnv(projectId.toString());
		Store defaultStore = getStore(env, DEFAULT_STORE);
		Store stateHistoryStore = getStore(env, STATE_HISTORY_STORE);
		Store spentTimeHistoryStore = getStore(env, SPENT_TIME_HISTORY_STORE);

		Long lastIssueId = env.computeInTransaction(txn -> readLong(defaultStore, txn, LAST_ISSUE_KEY, 0));
		
		List<Issue> unprocessedIssues = issueService.queryAfter(projectId, lastIssueId, BATCH_SIZE); 
		env.executeInTransaction(txn -> {
			Issue lastIssue = null;
			for (Issue issue: unprocessedIssues) {
				initStateHistory(stateHistoryStore, txn, issue);
				initSpentTimeHistory(spentTimeHistoryStore, txn, issue);
				lastIssue = issue;
			}
			if (lastIssue != null)
				defaultStore.put(txn, LAST_ISSUE_KEY, new LongByteIterable(lastIssue.getId()));
		});
		
		Long lastChangeId = env.computeInTransaction(txn -> readLong(defaultStore, txn, LAST_ISSUE_CHANGE_KEY, 0));
		
		List<IssueChange> unprocessedChanges = issueChangeService.queryAfter(projectId, lastChangeId, BATCH_SIZE); 
		env.executeInTransaction(txn -> {
			IssueChange lastChange = null;
			
			for (IssueChange change: unprocessedChanges) {
				Issue issue = change.getIssue();
				initStateHistory(stateHistoryStore, txn, issue);
				initSpentTimeHistory(spentTimeHistoryStore, txn, issue);
				
				String state = null;
				if (change.getData() instanceof IssueStateChangeData) {
					IssueStateChangeData changeData = (IssueStateChangeData) change.getData();
					if (!changeData.getOldState().equals(changeData.getNewState()))
						state = changeData.getNewState();
				} else if (change.getData() instanceof IssueBatchUpdateData) {
					IssueBatchUpdateData changeData = (IssueBatchUpdateData) change.getData();
					if (!changeData.getOldState().equals(changeData.getNewState()))
						state = changeData.getNewState();
				}

				if (state != null) {
					ArrayByteIterable issueKey = new LongByteIterable(issue.getId());
					byte[] bytes = Preconditions.checkNotNull(readBytes(stateHistoryStore, txn, issueKey));
					@SuppressWarnings("unchecked")
					Map<Long, String> stateHistory = (Map<Long, String>) SerializationUtils.deserialize(bytes);
					stateHistory.put(toLocalDate(change.getDate(), ZoneId.systemDefault()).toEpochDay(), state);
					stateHistoryStore.put(txn, issueKey, new ArrayByteIterable(SerializationUtils.serialize((Serializable) stateHistory)));
				}

				if (change.getData() instanceof IssueOwnSpentTimeChangeData) {
					IssueOwnSpentTimeChangeData changeData = (IssueOwnSpentTimeChangeData) change.getData();
					int spentTime = changeData.getNewValue();
					ArrayByteIterable issueKey = new LongByteIterable(issue.getId());
					byte[] bytes = Preconditions.checkNotNull(readBytes(spentTimeHistoryStore, txn, issueKey));
					@SuppressWarnings("unchecked")
					Map<Long, Integer> spentTimeHistory = (Map<Long, Integer>) SerializationUtils.deserialize(bytes);
					spentTimeHistory.put(toLocalDate(change.getDate(), ZoneId.systemDefault()).toEpochDay(), spentTime);
					spentTimeHistoryStore.put(txn, issueKey, new ArrayByteIterable(SerializationUtils.serialize((Serializable) spentTimeHistory)));
				}
				
				lastChange = change;
			}
			if (lastChange != null)
				defaultStore.put(txn, LAST_ISSUE_CHANGE_KEY, new LongByteIterable(lastChange.getId()));
		});
		
		logger.debug("Collected issue info (project: {})", projectPath);
		
		return unprocessedIssues.size() == BATCH_SIZE;
	}
	
	private void initStateHistory(Store stateHistoryStore, Transaction txn, Issue issue) {
		ArrayByteIterable issueKey = new LongByteIterable(issue.getId());
		byte[] bytes = readBytes(stateHistoryStore, txn, issueKey);
		if (bytes == null) {
			Map<Long, String> stateHistory = new LinkedHashMap<>(); 
			stateHistory.put(toLocalDate(issue.getSubmitDate(), ZoneId.systemDefault()).toEpochDay(), issue.getState());
			stateHistoryStore.put(txn, issueKey, new ArrayByteIterable(SerializationUtils.serialize((Serializable) stateHistory)));
		}
	}

	private void initSpentTimeHistory(Store spentTimeHistoryStore, Transaction txn, Issue issue) {
		ArrayByteIterable issueKey = new LongByteIterable(issue.getId());
		byte[] bytes = readBytes(spentTimeHistoryStore, txn, issueKey);
		if (bytes == null) {
			Map<Long, Integer> spentTimeHistory = new LinkedHashMap<>();
			spentTimeHistory.put(toLocalDate(issue.getSubmitDate(), ZoneId.systemDefault()).toEpochDay(), issue.getOwnSpentTime());
			spentTimeHistoryStore.put(txn, issueKey, new ArrayByteIterable(SerializationUtils.serialize((Serializable) spentTimeHistory)));
		}
	}
	
	private void remove(Long projectId, Long issueId) {
		Environment env = getEnv(projectId.toString());
		Store stateHistoryStore = getStore(env, STATE_HISTORY_STORE);
		env.executeInTransaction(txn -> stateHistoryStore.delete(txn, new LongByteIterable(issueId)));
		Store spentTimeHistoryStore = getStore(env, SPENT_TIME_HISTORY_STORE);
		env.executeInTransaction(txn -> spentTimeHistoryStore.delete(txn, new LongByteIterable(issueId)));
	}
	
	@Sessional
	@Listen
	public void on(ProjectDeleted event) {
		removeEnv(event.getProjectId().toString());
	}

	@Sessional
	@Listen
	public void on(IssueOpened event) {
		batchWorkExecutionService.submit(getBatchWorker(event.getProject().getId()), new Prioritized(UPDATE_PRIORITY));
	}

	@Sessional
	@Listen
	public void on(IssuesCopied event) {
		batchWorkExecutionService.submit(getBatchWorker(event.getProject().getId()), new Prioritized(UPDATE_PRIORITY));
	}

	@Sessional
	@Listen
	public void on(IssuesImported event) {
		batchWorkExecutionService.submit(getBatchWorker(event.getProject().getId()), new Prioritized(UPDATE_PRIORITY));
	}

	@Sessional
	@Listen
	public void on(IssueChanged event) {
		batchWorkExecutionService.submit(getBatchWorker(event.getProject().getId()), new Prioritized(UPDATE_PRIORITY));
	}
	
	@Sessional
	@Listen
	public void on(IssuesDeleted event) {
		for (var issueId: event.getIssueIds()) 
			remove(event.getProject().getId(), issueId);
	}

	@Sessional
	@Listen
	public void on(IssueDeleted event) {
		remove(event.getProject().getId(), event.getIssueId());
	}
	
	@Sessional
	@Listen
	public void on(IssuesMoved event) {
		Long sourceProjectId = event.getSourceProject().getId();
		projectService.submitToActiveServer(sourceProjectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() {
				try {
					event.getIssueIds().forEach(it -> remove(sourceProjectId, it));
				} catch (Exception e) {
					logger.error("Error removing issue info", e);
				}
				return null;
			}

		});
		
		batchWorkExecutionService.submit(getBatchWorker(event.getProject().getId()), new Prioritized(UPDATE_PRIORITY));
	}

	@Sessional
	@Listen
	public void on(SystemStarted event) {
		var activeProjectIds = projectService.getActiveIds();
		var issueProjectIds = new HashSet<Long>(issueService.getProjectIds());		
		for (var projectId: activeProjectIds) {
			checkVersion(getEnvDir(projectId.toString()));
			if (issueProjectIds.contains(projectId))
				batchWorkExecutionService.submit(getBatchWorker(projectId), new Prioritized(CHECK_PRIORITY));
		}
	}

	@Sessional
	@Listen
	public void on(ActiveServerChanged event) {
		for (var projectId: event.getProjectIds()) {
			checkVersion(getEnvDir(projectId.toString()));
			batchWorkExecutionService.submit(getBatchWorker(projectId), new Prioritized(CHECK_PRIORITY));
		}
	}
	
	@Override
	protected File getEnvDir(String envKey) {
		File infoDir = new File(projectService.getInfoDir(valueOf(envKey)), "issue");
		FileUtils.createDir(infoDir);
		return infoDir;
	}

	@Override
	protected int getEnvVersion() {
		return INFO_VERSION;
	}

	private <T> Map<Long, T> getDailyMetrics(Issue issue, String metricStore, Long fromDay, Long toDay) {
		Long projectId = issue.getProject().getId();
		Long issueId = issue.getId();

		return projectService.runOnActiveServer(projectId, new ClusterTask<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Map<Long, T> call() {
				Environment env = getEnv(projectId.toString());
				Store metricHistoryStore = getStore(env, metricStore);

				return env.computeInTransaction(txn -> {
					Map<Long, T> dailyMetrics = new LinkedHashMap<>();
					byte[] bytes = readBytes(metricHistoryStore, txn, new LongByteIterable(issueId));
					if (bytes != null) {
						@SuppressWarnings("unchecked")
						var metricHistory = (Map<Long, T>) SerializationUtils.deserialize(bytes);
						T currentMetric = null;
						for (var entry : metricHistory.entrySet()) {
							if (entry.getKey() <= fromDay)
								currentMetric = entry.getValue();
							else
								break;
						}

						long currentDay = fromDay;
						while (currentDay <= toDay) {
							T metricOnDay = metricHistory.get(currentDay);
							if (metricOnDay != null)
								currentMetric = metricOnDay;
							dailyMetrics.put(currentDay, currentMetric);
							currentDay++;
						}
					}

					return dailyMetrics;
				});
			}

		});
	}
	
	@Sessional
	@Override
	public Map<Long, String> getDailyStates(Issue issue, Long fromDay, Long toDay) {
		return getDailyMetrics(issue, STATE_HISTORY_STORE, fromDay, toDay);
	}

	@Override
	public Map<Long, Integer> getDailySpentTimes(Issue issue, Long fromDay, Long toDay) {
		Map<Long, Integer> dailySpentTimes = getDailyMetrics(issue, SPENT_TIME_HISTORY_STORE, fromDay, toDay);
		for (var entry: dailySpentTimes.entrySet()) {
			if (entry.getValue() == null)
				entry.setValue(0);
		}
		return dailySpentTimes;
	}

}
