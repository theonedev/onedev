package io.onedev.server.infomanager;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.loader.Listen;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.support.issue.changedata.IssueBatchUpdateData;
import io.onedev.server.model.support.issue.changedata.IssueStateChangeData;
import io.onedev.server.persistence.SessionManager;
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
public class DefaultIssueInfoManager extends AbstractSingleEnvironmentManager 
		implements IssueInfoManager {

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
	
	private final SessionManager sessionManager;
	
	private final TransactionManager transactionManager;
	
	@Inject
	public DefaultIssueInfoManager(TransactionManager transactionManager, StorageManager storageManager, 
			IssueManager issueManager, IssueChangeManager issueChangeManager, 
			BatchWorkManager batchWorkManager, SessionManager sessionManager) {
		this.storageManager = storageManager;
		this.issueManager = issueManager;
		this.issueChangeManager = issueChangeManager;
		this.batchWorkManager = batchWorkManager;
		this.sessionManager = sessionManager;
		this.transactionManager = transactionManager;
	}
	
	private BatchWorker getBatchWorker() {
		return new BatchWorker("collectIssueInfo") {

			@Override
			public void doWorks(Collection<Prioritized> works) {
				boolean hasMore;
				do {
					// do the work batch by batch to avoid consuming too much memory
					hasMore = sessionManager.call(new Callable<Boolean>() {

						@Override
						public Boolean call() throws Exception {
							return collect();
						}
						
					});
				} while (hasMore);
			}
			
		};
	}
	
	private boolean collect() {
		logger.debug("Collecting issue info...");
		
		Environment env = getEnv();
		Store defaultStore = getStore(env, DEFAULT_STORE);
		Store stateHistoryStore = getStore(env, STATE_HISTORY_STORE);

		Long lastIssueId = env.computeInTransaction(new TransactionalComputable<Long>() {
			
			@Override
			public Long compute(Transaction txn) {
				return readLong(defaultStore, txn, LAST_ISSUE_KEY, 0);
			}
			
		});
		
		List<Issue> unprocessedIssues = issueManager.queryAfter(lastIssueId, BATCH_SIZE); 
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
		
		List<IssueChange> unprocessedChanges = issueChangeManager.queryAfter(lastChangeId, BATCH_SIZE); 
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
		if (event.isNew() && (event.getEntity() instanceof Issue || event.getEntity() instanceof IssueChange)) {
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					batchWorkManager.submit(getBatchWorker(), new Prioritized(PRIORITY));
				}
				
			});
		}
	}

	@Sessional
	@Listen
	public void on(SystemStarted event) {
		checkVersion(getEnvDir());
		batchWorkManager.submit(getBatchWorker(), new Prioritized(PRIORITY));
	}
	
	@Override
	protected File getEnvDir() {
		File infoDir = new File(storageManager.getInfoDir(), INFO_DIR);
		if (!infoDir.exists()) 
			FileUtils.createDir(infoDir);
		return infoDir;
	}

	@Override
	protected int getEnvVersion() {
		return INFO_VERSION;
	}

	@Override
	public Map<Integer, String> getDailyStates(Long issueId, Integer fromDay, Integer toDay) {
		Environment env = getEnv();
		Store stateHistoryStore = getStore(env, STATE_HISTORY_STORE);
		
		return env.computeInTransaction(new TransactionalComputable<Map<Integer, String>>() {

			@SuppressWarnings("unchecked")
			@Override
			public Map<Integer, String> compute(Transaction txn) {
				Map<Integer, String> stateHistory = (Map<Integer, String>) 
						SerializationUtils.deserialize(readBytes(stateHistoryStore, txn, new LongByteIterable(issueId)));
				String currentState = null;
				for (Map.Entry<Integer, String> entry: stateHistory.entrySet()) {
					if (entry.getKey()<=fromDay) 
						currentState = entry.getValue();
					else
						break;
				}
				
				Map<Integer, String> dailyStates = new LinkedHashMap<>();
				int currentDay = fromDay;
				while (currentDay <= toDay) {
					String stateOnDay = stateHistory.get(currentDay);
					if (stateOnDay != null) 
						currentState = stateOnDay;
					dailyStates.put(currentDay, currentState);
					currentDay = new Day(new Day(currentDay).getDate().plusDays(1)).getValue();
				}
				
				return dailyStates;
			}
			
		});
	}

}
