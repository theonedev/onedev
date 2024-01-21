package io.onedev.server.xodus;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestUpdateManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.ActiveServerChanged;
import io.onedev.server.event.project.ProjectDeleted;
import io.onedev.server.event.project.pullrequest.PullRequestOpened;
import io.onedev.server.event.project.pullrequest.PullRequestUpdated;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.util.concurrent.BatchWorkManager;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

import static java.lang.Long.valueOf;

@Singleton
public class DefaultPullRequestInfoManager extends AbstractMultiEnvironmentManager 
		implements PullRequestInfoManager, Serializable {

	private static final int INFO_VERSION = 7;
	
	private static final int BATCH_SIZE = 5000;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestInfoManager.class);
	
	private static final String DEFAULT_STORE = "default";
	
	private static final String COMMIT_TO_IDS_STORE = "commitToIds";
	
	private static final String COMPARISON_BASES_STORE = "comparisonBases";
	
	private static final ByteIterable LAST_PULL_REQUEST_UPDATE_KEY = new StringByteIterable("lastPullRequestUpdate");

	private static final int PRIORITY = 100;
	
	private final BatchWorkManager batchWorkManager;
	
	private final ProjectManager projectManager;
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	@Inject
	public DefaultPullRequestInfoManager(ProjectManager projectManager, PullRequestUpdateManager pullRequestUpdateManager, 
										 BatchWorkManager batchWorkManager) {
		this.projectManager = projectManager;
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.batchWorkManager = batchWorkManager;
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(PullRequestInfoManager.class);
	}
	
	private BatchWorker getBatchWorker(Long projectId) {
		return new BatchWorker("project-" + projectId + "-collectPullRequestInfo") {

			@Override
			public void doWorks(List<Prioritized> works) {
				// do the work batch by batch to avoid consuming too much memory
				while (collect(projectId));
			}
			
		};
	}
	
	@Listen
	public void on(ProjectDeleted event) {
		removeEnv(event.getProjectId().toString());
	}
	
	@Sessional
	protected boolean collect(Long projectId) {
		logger.debug("Collecting pull request info (project id: {})...", projectId);
		
		Environment env = getEnv(projectId.toString());
		Store defaultStore = getStore(env, DEFAULT_STORE);
		Store commitToIdsStore = getStore(env, COMMIT_TO_IDS_STORE);

		Long lastPullRequestUpdateId = env.computeInTransaction(txn -> readLong(defaultStore, txn, LAST_PULL_REQUEST_UPDATE_KEY, 0));
		
		List<PullRequestUpdate> unprocessedPullRequestUpdates = pullRequestUpdateManager.queryAfter(
				projectId, lastPullRequestUpdateId, BATCH_SIZE); 
		env.executeInTransaction(txn -> {
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
		});
		logger.debug("Collected pull request info (project id: {})", projectId);
		
		return unprocessedPullRequestUpdates.size() == BATCH_SIZE;
	}
	
	@Override
	public Collection<Long> getPullRequestIds(Project project, ObjectId commitId) {
		Long projectId = project.getId();
		
		return projectManager.runOnActiveServer(projectId, new ClusterTask<Collection<Long>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Collection<Long> call() throws Exception {
				Environment env = getEnv(projectId.toString());
				Store store = getStore(env, COMMIT_TO_IDS_STORE);
				
				return env.computeInTransaction(txn -> readLongs(store, txn, new CommitByteIterable(commitId)));
			}
			
		});
	}

	@Sessional
	@Listen
	public void on(PullRequestOpened event) {
		batchWorkManager.submit(getBatchWorker(event.getProject().getId()), new Prioritized(PRIORITY));
	}
	
	@Sessional
	@Listen
	public void on(PullRequestUpdated event) {
		batchWorkManager.submit(getBatchWorker(event.getProject().getId()), new Prioritized(PRIORITY));
	}

	@Sessional
	@Listen
	public void on(SystemStarted event) {
		for (var projectId: projectManager.getActiveIds()) {
			checkVersion(getEnvDir(projectId.toString()));
			batchWorkManager.submit(getBatchWorker(projectId), new Prioritized(PRIORITY));
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
		File infoDir = new File(projectManager.getInfoDir(valueOf(envKey)), "pullRequest");
		FileUtils.createDir(infoDir);
		return infoDir;
	}

	@Override
	protected int getEnvVersion() {
		return INFO_VERSION;
	}

	@Sessional
	@Override
	public ObjectId getComparisonBase(PullRequest request, ObjectId commitId1, ObjectId commitId2) {
		Long targetProjectId = request.getTargetProject().getId();
		Long requestId = request.getId();
		
		return projectManager.runOnActiveServer(targetProjectId, new ClusterTask<ObjectId>() {

			private static final long serialVersionUID = 1L;

			@Override
			public ObjectId call() throws Exception {
				Environment env = getEnv(targetProjectId.toString());
				Store store = getStore(env, COMPARISON_BASES_STORE);
				
				return env.computeInTransaction(txn -> {
					byte[] valueBytes = readBytes(store, txn, getComparisonBaseKey(requestId, commitId1, commitId2));
					if (valueBytes != null)
						return ObjectId.fromRaw(valueBytes);
					else
						return null;
				});
			}
			
		});

	}
	
	private ByteIterable getComparisonBaseKey(Long requestId, ObjectId commitId1, ObjectId commitId2) {
		byte[] keyBytes = new byte[40 + Long.BYTES];
		ByteBuffer.wrap(keyBytes, 0, Long.BYTES).putLong(requestId);
		commitId1.copyRawTo(keyBytes, Long.BYTES);
		commitId2.copyRawTo(keyBytes, Long.BYTES + 20);
		return new ArrayByteIterable(keyBytes);
	}

	@Sessional
	@Override
	public void cacheComparisonBase(PullRequest request, ObjectId commitId1, ObjectId commitId2, ObjectId comparisonBase) {
		Long targetProjectId = request.getTargetProject().getId();
		Long requestId = request.getId();
		
		projectManager.runOnActiveServer(targetProjectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				Environment env = getEnv(targetProjectId.toString());
				Store store = getStore(env, COMPARISON_BASES_STORE);
				
				env.executeInTransaction(txn -> store.put(txn, getComparisonBaseKey(requestId, commitId1, commitId2), new CommitByteIterable(comparisonBase)));
				return null;
			}
			
		});
	}

}
