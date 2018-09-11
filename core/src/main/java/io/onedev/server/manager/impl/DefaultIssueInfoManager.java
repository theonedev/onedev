package io.onedev.server.manager.impl;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.event.lifecycle.SystemStarted;
import io.onedev.server.manager.BatchWorkManager;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.manager.IssueInfoManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.StorageManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
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
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalComputable;
import jetbrains.exodus.env.TransactionalExecutable;

@Singleton
public class DefaultIssueInfoManager extends AbstractEnvironmentManager implements IssueInfoManager {

	private static final int INFO_VERSION = 6;
	
	private static final int BATCH_SIZE = 5000;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultIssueInfoManager.class);
	
	private static final String INFO_DIR = "issue";
	
	private static final String DEFAULT_STORE = "default";
	
	private static final String ISSUE_STORE = "issue";
	
	private static final ByteIterable LAST_BUILD_KEY = new StringByteIterable("lastBuild");
	
	private static final int PRIORITY = 100;
	
	private final StorageManager storageManager;
	
	private final BatchWorkManager batchWorkManager;
	
	private final ProjectManager projectManager;
	
	private final BuildManager buildManager;
	
	private final UnitOfWork unitOfWork;
	
	private final Dao dao;
	
	@Inject
	public DefaultIssueInfoManager(Dao dao, ProjectManager projectManager, StorageManager storageManager, 
			BatchWorkManager batchWorkManager, UnitOfWork unitOfWork, BuildManager buildManager) {
		this.projectManager = projectManager;
		this.storageManager = storageManager;
		this.batchWorkManager = batchWorkManager;
		this.unitOfWork = unitOfWork;
		this.buildManager = buildManager;
		this.dao = dao;
	}
	
	private BatchWorker getBatchWorker(Long projectId) {
		return new BatchWorker("project-" + projectId + "-collectIssueInfo") {

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
		logger.debug("Collecting issue info in project '{}'...", project);
		
		Environment env = getEnv(project.getId().toString());
		Store defaultStore = getStore(env, DEFAULT_STORE);
		Store issueStore = getStore(env, ISSUE_STORE);

		String lastBuildUUID = env.computeInTransaction(new TransactionalComputable<String>() {
			
			@Override
			public String compute(Transaction txn) {
				byte[] value = readBytes(defaultStore, txn, LAST_BUILD_KEY);
				return value!=null?new String(value):null;									
			}
			
		});
		
		List<Build> unprocessedBuilds = buildManager.findAllAfter(project, lastBuildUUID, BATCH_SIZE); 
		for (Build build: unprocessedBuilds) {
			env.executeInTransaction(new TransactionalExecutable() {

				@Override
				public void execute(Transaction txn) {
					for (Issue issue: build.getFixedIssues()) {
						ByteIterable issueKey = new StringByteIterable(issue.getUUID());
						Set<String> fixedInBuildUUIDs = getFixedInBuildUUIDs(issueStore, txn, issueKey);
						fixedInBuildUUIDs.add(build.getUUID());
						issueStore.put(txn, issueKey, 
								new ArrayByteIterable(SerializationUtils.serialize((Serializable) fixedInBuildUUIDs)));
					}
					defaultStore.put(txn, LAST_BUILD_KEY, new StringByteIterable(build.getUUID()));
				}
				
			});
		}
		
		return unprocessedBuilds.size() == BATCH_SIZE;
	}
	
	@SuppressWarnings("unchecked")
	private Set<String> getFixedInBuildUUIDs(Store store, Transaction txn, ByteIterable issueKey) {
		byte[] valueBytes = readBytes(store, txn, issueKey);
		if (valueBytes != null) {
			return (Set<String>) SerializationUtils.deserialize(valueBytes);
		} else {
			return new HashSet<>();
		}
	}
	
	@Override
	public Set<String> getFixedInBuildUUIDs(Project project, String issueUUID) {
		Environment env = getEnv(project.getId().toString());
		Store store = getStore(env, ISSUE_STORE);
		
		return env.computeInTransaction(new TransactionalComputable<Set<String>>() {
			
			@Override
			public Set<String> compute(Transaction txn) {
				return getFixedInBuildUUIDs(store, txn, new StringByteIterable(issueUUID));
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
		for (Project project: projectManager.findAll()) {
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
