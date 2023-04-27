package io.onedev.server.infomanager;

import com.google.common.base.Preconditions;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.ProjectDeleted;
import io.onedev.server.event.project.ActiveServerChanged;
import io.onedev.server.event.project.issue.*;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.support.issue.changedata.IssueBatchUpdateData;
import io.onedev.server.model.support.issue.changedata.IssueStateChangeData;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.util.Day;
import io.onedev.server.util.concurrent.BatchWorkManager;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Long.valueOf;

@Singleton
public class DefaultIssueInfoManager extends AbstractMultiEnvironmentManager 
		implements IssueInfoManager, Serializable {

	private static final int INFO_VERSION = 1;
	
	private static final int BATCH_SIZE = 5000;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultIssueInfoManager.class);
	
	private static final String DEFAULT_STORE = "default";
	
	private static final String STATE_HISTORY_STORE = "stateHistory";
	
	private static final ByteIterable LAST_ISSUE_KEY = new StringByteIterable("lastIssue");

	private static final ByteIterable LAST_ISSUE_CHANGE_KEY = new StringByteIterable("lastIssueChange");
	
	private static final int PRIORITY = 100;
	
	private final BatchWorkManager batchWorkManager;
	
	private final IssueManager issueManager;
	
	private final IssueChangeManager issueChangeManager;
	
	private final ClusterManager clusterManager;
	
	private final ProjectManager projectManager;
	
	@Inject
	public DefaultIssueInfoManager(IssueManager issueManager, IssueChangeManager issueChangeManager, 
								   BatchWorkManager batchWorkManager, ProjectManager projectManager, 
								   ClusterManager clusterManager) {
		this.issueManager = issueManager;
		this.issueChangeManager = issueChangeManager;
		this.batchWorkManager = batchWorkManager;
		this.projectManager = projectManager;
		this.clusterManager = clusterManager;
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(IssueInfoManager.class);
	}
	
	private BatchWorker getBatchWorker(Long projectId) {
		return new BatchWorker("project-" + projectId + "-collectIssueInfo") {

			@Override
			public void doWorks(List<Prioritized> works) {
				// do the work batch by batch to avoid consuming too much memory
				while (collect(projectId));
			}
			
		};
	}
	
	@Sessional
	protected boolean collect(Long projectId) {
		logger.debug("Collecting issue info...");
		
		Environment env = getEnv(projectId.toString());
		Store defaultStore = getStore(env, DEFAULT_STORE);
		Store stateHistoryStore = getStore(env, STATE_HISTORY_STORE);

		Long lastIssueId = env.computeInTransaction(txn -> readLong(defaultStore, txn, LAST_ISSUE_KEY, 0));
		
		List<Issue> unprocessedIssues = issueManager.queryAfter(projectId, lastIssueId, BATCH_SIZE); 
		env.executeInTransaction(txn -> {
			Issue lastIssue = null;
			for (Issue issue: unprocessedIssues) {
				initStateHistory(stateHistoryStore, txn, issue);
				lastIssue = issue;
			}
			if (lastIssue != null)
				defaultStore.put(txn, LAST_ISSUE_KEY, new LongByteIterable(lastIssue.getId()));
		});
		
		Long lastChangeId = env.computeInTransaction(txn -> readLong(defaultStore, txn, LAST_ISSUE_CHANGE_KEY, 0));
		
		List<IssueChange> unprocessedChanges = issueChangeManager.queryAfter(projectId, lastChangeId, BATCH_SIZE); 
		env.executeInTransaction(txn -> {
			IssueChange lastChange = null;
			
			for (IssueChange change: unprocessedChanges) {
				Issue issue = change.getIssue();
				initStateHistory(stateHistoryStore, txn, issue);
				
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
					Map<Integer, String> stateHistory = (Map<Integer, String>) SerializationUtils.deserialize(bytes);
					stateHistory.put(new Day(change.getDate()).getValue(), state);
					stateHistoryStore.put(txn, issueKey, new ArrayByteIterable(SerializationUtils.serialize((Serializable) stateHistory)));
				}
				
				lastChange = change;
			}
			if (lastChange != null)
				defaultStore.put(txn, LAST_ISSUE_CHANGE_KEY, new LongByteIterable(lastChange.getId()));
		});
		
		logger.debug("Collected issue info");
		
		return unprocessedIssues.size() == BATCH_SIZE;
	}
	
	private void initStateHistory(Store stateHistoryStore, Transaction txn, Issue issue) {
		ArrayByteIterable issueKey = new LongByteIterable(issue.getId());
		byte[] bytes = readBytes(stateHistoryStore, txn, issueKey);
		if (bytes == null) {
			Map<Integer, String> stateHistory = new LinkedHashMap<>(); 
			Day day = new Day(issue.getSubmitDate());
			stateHistory.put(day.getValue(), issue.getState());
			stateHistoryStore.put(txn, issueKey, new ArrayByteIterable(SerializationUtils.serialize((Serializable) stateHistory)));
		}
	}
	
	private void remove(Long projectId, Long issueId) {
		Environment env = getEnv(projectId.toString());
		Store stateHistoryStore = getStore(env, STATE_HISTORY_STORE);
		env.executeInTransaction(txn -> stateHistoryStore.delete(txn, new LongByteIterable(issueId)));
	}
	
	@Sessional
	@Listen
	public void on(ProjectDeleted event) {
		removeEnv(event.getProjectId().toString());
	}

	@Sessional
	@Listen
	public void on(IssueOpened event) {
		batchWorkManager.submit(getBatchWorker(event.getProject().getId()), new Prioritized(PRIORITY));
	}

	@Sessional
	@Listen
	public void on(IssuesCopied event) {
		batchWorkManager.submit(getBatchWorker(event.getProject().getId()), new Prioritized(PRIORITY));
	}

	@Sessional
	@Listen
	public void on(IssuesImported event) {
		batchWorkManager.submit(getBatchWorker(event.getProject().getId()), new Prioritized(PRIORITY));
	}

	@Sessional
	@Listen
	public void on(IssueChanged event) {
		batchWorkManager.submit(getBatchWorker(event.getProject().getId()), new Prioritized(PRIORITY));
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
		projectManager.submitToActiveServer(sourceProjectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				try {
					event.getIssueIds().forEach(it -> remove(sourceProjectId, it));
				} catch (Exception e) {
					logger.error("Error removing issue info", e);
				}
				return null;
			}

		});
		
		batchWorkManager.submit(getBatchWorker(event.getProject().getId()), new Prioritized(PRIORITY));
	}

	@Sessional
	@Listen
	public void on(SystemStarted event) {
		var localServer = clusterManager.getLocalServerAddress();
		for (var entry: projectManager.getActiveServers().entrySet()) {
			var projectId = entry.getKey();
			var activeServer = entry.getValue();
			if (localServer.equals(activeServer)) {
				checkVersion(getEnvDir(projectId.toString()));
				batchWorkManager.submit(getBatchWorker(projectId), new Prioritized(PRIORITY));
			}
		}
	}

	@Sessional
	@Listen
	public void on(ActiveServerChanged event) {
		for (var projectId: event.getProjectIds()) {
			checkVersion(getEnvDir(projectId.toString()));
			batchWorkManager.submit(getBatchWorker(projectId), new Prioritized(PRIORITY));
		}
	}
	
	@Override
	protected File getEnvDir(String envKey) {
		File infoDir = new File(projectManager.getInfoDir(valueOf(envKey)), "issue");
		FileUtils.createDir(infoDir);
		return infoDir;
	}

	@Override
	protected int getEnvVersion() {
		return INFO_VERSION;
	}

	@Sessional
	@Override
	public Map<Integer, String> getDailyStates(Issue issue, Integer fromDay, Integer toDay) {
		Long projectId = issue.getProject().getId();
		Long issueId = issue.getId();
		
		return projectManager.runOnActiveServer(projectId, new ClusterTask<Map<Integer, String>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Map<Integer, String> call() throws Exception {
				Environment env = getEnv(projectId.toString());
				Store stateHistoryStore = getStore(env, STATE_HISTORY_STORE);
				
				return env.computeInTransaction(txn -> {
					Map<Integer, String> dailyStates = new LinkedHashMap<>();
					byte[] bytes = readBytes(stateHistoryStore, txn, new LongByteIterable(issueId));
					if (bytes != null) {
						Map<Integer, String> stateHistory = (Map<Integer, String>) SerializationUtils.deserialize(bytes);
						String currentState = null;
						for (Map.Entry<Integer, String> entry: stateHistory.entrySet()) {
							if (entry.getKey()<=fromDay) 
								currentState = entry.getValue();
							else
								break;
						}
						
						int currentDay = fromDay;
						while (currentDay <= toDay) {
							String stateOnDay = stateHistory.get(currentDay);
							if (stateOnDay != null) 
								currentState = stateOnDay;
							dailyStates.put(currentDay, currentState);
							currentDay = new Day(new Day(currentDay).getDate().plusDays(1)).getValue();
						}
					} 
					
					return dailyStates;
				});
			}
			
		});
	}

}
