package io.onedev.server.manager.impl;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.event.lifecycle.SystemStarted;
import io.onedev.server.manager.BatchWorkManager;
import io.onedev.server.manager.CodeCommentManager;
import io.onedev.server.manager.CodeCommentRelationInfoManager;
import io.onedev.server.manager.CodeCommentRelationManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.manager.PullRequestUpdateManager;
import io.onedev.server.manager.StorageManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentRelation;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.CodeComment.ComparingInfo;
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
public class DefaultCodeCommentRelationInfoManager extends AbstractEnvironmentManager 
		implements CodeCommentRelationInfoManager {

	private static final int INFO_VERSION = 6;
	
	private static final int BATCH_SIZE = 5000;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultCodeCommentRelationInfoManager.class);
	
	private static final String INFO_DIR = "codeCommentRelation";
	
	private static final String DEFAULT_STORE = "default";
	
	private static final String CODE_COMMENT_STORE = "codeComment";
	
	private static final String PULL_REQUEST_STORE = "pullRequest";
	
	private static final ByteIterable LAST_PULL_REQUEST_UPDATE_KEY = new StringByteIterable("lastPullRequestUpdate");

	private static final ByteIterable LAST_CODE_COMMENT_KEY = new StringByteIterable("lastCodeComment");
	
	private static final int PRIORITY = 100;
	
	private final StorageManager storageManager;
	
	private final BatchWorkManager batchWorkManager;
	
	private final ProjectManager projectManager;
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	private final PullRequestManager pullRequestManager;
	
	private final CodeCommentRelationManager codeCommentRelationManager;
	
	private final UnitOfWork unitOfWork;
	
	private final CodeCommentManager codeCommentManager;
	
	private final Dao dao;
	
	@Inject
	public DefaultCodeCommentRelationInfoManager(Dao dao, ProjectManager projectManager, StorageManager storageManager, 
			PullRequestUpdateManager pullRequestUpdateManager, CodeCommentManager codeCommentManager, 
			BatchWorkManager batchWorkManager, UnitOfWork unitOfWork, PullRequestManager pullRequestManager, 
			CodeCommentRelationManager codeCommentRelationManager) {
		this.projectManager = projectManager;
		this.storageManager = storageManager;
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.codeCommentManager = codeCommentManager;
		this.batchWorkManager = batchWorkManager;
		this.unitOfWork = unitOfWork;
		this.pullRequestManager = pullRequestManager;
		this.codeCommentRelationManager = codeCommentRelationManager;
		this.dao = dao;
	}
	
	private BatchWorker getBatchWorker(Long projectId) {
		return new BatchWorker("project-" + projectId + "-collectCodeCommentRelationInfo") {

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
		logger.debug("Collecting code comment relation info in project '{}'...", project);
		
		Environment env = getEnv(project.getId().toString());
		Store defaultStore = getStore(env, DEFAULT_STORE);
		Store codeCommentStore = getStore(env, CODE_COMMENT_STORE);
		Store pullRequestStore = getStore(env, PULL_REQUEST_STORE);

		String lastPullRequestUpdateUUID = env.computeInTransaction(new TransactionalComputable<String>() {
			
			@Override
			public String compute(Transaction txn) {
				byte[] value = readBytes(defaultStore, txn, LAST_PULL_REQUEST_UPDATE_KEY);
				return value!=null?new String(value):null;									
			}
			
		});
		
		List<PullRequestUpdate> unprocessedPullRequestUpdates = pullRequestUpdateManager.queryAfter(
				project, lastPullRequestUpdateUUID, BATCH_SIZE); 
		for (PullRequestUpdate update: unprocessedPullRequestUpdates) {
			env.executeInTransaction(new TransactionalExecutable() {

				@Override
				public void execute(Transaction txn) {
					PullRequest request = update.getRequest();
					if (request.isValid()) {
						for (ObjectId commit: update.getCommits()) {
							byte[] keyBytes = new byte[20];
							commit.copyRawTo(keyBytes, 0);
							ByteIterable commitKey = new ArrayByteIterable(keyBytes);
							
							Set<String> pullRequestUUIDs = getPullRequestUUIDs(pullRequestStore, txn, commitKey);
							pullRequestUUIDs.add(update.getRequest().getUUID());
							
							pullRequestStore.put(txn, commitKey, 
									new ArrayByteIterable(SerializationUtils.serialize((Serializable) pullRequestUUIDs)));
							
							Map<String, ComparingInfo> comments = getCodeCommentComparingInfos(codeCommentStore, txn, commitKey);
							Set<String> uuidsToRemove = new HashSet<>();
							for (Map.Entry<String, ComparingInfo> entry: comments.entrySet()) {
								if (request.getRequestComparingInfo(entry.getValue()) != null) {
									String uuid = entry.getKey();
									CodeComment comment = codeCommentManager.find(uuid);
									if (comment != null) {
										if (codeCommentRelationManager.find(request, comment) == null) {
											CodeCommentRelation relation = new CodeCommentRelation();
											relation.setComment(comment);
											relation.setRequest(request);
											codeCommentRelationManager.save(relation);
										}
									} else {
										uuidsToRemove.add(uuid);
									}
								}
							}
							if (!uuidsToRemove.isEmpty()) {
								comments.keySet().removeAll(uuidsToRemove);
								codeCommentStore.put(txn, commitKey, 
										new ArrayByteIterable(SerializationUtils.serialize((Serializable) comments)));
							}
						}
					}
					defaultStore.put(txn, LAST_PULL_REQUEST_UPDATE_KEY, new StringByteIterable(update.getUUID()));
				}
				
			});
		}
		
		String lastCodeCommentUUID = env.computeInTransaction(new TransactionalComputable<String>() {
			
			@Override
			public String compute(final Transaction txn) {
				byte[] value = readBytes(defaultStore, txn, LAST_CODE_COMMENT_KEY);
				return value!=null?new String(value):null;									
			}
			
		});
		
		List<CodeComment> unprocessedCodeComments = codeCommentManager.queryAfter(project, 
				lastCodeCommentUUID, BATCH_SIZE);
		for (CodeComment comment: unprocessedCodeComments) {
			if (comment.isValid()) {
				env.executeInTransaction(new TransactionalExecutable() {

					private void associateCommentWithCommit(Transaction txn, String commit) {
						ObjectId commitId = ObjectId.fromString(commit);
						byte[] keyBytes = new byte[20];
						commitId.copyRawTo(keyBytes, 0);
						ByteIterable commitKey = new ArrayByteIterable(keyBytes);
						
						Map<String, ComparingInfo> comments = getCodeCommentComparingInfos(codeCommentStore, txn, commitKey);
						comments.put(comment.getUUID(), comment.getComparingInfo());
						codeCommentStore.put(txn, commitKey, 
								new ArrayByteIterable(SerializationUtils.serialize((Serializable) comments)));

						Set<String> pullRequestUUIDs = getPullRequestUUIDs(pullRequestStore, txn, commitKey);
						
						Set<String> uuidsToRemove = new HashSet<>();
						for (String uuid: pullRequestUUIDs) {
							PullRequest request = pullRequestManager.find(uuid);
							if (request != null && request.isValid()) {
								if (request.getRequestComparingInfo(comment.getComparingInfo()) != null 
										&& codeCommentRelationManager.find(request, comment) == null) {
									CodeCommentRelation relation = new CodeCommentRelation();
									relation.setComment(comment);
									relation.setRequest(request);
									codeCommentRelationManager.save(relation);
								}
							} else {
								uuidsToRemove.add(uuid);
							}
						}
						if (!uuidsToRemove.isEmpty()) {
							pullRequestUUIDs.removeAll(uuidsToRemove);
							pullRequestStore.put(txn, commitKey, 
									new ArrayByteIterable(SerializationUtils.serialize((Serializable) pullRequestUUIDs)));
						}
					}
					
					@Override
					public void execute(Transaction txn) {
						associateCommentWithCommit(txn, comment.getMarkPos().getCommit());
						String compareCommit = comment.getCompareContext().getCompareCommit();
						if (!comment.getMarkPos().getCommit().equals(compareCommit)
								&& project.getRepository().hasObject(ObjectId.fromString(compareCommit)))
							associateCommentWithCommit(txn, comment.getCompareContext().getCompareCommit());
						defaultStore.put(txn, LAST_CODE_COMMENT_KEY, new StringByteIterable(comment.getUUID()));
					}
					
				});
			}
		}
		
		return unprocessedPullRequestUpdates.size() == BATCH_SIZE || unprocessedCodeComments.size() == BATCH_SIZE;
	}
	
	@SuppressWarnings("unchecked")
	private Set<String> getPullRequestUUIDs(Store store, Transaction txn, ByteIterable commitKey) {
		byte[] valueBytes = readBytes(store, txn, commitKey);
		if (valueBytes != null) {
			return (Set<String>) SerializationUtils.deserialize(valueBytes);
		} else {
			return new HashSet<>();
		}
	}
	
	@Override
	public Set<String> getPullRequestUUIDs(Project project, ObjectId commitId) {
		byte[] commitKey = new byte[20];
		commitId.copyRawTo(commitKey, 0);

		Environment env = getEnv(project.getId().toString());
		Store store = getStore(env, PULL_REQUEST_STORE);
		
		return env.computeInTransaction(new TransactionalComputable<Set<String>>() {
			
			@Override
			public Set<String> compute(Transaction txn) {
				return getPullRequestUUIDs(store, txn, new ArrayByteIterable(commitKey));
			}
			
		});
		
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, ComparingInfo> getCodeCommentComparingInfos(Store store, Transaction txn, ByteIterable commitKey) {
		byte[] valueBytes = readBytes(store, txn, commitKey);
		if (valueBytes != null) {
			return (Map<String, ComparingInfo>) SerializationUtils.deserialize(valueBytes);
		} else {
			return new HashMap<>();
		}
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.isNew()) {
			if (event.getEntity() instanceof PullRequestUpdate) {
				Long projectId = ((PullRequestUpdate) event.getEntity()).getRequest().getTargetProject().getId();
				dao.doAfterCommit(new Runnable() {

					@Override
					public void run() {
						batchWorkManager.submit(getBatchWorker(projectId), new Prioritized(PRIORITY));
					}
					
				});
			} else if (event.getEntity() instanceof CodeComment) {
				Long projectId = ((CodeComment)event.getEntity()).getProject().getId();
				dao.doAfterCommit(new Runnable() {

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
	
}
