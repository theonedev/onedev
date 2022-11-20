package io.onedev.server.infomanager;

import java.io.File;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.changedata.IssueBatchUpdateData;
import io.onedev.server.model.support.issue.changedata.IssueStateChangeData;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.Day;
import io.onedev.server.util.concurrent.BatchWorkManager;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalComputable;
import jetbrains.exodus.env.TransactionalExecutable;

@Singleton
public class DefaultIssueInfoManager extends AbstractMultiEnvironmentManager 
		implements IssueInfoManager, Serializable {

	private static final int INFO_VERSION = 1;
	
	private static final int BATCH_SIZE = 5000;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultIssueInfoManager.class);
	
	private static final String INFO_DIR = "issue";
	
	private static final String DEFAULT_STORE = "default";
	
	private static final String STATE_HISTORY_STORE = "stateHistory";
	
	private static final ByteIterable LAST_ISSUE_KEY = new StringByteIterable("lastIssue");

	private static final ByteIterable LAST_ISSUE_CHANGE_KEY = new StringByteIterable("lastIssueChange");
	
	private static final int PRIORITY = 100;
	
	private final StorageManager storageManager;
	
	private final BatchWorkManager batchWorkManager;
	
	private final IssueManager issueManager;
	
	private final IssueChangeManager issueChangeManager;
	
	private final TransactionManager transactionManager;
	
	private final ProjectManager projectManager;
	
	private final ClusterManager clusterManager;
	
	@Inject
	public DefaultIssueInfoManager(TransactionManager transactionManager, 
			StorageManager storageManager, IssueManager issueManager, 
			IssueChangeManager issueChangeManager, BatchWorkManager batchWorkManager, 
			ProjectManager projectManager, ClusterManager clusterManager) {
		this.storageManager = storageManager;
		this.issueManager = issueManager;
		this.issueChangeManager = issueChangeManager;
		this.batchWorkManager = batchWorkManager;
		this.transactionManager = transactionManager;
		this.projectManager = projectManager;
		this.clusterManager = clusterManager;
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(IssueInfoManager.class);
	}
	
	private BatchWorker getBatchWorker(Long projectId) {
		return new BatchWorker("project-" + projectId + "-collectIssueInfo") {

			@Override
			public void doWorks(Collection<Prioritized> works) {
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

		Long lastIssueId = env.computeInTransaction(new TransactionalComputable<Long>() {
			
			@Override
			public Long compute(Transaction txn) {
				return readLong(defaultStore, txn, LAST_ISSUE_KEY, 0);
			}
			
		});
		
		List<Issue> unprocessedIssues = issueManager.queryAfter(projectId, lastIssueId, BATCH_SIZE); 
		env.executeInTransaction(new TransactionalExecutable() {

			@Override
			public void execute(Transaction txn) {
				Issue lastIssue = null;
				for (Issue issue: unprocessedIssues) {
					initStateHistory(stateHistoryStore, txn, issue);
					lastIssue = issue;
				}
				if (lastIssue != null)
					defaultStore.put(txn, LAST_ISSUE_KEY, new LongByteIterable(lastIssue.getId()));
			}
			
		});
		
		Long lastChangeId = env.computeInTransaction(new TransactionalComputable<Long>() {
			
			@Override
			public Long compute(Transaction txn) {
				return readLong(defaultStore, txn, LAST_ISSUE_CHANGE_KEY, 0);
			}
			
		});
		
		List<IssueChange> unprocessedChanges = issueChangeManager.queryAfter(projectId, lastChangeId, BATCH_SIZE); 
		env.executeInTransaction(new TransactionalExecutable() {

			@SuppressWarnings("unchecked")
			@Override
			public void execute(Transaction txn) {
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
			}
			
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
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.isNew()) {
			Long projectId;
			if (event.getEntity() instanceof Issue)
				projectId = ((Issue)event.getEntity()).getProject().getId();
			else if (event.getEntity() instanceof IssueChange)
				projectId = ((IssueChange)event.getEntity()).getIssue().getProject().getId();
			else
				projectId = null;
				
			if(projectId != null) {
				if (event.getEntity() instanceof Issue) {
					Issue issue = (Issue) event.getEntity();
					Long oldProjectId;
					if (issue.getOldVersion() != null) 
						oldProjectId = issue.getOldVersion().getProjectId();
					else 
						oldProjectId = null;
					if (oldProjectId != null && !projectId.equals(oldProjectId)) {
						Long issueId = issue.getId();
						transactionManager.runAfterCommit(new ClusterRunnable() {
							
							private static final long serialVersionUID = 1L;

							@Override
							public void run() {
								projectManager.submitToProjectServer(oldProjectId, new ClusterTask<Void>() {

									private static final long serialVersionUID = 1L;

									@Override
									public Void call() throws Exception {
										remove(oldProjectId, issueId);
										return null;
									}
									
								});
							}
							
						});
					}
				}
				transactionManager.runAfterCommit(new ClusterRunnable() {
	
					private static final long serialVersionUID = 1L;

					@Override
					public void run() {
						projectManager.submitToProjectServer(projectId, new ClusterTask<Void>() {

							private static final long serialVersionUID = 1L;

							@Override
							public Void call() throws Exception {
								batchWorkManager.submit(getBatchWorker(projectId), new Prioritized(PRIORITY));
								return null;
							}
							
						});
					}
					
				});
			}
		}
	}
	
	private void remove(Long projectId, Long issueId) {
		Environment env = getEnv(projectId.toString());
		Store stateHistoryStore = getStore(env, STATE_HISTORY_STORE);
		env.executeInTransaction(new TransactionalExecutable() {

			@Override
			public void execute(Transaction txn) {
				stateHistoryStore.delete(txn, new LongByteIterable(issueId));
			}
			
		});
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Issue) {
			Issue issue = (Issue) event.getEntity();
			Long projectId = issue.getProject().getId(); 
			Long issueId = issue.getId();
			transactionManager.runAfterCommit(new ClusterRunnable() {

				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					projectManager.submitToProjectServer(projectId, new ClusterTask<Void>() {

						private static final long serialVersionUID = 1L;

						@Override
						public Void call() throws Exception {
							remove(projectId, issueId);
							return null;
						}
						
					});
				}
				
			});
		} else if (event.getEntity() instanceof Project) {
			Long projectId = event.getEntity().getId();
			UUID storageServerUUID = projectManager.getStorageServerUUID(projectId, false);
			if (storageServerUUID != null) {
				clusterManager.runOnServer(storageServerUUID, new ClusterTask<Void>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Void call() throws Exception {
						removeEnv(projectId.toString());
						return null;
					}
					
				});
			}
		} 
	}
	
	@Sessional
	@Listen
	public void on(SystemStarted event) {
		Collection<Long> projectIds = projectManager.getIds();
		for (File file: storageManager.getProjectsDir().listFiles()) {
			Long projectId = Long.valueOf(file.getName());
			if (projectIds.contains(projectId)) {
				checkVersion(getEnvDir(projectId.toString()));
				batchWorkManager.submit(getBatchWorker(projectId), new Prioritized(PRIORITY));
			}
		}
	}
	
	@Override
	protected File getEnvDir(String envKey) {
		File infoDir = new File(storageManager.getProjectInfoDir(Long.valueOf(envKey)), INFO_DIR);
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
		
		return projectManager.runOnProjectServer(projectId, new ClusterTask<Map<Integer, String>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Map<Integer, String> call() throws Exception {
				Environment env = getEnv(projectId.toString());
				Store stateHistoryStore = getStore(env, STATE_HISTORY_STORE);
				
				return env.computeInTransaction(new TransactionalComputable<Map<Integer, String>>() {

					@SuppressWarnings("unchecked")
					@Override
					public Map<Integer, String> compute(Transaction txn) {
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
					}
					
				});
			}
			
		});
	}

}
