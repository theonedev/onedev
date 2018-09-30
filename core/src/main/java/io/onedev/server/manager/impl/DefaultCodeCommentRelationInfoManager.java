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
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.manager.BatchWorkManager;
import io.onedev.server.manager.CodeCommentManager;
import io.onedev.server.manager.CodeCommentRelationInfoManager;
import io.onedev.server.manager.CodeCommentRelationManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.manager.PullRequestUpdateManager;
import io.onedev.server.manager.StorageManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeComment.ComparingInfo;
import io.onedev.server.model.CodeCommentRelation;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.persistence.UnitOfWork;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
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

		Long lastPullRequestUpdateId = env.computeInTransaction(new TransactionalComputable<Long>() {
			
			@Override
			public Long compute(Transaction txn) {
				return readLong(defaultStore, txn, LAST_PULL_REQUEST_UPDATE_KEY, 0);
			}
			
		});
		
		List<PullRequestUpdate> unprocessedPullRequestUpdates = pullRequestUpdateManager.queryAfter(
				project, lastPullRequestUpdateId, BATCH_SIZE); 
		for (PullRequestUpdate update: unprocessedPullRequestUpdates) {
			env.executeInTransaction(new TransactionalExecutable() {

				@Override
				public void execute(Transaction txn) {
					PullRequest request = update.getRequest();
					if (request.isValid()) {
						for (ObjectId commit: update.getCommits()) {
							ByteIterable commitKey = new CommitByteIterable(commit);
							
							Collection<Long> pullRequestIds = readLongs(pullRequestStore, txn, commitKey);
							pullRequestIds.add(update.getRequest().getId());

							writeLongs(pullRequestStore, txn, commitKey, pullRequestIds);
							
							Map<Long, ComparingInfo> comments = getCodeCommentComparingInfos(codeCommentStore, txn, commitKey);
							Set<Long> commentIdsToRemove = new HashSet<>();
							for (Map.Entry<Long, ComparingInfo> entry: comments.entrySet()) {
								if (request.getRequestComparingInfo(entry.getValue()) != null) {
									Long commentId = entry.getKey();
									CodeComment comment = codeCommentManager.get(commentId);
									if (comment != null) {
										if (codeCommentRelationManager.find(request, comment) == null) {
											CodeCommentRelation relation = new CodeCommentRelation();
											relation.setComment(comment);
											relation.setRequest(request);
											codeCommentRelationManager.save(relation);
										}
									} else {
										commentIdsToRemove.add(commentId);
									}
								}
							}
							if (!commentIdsToRemove.isEmpty()) {
								comments.keySet().removeAll(commentIdsToRemove);
								codeCommentStore.put(txn, commitKey, 
										new ArrayByteIterable(SerializationUtils.serialize((Serializable) comments)));
							}
						}
					}
					defaultStore.put(txn, LAST_PULL_REQUEST_UPDATE_KEY, new LongByteIterable(update.getId()));
				}
				
			});
		}
		
		Long lastCodeCommentId = env.computeInTransaction(new TransactionalComputable<Long>() {
			
			@Override
			public Long compute(final Transaction txn) {
				return readLong(defaultStore, txn, LAST_CODE_COMMENT_KEY, 0);
			}
			
		});
		
		List<CodeComment> unprocessedCodeComments = codeCommentManager.queryAfter(project, 
				lastCodeCommentId, BATCH_SIZE);
		for (CodeComment comment: unprocessedCodeComments) {
			if (comment.isValid()) {
				env.executeInTransaction(new TransactionalExecutable() {

					private void associateCommentWithCommit(Transaction txn, String commit) {
						ObjectId commitId = ObjectId.fromString(commit);
						ByteIterable commitKey = new CommitByteIterable(commitId);
						
						Map<Long, ComparingInfo> comments = getCodeCommentComparingInfos(codeCommentStore, txn, commitKey);
						comments.put(comment.getId(), comment.getComparingInfo());
						codeCommentStore.put(txn, commitKey, 
								new ArrayByteIterable(SerializationUtils.serialize((Serializable) comments)));

						Collection<Long> pullRequestIds = readLongs(pullRequestStore, txn, commitKey);
						
						Set<Long> pullRequestIdsToRemove = new HashSet<>();
						for (Long pullRequestId: pullRequestIds) {
							PullRequest request = pullRequestManager.get(pullRequestId);
							if (request != null && request.isValid()) {
								if (request.getRequestComparingInfo(comment.getComparingInfo()) != null 
										&& codeCommentRelationManager.find(request, comment) == null) {
									CodeCommentRelation relation = new CodeCommentRelation();
									relation.setComment(comment);
									relation.setRequest(request);
									codeCommentRelationManager.save(relation);
								}
							} else {
								pullRequestIdsToRemove.add(pullRequestId);
							}
						}
						if (!pullRequestIdsToRemove.isEmpty()) {
							pullRequestIds.removeAll(pullRequestIdsToRemove);
							writeLongs(pullRequestStore, txn, commitKey, pullRequestIds);
						}
					}
					
					@Override
					public void execute(Transaction txn) {
						associateCommentWithCommit(txn, comment.getMarkPos().getCommit());
						String compareCommit = comment.getCompareContext().getCompareCommit();
						if (!comment.getMarkPos().getCommit().equals(compareCommit)
								&& project.getRepository().hasObject(ObjectId.fromString(compareCommit)))
							associateCommentWithCommit(txn, comment.getCompareContext().getCompareCommit());
						defaultStore.put(txn, LAST_CODE_COMMENT_KEY, new LongByteIterable(comment.getId()));
					}
					
				});
			}
		}
		
		return unprocessedPullRequestUpdates.size() == BATCH_SIZE || unprocessedCodeComments.size() == BATCH_SIZE;
	}
	
	@Override
	public Collection<Long> getPullRequestIds(Project project, ObjectId commitId) {
		Environment env = getEnv(project.getId().toString());
		Store store = getStore(env, PULL_REQUEST_STORE);
		
		return env.computeInTransaction(new TransactionalComputable<Collection<Long>>() {
			
			@Override
			public Collection<Long> compute(Transaction txn) {
				return readLongs(store, txn, new CommitByteIterable(commitId));
			}
			
		});
		
	}
	
	@SuppressWarnings("unchecked")
	private Map<Long, ComparingInfo> getCodeCommentComparingInfos(Store store, Transaction txn, ByteIterable commitKey) {
		byte[] valueBytes = readBytes(store, txn, commitKey);
		if (valueBytes != null) {
			return (Map<Long, ComparingInfo>) SerializationUtils.deserialize(valueBytes);
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
