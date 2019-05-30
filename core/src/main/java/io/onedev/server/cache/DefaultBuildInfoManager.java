package io.onedev.server.cache;

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

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.concurrent.Prioritized;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.work.BatchWorkManager;
import io.onedev.server.util.work.BatchWorker;
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
	
	private final SessionManager sessionManager;
	
	private final TransactionManager transactionManager;
	
	@Inject
	public DefaultBuildInfoManager(ProjectManager projectManager, StorageManager storageManager, 
			BatchWorkManager batchWorkManager, SessionManager sessionManager, BuildManager buildManager, 
			TransactionManager transactionManager) {
		this.projectManager = projectManager;
		this.storageManager = storageManager;
		this.batchWorkManager = batchWorkManager;
		this.sessionManager = sessionManager;
		this.buildManager = buildManager;
		this.transactionManager = transactionManager;
	}
	
	private BatchWorker getBatchWorker(Long projectId) {
		return new BatchWorker("project-" + projectId + "-collectBuildInfo") {

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
					ByteIterable jobKey = new StringByteIterable(build.getProject().getId() + ":" + build.getJobName());
					Collection<ObjectId> lastCommits = readCommits(lastCommitsStore, txn, jobKey);
					for (Iterator<ObjectId> it = lastCommits.iterator(); it.hasNext();) {
						if (project.getRevCommit(it.next(), false) == null)
							it.remove();
					}					
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
					writeCommits(lastCommitsStore, txn, jobKey, lastCommits);
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
			Long projectId = ((Build) event.getEntity()).getProject().getId();
			transactionManager.runAfterCommit(new Runnable() {

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

	@Override
	public void delete(Project project, Long buildId) {
		Environment env = getEnv(project.getId().toString());
		Store store = getStore(env, PREV_COMMITS_STORE);
		
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				store.delete(txn, new LongByteIterable(buildId));
			}
			
		});
	}
	
}
