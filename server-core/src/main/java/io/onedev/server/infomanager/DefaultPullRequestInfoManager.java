package io.onedev.server.infomanager;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestUpdateManager;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.concurrent.Prioritized;
import io.onedev.server.util.work.BatchWorkManager;
import io.onedev.server.util.work.BatchWorker;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalComputable;
import jetbrains.exodus.env.TransactionalExecutable;

@Singleton
public class DefaultPullRequestInfoManager extends AbstractEnvironmentManager 
		implements PullRequestInfoManager {

	private static final int INFO_VERSION = 7;
	
	private static final int BATCH_SIZE = 5000;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestInfoManager.class);
	
	private static final String INFO_DIR = "pullRequest";
	
	private static final String DEFAULT_STORE = "default";
	
	private static final String COMMIT_TO_IDS_STORE = "commitToIds";
	
	private static final String COMPARISON_BASES_STORE = "comparisonBases";
	
	private static final ByteIterable LAST_PULL_REQUEST_UPDATE_KEY = new StringByteIterable("lastPullRequestUpdate");

	private static final int PRIORITY = 100;
	
	private final StorageManager storageManager;
	
	private final BatchWorkManager batchWorkManager;
	
	private final ProjectManager projectManager;
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	private final SessionManager sessionManager;
	
	private final TransactionManager transactionManager;
	
	@Inject
	public DefaultPullRequestInfoManager(TransactionManager transactionManager, ProjectManager projectManager, 
			StorageManager storageManager, PullRequestUpdateManager pullRequestUpdateManager, 
			BatchWorkManager batchWorkManager, SessionManager sessionManager) {
		this.projectManager = projectManager;
		this.storageManager = storageManager;
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.batchWorkManager = batchWorkManager;
		this.sessionManager = sessionManager;
		this.transactionManager = transactionManager;
	}
	
	private BatchWorker getBatchWorker(Long projectId) {
		return new BatchWorker("project-" + projectId + "-collectPullRequestInfo") {

			@Override
			public void doWorks(Collection<Prioritized> works) {
				boolean hasMore;
				do {
					// do the work batch by batch to avoid consuming too much memory
					hasMore = sessionManager.call(new Callable<Boolean>() {

						@Override
						public Boolean call() throws Exception {
							return collect(projectManager.load(projectId));
						}
						
					});
				} while (hasMore);
			}
			
		};
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Long projectId = event.getEntity().getId();
			removeEnv(projectId.toString());
		}
	}
	
	private boolean collect(Project project) {
		logger.debug("Collecting pull request info (project: {})...", project);
		
		Environment env = getEnv(project.getId().toString());
		Store defaultStore = getStore(env, DEFAULT_STORE);
		Store commitToIdsStore = getStore(env, COMMIT_TO_IDS_STORE);

		Long lastPullRequestUpdateId = env.computeInTransaction(new TransactionalComputable<Long>() {
			
			@Override
			public Long compute(Transaction txn) {
				return readLong(defaultStore, txn, LAST_PULL_REQUEST_UPDATE_KEY, 0);
			}
			
		});
		
		List<PullRequestUpdate> unprocessedPullRequestUpdates = pullRequestUpdateManager.queryAfter(
				project, lastPullRequestUpdateId, BATCH_SIZE); 
		env.executeInTransaction(new TransactionalExecutable() {

			@Override
			public void execute(Transaction txn) {
				PullRequestUpdate lastUpdate = null;
				for (PullRequestUpdate update: unprocessedPullRequestUpdates) {
					PullRequest request = update.getRequest();
					if (request.isValid()) {
						for (ObjectId commit: update.getCommits()) {
							ByteIterable commitKey = new CommitByteIterable(commit);
							Collection<Long> pullRequestIds = readLongs(commitToIdsStore, txn, commitKey);
							pullRequestIds.add(update.getRequest().getId());
							writeLongs(commitToIdsStore, txn, commitKey, pullRequestIds);
						}
					}
					lastUpdate = update;
				}
				if (lastUpdate != null)
					defaultStore.put(txn, LAST_PULL_REQUEST_UPDATE_KEY, new LongByteIterable(lastUpdate.getId()));
			}
			
		});
		logger.debug("Collected pull request info (project: {})", project);
		
		return unprocessedPullRequestUpdates.size() == BATCH_SIZE;
	}
	
	@Override
	public Collection<Long> getPullRequestIds(Project project, ObjectId commitId) {
		Environment env = getEnv(project.getId().toString());
		Store store = getStore(env, COMMIT_TO_IDS_STORE);
		
		return env.computeInTransaction(new TransactionalComputable<Collection<Long>>() {
			
			@Override
			public Collection<Long> compute(Transaction txn) {
				return readLongs(store, txn, new CommitByteIterable(commitId));
			}
			
		});
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.isNew()) {
			if (event.getEntity() instanceof PullRequestUpdate) {
				Long projectId = ((PullRequestUpdate) event.getEntity()).getRequest().getTargetProject().getId();
				transactionManager.runAfterCommit(new Runnable() {

					@Override
					public void run() {
						batchWorkManager.submit(getBatchWorker(projectId), new Prioritized(PRIORITY));
					}
					
				});
			} else if (event.getEntity() instanceof CodeComment) {
				Long projectId = ((CodeComment)event.getEntity()).getProject().getId();
				transactionManager.runAfterCommit(new Runnable() {

					@Override
					public void run() {
						batchWorkManager.submit(getBatchWorker(projectId), new Prioritized(PRIORITY));
					}
					
				});
			} 
		}
	}

	@Sessional
	@Listen
	public void on(SystemStarted event) {
		for (Project project: projectManager.query()) {
			checkVersion(project.getId().toString());
			batchWorkManager.submit(getBatchWorker(project.getId()), new Prioritized(PRIORITY));
		}
	}
	
	@Override
	protected File getEnvDir(String envKey) {
		File infoDir = new File(storageManager.getProjectInfoDir(Long.valueOf(envKey)), INFO_DIR);
		if (!infoDir.exists()) 
			FileUtils.createDir(infoDir);
		return infoDir;
	}

	@Override
	protected int getEnvVersion() {
		return INFO_VERSION;
	}

	@Override
	public ObjectId getComparisonBase(PullRequest request, ObjectId commitId1, ObjectId commitId2) {
		Environment env = getEnv(request.getTargetProject().getId().toString());
		Store store = getStore(env, COMPARISON_BASES_STORE);
		
		return env.computeInTransaction(new TransactionalComputable<ObjectId>() {
			
			@Override
			public ObjectId compute(Transaction txn) {
				byte[] valueBytes = readBytes(store, txn, getComparisonBaseKey(request, commitId1, commitId2));
				if (valueBytes != null)
					return ObjectId.fromRaw(valueBytes);
				else
					return null;
			}
			
		});
	}
	
	private ByteIterable getComparisonBaseKey(PullRequest request, ObjectId commitId1, ObjectId commitId2) {
		byte[] keyBytes = new byte[40 + Long.BYTES];
		ByteBuffer.wrap(keyBytes, 0, Long.BYTES).putLong(request.getId());
		commitId1.copyRawTo(keyBytes, Long.BYTES);
		commitId2.copyRawTo(keyBytes, Long.BYTES + 20);
		return new ArrayByteIterable(keyBytes);
	}

	@Override
	public void cacheComparisonBase(PullRequest request, ObjectId commitId1, ObjectId commitId2, ObjectId comparisonBase) {
		Environment env = getEnv(request.getTargetProject().getId().toString());
		Store store = getStore(env, COMPARISON_BASES_STORE);
		
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				store.put(txn, getComparisonBaseKey(request, commitId1, commitId2), new CommitByteIterable(comparisonBase));
			}
			
		});
	}

}
