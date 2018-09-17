package io.onedev.server.manager.impl;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.event.lifecycle.SystemStarted;
import io.onedev.server.git.GitUtils;
import io.onedev.server.manager.BatchWorkManager;
import io.onedev.server.manager.BuildInfoManager;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.StorageManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.UnitOfWork;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityPersisted;
import io.onedev.server.persistence.dao.EntityRemoved;
import io.onedev.server.util.BatchWorker;
import io.onedev.utils.FileUtils;
import io.onedev.utils.concurrent.Prioritized;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalComputable;
import jetbrains.exodus.env.TransactionalExecutable;

@Singleton
public class DefaultBuildInfoManager extends AbstractEnvironmentManager implements BuildInfoManager {

	private static final int INFO_VERSION = 6;
	
	private static final int BATCH_SIZE = 5000;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultBuildInfoManager.class);
	
	private static final String INFO_DIR = "build";
	
	private static final String DEFAULT_STORE = "default";
	
	private static final String LAST_COMMITS_STORE = "lastCommits";
	
	private static final String PREV_COMMITS_STORE = "prevCommits";
	
	private static final ByteIterable LAST_BUILD_KEY = new StringByteIterable("lastBuild");
	
	private static final int PRIORITY = 100;
	
	private final StorageManager storageManager;
	
	private final BatchWorkManager batchWorkManager;
	
	private final ProjectManager projectManager;
	
	private final BuildManager buildManager;
	
	private final UnitOfWork unitOfWork;
	
	private final Dao dao;
	
	@Inject
	public DefaultBuildInfoManager(Dao dao, ProjectManager projectManager, StorageManager storageManager, 
			BatchWorkManager batchWorkManager, UnitOfWork unitOfWork, BuildManager buildManager) {
		this.projectManager = projectManager;
		this.storageManager = storageManager;
		this.batchWorkManager = batchWorkManager;
		this.unitOfWork = unitOfWork;
		this.buildManager = buildManager;
		this.dao = dao;
	}
	
	private BatchWorker getBatchWorker(Long projectId) {
		return new BatchWorker("project-" + projectId + "-collectBuildInfo") {

			@Override
			public void doWorks(Collection<Prioritized> works) {
				boolean hasMore;
				do {
					// do the work batch by batch to avoid consuming too much memory
					hasMore = unitOfWork.call(new Callable<Boolean>() {

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
		logger.debug("Collecting build info in project '{}'...", project);
		
		Environment env = getEnv(project.getId().toString());
		Store defaultStore = getStore(env, DEFAULT_STORE);
		Store lastCommitsStore = getStore(env, LAST_COMMITS_STORE);
		Store prevCommitsStore = getStore(env, PREV_COMMITS_STORE);

		Long lastBuildId = env.computeInTransaction(new TransactionalComputable<Long>() {
			
			@Override
			public Long compute(Transaction txn) {
				return readLong(defaultStore, txn, LAST_BUILD_KEY, 0);
			}
			
		});
		
		List<Build> unprocessedBuilds = buildManager.queryAfter(project, lastBuildId, BATCH_SIZE); 
		for (Build build: unprocessedBuilds) {
			env.executeInTransaction(new TransactionalExecutable() {

				@Override
				public void execute(Transaction txn) {
					ByteIterable configurationKey = new LongByteIterable(build.getConfiguration().getId());
					Collection<ObjectId> lastCommits = readCommits(lastCommitsStore, txn, configurationKey);
					if (lastCommits.isEmpty() && build.getConfiguration().getBaseCommit() != null)
						lastCommits.add(ObjectId.fromString(build.getConfiguration().getBaseCommit()));
					writeCommits(prevCommitsStore, txn, new LongByteIterable(build.getId()), lastCommits);
					
					ObjectId buildCommit = ObjectId.fromString(build.getCommitHash());
					boolean addCommit = true;
					for (Iterator<ObjectId> it = lastCommits.iterator(); it.hasNext();) {
						ObjectId lastCommit = it.next();
						if (GitUtils.isMergedInto(project.getRepository(), null, lastCommit, buildCommit)) { 
							it.remove();
						} else if (GitUtils.isMergedInto(project.getRepository(), null, buildCommit, lastCommit)) {
							addCommit = false;
							break;
						}
					}
					if (addCommit)
						lastCommits.add(buildCommit);
					writeCommits(lastCommitsStore, txn, configurationKey, lastCommits);
					defaultStore.put(txn, LAST_BUILD_KEY, new LongByteIterable(build.getId()));
				}
				
			});
		}
		
		return unprocessedBuilds.size() == BATCH_SIZE;
	}
	
	@Override
	public Collection<ObjectId> getPrevCommits(Project project, Long buildId) {
		Environment env = getEnv(project.getId().toString());
		Store defaultStore = getStore(env, DEFAULT_STORE);
		Store prevCommitsStore = getStore(env, PREV_COMMITS_STORE);
		
		return env.computeInTransaction(new TransactionalComputable<Collection<ObjectId>>() {
			
			@Override
			public Collection<ObjectId> compute(Transaction txn) {
				if (readLong(defaultStore, txn, LAST_BUILD_KEY, 0) < buildId) { 
					batchWorkManager.submit(getBatchWorker(project.getId()), new Prioritized(PRIORITY));
					return null;
				} else {
					return readCommits(prevCommitsStore, txn, new LongByteIterable(buildId));
				}
			}
			
		});
		
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.isNew() && event.getEntity() instanceof Build) {
			Long projectId = ((Build) event.getEntity()).getConfiguration().getProject().getId();
			dao.doAfterCommit(new Runnable() {

				@Override
				public void run() {
					batchWorkManager.submit(getBatchWorker(projectId), new Prioritized(PRIORITY));
				}
				
			});
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
	
}
