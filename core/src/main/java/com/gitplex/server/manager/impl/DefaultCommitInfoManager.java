package com.gitplex.server.manager.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.event.RefUpdated;
import com.gitplex.server.event.lifecycle.SystemStarted;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.git.NameAndEmail;
import com.gitplex.server.git.command.LogCommand;
import com.gitplex.server.git.command.LogCommit;
import com.gitplex.server.git.command.RevListCommand;
import com.gitplex.server.manager.BatchWorkManager;
import com.gitplex.server.manager.CommitInfoManager;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.manager.StorageManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.persistence.UnitOfWork;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityRemoved;
import com.gitplex.server.util.BatchWorker;
import com.gitplex.server.util.FileUtils;
import com.gitplex.server.util.PathUtils;
import com.gitplex.server.util.StringUtils;
import com.gitplex.server.util.concurrent.Prioritized;
import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.util.facade.UserFacade;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;

import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.backup.BackupStrategy;
import jetbrains.exodus.backup.BackupStrategy.FileDescriptor;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalComputable;
import jetbrains.exodus.env.TransactionalExecutable;

@Singleton
public class DefaultCommitInfoManager extends AbstractEnvironmentManager implements CommitInfoManager {

	private static final int INFO_VERSION = 1;
	
	private static final long LOG_FILE_SIZE = 256*1024;
	
	private static final int COLLECT_BATCH_SIZE = 50000;
	
	private static final int MAX_COLLECTING_FILES = 50000;
	
	private static final String INFO_DIR = "commit";
	
	private static final String DEFAULT_STORE = "default";
	
	private static final String COMMITS_STORE = "commits";
	
	private static final String CONTRIBUTIONS_STORE = "contributions";
	
	private static final ByteIterable LAST_COMMIT_KEY = new StringByteIterable("lastCommit");
	
	private static final ByteIterable AUTHORS_KEY = new StringByteIterable("authors");
	
	private static final ByteIterable COMMITTERS_KEY = new StringByteIterable("committers");
	
	private static final ByteIterable FILES_KEY = new StringByteIterable("files");
	
	private static final ByteIterable COMMIT_COUNT_KEY = new StringByteIterable("commitCount");
	
	private static final int PRIORITY = 100;
	
	private final StorageManager storageManager;
	
	private final BatchWorkManager batchWorkManager;
	
	private final ExecutorService executorService;
	
	private final ProjectManager projectManager;
	
	private final UnitOfWork unitOfWork;
	
	private final Dao dao;
	
	private final Map<Long, List<String>> filesCache = new ConcurrentHashMap<>();
	
	private final Map<Long, Integer> commitCountCache = new ConcurrentHashMap<>();
	
	private final Map<Long, List<NameAndEmail>> authorsCache = new ConcurrentHashMap<>();
	
	private final Map<Long, List<NameAndEmail>> committersCache = new ConcurrentHashMap<>();
	
	@Inject
	public DefaultCommitInfoManager(ProjectManager projectManager, StorageManager storageManager, 
			BatchWorkManager batchWorkManager, UnitOfWork unitOfWork, Dao dao, ExecutorService executorService) {
		this.projectManager = projectManager;
		this.storageManager = storageManager;
		this.batchWorkManager = batchWorkManager;
		this.unitOfWork = unitOfWork;
		this.dao = dao;
		this.executorService = executorService;
	}
	
	private void doCollect(Project project, ObjectId commitId, boolean divide) {
		Environment env = getEnv(project.getId().toString());
		Store defaultStore = getStore(env, DEFAULT_STORE);

		if (divide) {
			List<ObjectId> intermediateCommitIds = new ArrayList<>();
			List<String> revisions = new ArrayList<>();
			revisions.add(commitId.name());

			ObjectId lastCommitId = env.computeInTransaction(new TransactionalComputable<ObjectId>() {

				@Override
				public ObjectId compute(Transaction txn) {
					byte[] bytes = getBytes(defaultStore.get(txn, LAST_COMMIT_KEY));
					return bytes!=null? ObjectId.fromRaw(bytes): null;
				}
				
			});
			if (lastCommitId != null) {
				try (RevWalk revWalk = new RevWalk(project.getRepository())) {
					if (GitUtils.parseCommit(revWalk, lastCommitId) != null)
						revisions.add("^" + lastCommitId.name());
				} 
			}
			
			int count = 0;
			for (String commitHash: new RevListCommand(project.getGitDir()).revisions(revisions).call()) {
				count++;
				if (count > COLLECT_BATCH_SIZE) {
					intermediateCommitIds.add(ObjectId.fromString(commitHash));
					count = 0;
				}
			}
			
			for (int i=intermediateCommitIds.size()-1; i>=0; i--) {
				doCollect(project, intermediateCommitIds.get(i), false);
			}
			
			doCollect(project, commitId, false);
		} else {
			Store commitsStore = getStore(env, COMMITS_STORE);
			Store contributionsStore = getStore(env, CONTRIBUTIONS_STORE); 
			
			env.executeInTransaction(new TransactionalExecutable() {
				
				@SuppressWarnings("unchecked")
				@Override
				public void execute(Transaction txn) {
					try {
						byte[] bytes = getBytes(defaultStore.get(txn, LAST_COMMIT_KEY));
						
						ObjectId lastCommitId;
						if (bytes != null)
							lastCommitId = ObjectId.fromRaw(bytes);
						else
							lastCommitId = null;
						
						bytes = getBytes(defaultStore.get(txn, COMMIT_COUNT_KEY));
						int prevCommitCount;
						if (bytes != null)
							prevCommitCount = (int) SerializationUtils.deserialize(bytes);
						else
							prevCommitCount = 0;
						int newCommitCount = prevCommitCount;
						
						Set<NameAndEmail> prevAuthors;
						bytes = getBytes(defaultStore.get(txn, AUTHORS_KEY));
						if (bytes != null)
							prevAuthors = (Set<NameAndEmail>) SerializationUtils.deserialize(bytes);
						else
							prevAuthors = new HashSet<>();
						Set<NameAndEmail> authors = new HashSet<>(prevAuthors);			
						
						Set<NameAndEmail> prevCommitters;
						bytes = getBytes(defaultStore.get(txn, COMMITTERS_KEY));
						if (bytes != null)
							prevCommitters = (Set<NameAndEmail>) SerializationUtils.deserialize(bytes);
						else
							prevCommitters = new HashSet<>();
						Set<NameAndEmail> committers = new HashSet<>(prevCommitters);

						Map<String, Long> files;
						bytes = getBytes(defaultStore.get(txn, FILES_KEY));
						if (bytes != null)
							files = (Map<String, Long>) SerializationUtils.deserialize(bytes);
						else
							files = new HashMap<>();

						/*
						 * Use a synchronous queue to achieve below purpose:
						 * 1. Add commit to Xodus transactional store in the same thread opening the transaction 
						 * as this is required by Xodus
						 * 2. Do not pile up commits to use minimal memory 
						 */
						SynchronousQueue<Optional<LogCommit>> queue = new SynchronousQueue<>(); 
						AtomicReference<Exception> logException = new AtomicReference<>(null);
						executorService.execute(new Runnable() {

							@Override
							public void run() {
								try {
									List<String> revisions = new ArrayList<>();
									revisions.add(commitId.name());
									if (lastCommitId != null)
										revisions.add("^" + lastCommitId.name());
									
									new LogCommand(project.getGitDir()) {

										@Override
										protected void consume(LogCommit commit) {
											try {
												queue.put(Optional.of(commit));
											} catch (InterruptedException e) {
											}
										}
										
									}.revisions(revisions).call();
									
								} catch (Exception e) {
									logException.set(e);
								} finally {
									try {
										queue.put(Optional.empty());
									} catch (InterruptedException e) {
									}
								}
							}
							
						});
						
						Optional<LogCommit> commitOptional = queue.take();
						while (commitOptional.isPresent()) {
							LogCommit commit = commitOptional.get();
							
							byte[] keyBytes = new byte[20];
							ObjectId.fromString(commit.getHash()).copyRawTo(keyBytes, 0);
							ByteIterable key = new ArrayByteIterable(keyBytes);
							byte[] valueBytes = getBytes(commitsStore.get(txn, key));
							
							if (valueBytes == null || valueBytes.length % 20 == 0) {
								/*
								 * Length of stored bytes of a commit is either 20*nChild
								 * (20 is length of ObjectId), or 1+20*nChild, as we need 
								 * an extra leading byte to differentiate commits being 
								 * processed and commits with child information attached 
								 * but not processed. 
								 */
								byte[] newValueBytes;
								if (valueBytes == null) {
									newValueBytes = new byte[1];
								} else {
									newValueBytes = new byte[valueBytes.length+1];
									System.arraycopy(valueBytes, 0, newValueBytes, 1, valueBytes.length);
								}
								commitsStore.put(txn, key, new ArrayByteIterable(newValueBytes));
								
								newCommitCount++;
								
								for (String parentHash: commit.getParentHashes()) {
									keyBytes = new byte[20];
									ObjectId.fromString(parentHash).copyRawTo(keyBytes, 0);
									key = new ArrayByteIterable(keyBytes);
									valueBytes = getBytes(commitsStore.get(txn, key));
									if (valueBytes != null) {
										newValueBytes = new byte[valueBytes.length+20];
										System.arraycopy(valueBytes, 0, newValueBytes, 0, valueBytes.length);
									} else {
										newValueBytes = new byte[20];
									}
									ObjectId.fromString(commit.getHash()).copyRawTo(newValueBytes, newValueBytes.length-20);
									commitsStore.put(txn, key, new ArrayByteIterable(newValueBytes));
								}
								
								if (commit.getCommitter() != null) {
									committers.add(new NameAndEmail(commit.getCommitter()));
									for (String file: commit.getChangedFiles())
										files.put(file, commit.getCommitter().getWhen().getTime());
								}

								if (commit.getAuthor() != null) {
									authors.add(new NameAndEmail(commit.getAuthor()));
									for (String path: commit.getChangedFiles()) {
										updateContribution(txn, contributionsStore, commit.getAuthor().getEmailAddress(), path);
										while (path.contains("/")) {
											path = StringUtils.substringBeforeLast(path, "/");
											updateContribution(txn, contributionsStore, 
													commit.getAuthor().getEmailAddress(), path);
										}
										updateContribution(txn, contributionsStore, 
												commit.getAuthor().getEmailAddress(), "");
									}
								}
							}		
							commitOptional = queue.take();
						}
						if (logException.get() != null)
							throw logException.get();
						
						if (newCommitCount != prevCommitCount) {
							bytes = SerializationUtils.serialize(newCommitCount);
							defaultStore.put(txn, COMMIT_COUNT_KEY, new ArrayByteIterable(bytes));
							commitCountCache.put(project.getId(), newCommitCount);
						}
						
						if (!authors.equals(prevAuthors)) {
							bytes = SerializationUtils.serialize((Serializable) authors);
							defaultStore.put(txn, AUTHORS_KEY, new ArrayByteIterable(bytes));
							authorsCache.remove(project.getId());
						} 
						
						if (!committers.equals(prevCommitters)) {
							bytes = SerializationUtils.serialize((Serializable) committers);
							defaultStore.put(txn, COMMITTERS_KEY, new ArrayByteIterable(bytes));
							committersCache.remove(project.getId());
						}
						
						if (files.size() > MAX_COLLECTING_FILES) {
							List<String> fileList = new ArrayList<>(files.keySet());
							fileList.sort((file1, file2)->files.get(file1).compareTo(files.get(file2)));
							for (int i=0; i<fileList.size() - MAX_COLLECTING_FILES; i++)
								files.remove(fileList.get(i));
						}
						bytes = SerializationUtils.serialize((Serializable) files);
						defaultStore.put(txn, FILES_KEY, new ArrayByteIterable(bytes));
						filesCache.remove(project.getId());
						
						bytes = new byte[20];
						commitId.copyRawTo(bytes, 0);
						defaultStore.put(txn, LAST_COMMIT_KEY, new ArrayByteIterable(bytes));
					} catch (Exception e) {
						Throwables.propagate(e);
					}
				}
				
			});			
		}
	}
	
	private void updateContribution(Transaction txn, Store contributionsStore, String email, String path) {
		ArrayByteIterable contributionKey = 
				new ArrayByteIterable(getContributionKey(email, path));
		byte[] contributionBytes = 
				getBytes(contributionsStore.get(txn, contributionKey));
		int contributions;
		if (contributionBytes != null)
			contributions = ByteBuffer.wrap(contributionBytes).getInt() + 1;
		else
			contributions = 1;
		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
		byteBuffer.putInt(contributions);
		contributionsStore.put(txn, contributionKey, 
				new ArrayByteIterable(byteBuffer.array()));
	}
	
	@Override
	public List<NameAndEmail> getAuthors(Project project) {
		List<NameAndEmail> authors = authorsCache.get(project.getId());
		if (authors == null) {
			Environment env = getEnv(project.getId().toString());
			Store store = getStore(env, DEFAULT_STORE);

			authors = env.computeInReadonlyTransaction(new TransactionalComputable<List<NameAndEmail>>() {

				@SuppressWarnings("unchecked")
				@Override
				public List<NameAndEmail> compute(Transaction txn) {
					byte[] bytes = getBytes(store.get(txn, AUTHORS_KEY));
					if (bytes != null) { 
						List<NameAndEmail> authors = 
								new ArrayList<>((Set<NameAndEmail>) SerializationUtils.deserialize(bytes));
						Collections.sort(authors);
						return authors;
					} else { 
						return new ArrayList<>();
					}
				}
			});
			authorsCache.put(project.getId(), authors);
		}
		return authors;	
	}

	@Override
	public List<NameAndEmail> getCommitters(Project project) {
		List<NameAndEmail> committers = committersCache.get(project.getId());
		if (committers == null) {
			Environment env = getEnv(project.getId().toString());
			Store store = getStore(env, DEFAULT_STORE);

			committers = env.computeInReadonlyTransaction(new TransactionalComputable<List<NameAndEmail>>() {

				@SuppressWarnings("unchecked")
				@Override
				public List<NameAndEmail> compute(Transaction txn) {
					byte[] bytes = getBytes(store.get(txn, COMMITTERS_KEY));
					if (bytes != null) { 
						List<NameAndEmail> committers = 
								new ArrayList<>((Set<NameAndEmail>) SerializationUtils.deserialize(bytes));
						Collections.sort(committers);
						return committers;
					} else { 
						return new ArrayList<>();
					}
				}
			});
			committersCache.put(project.getId(), committers);
		}
		return committers;	
	}
	
	@Override
	public List<String> getFiles(Project project) {
		List<String> files = filesCache.get(project.getId());
		if (files == null) {
			Environment env = getEnv(project.getId().toString());
			final Store store = getStore(env, DEFAULT_STORE);

			files = env.computeInReadonlyTransaction(new TransactionalComputable<List<String>>() {

				@SuppressWarnings("unchecked")
				@Override
				public List<String> compute(Transaction txn) {
					byte[] bytes = getBytes(store.get(txn, FILES_KEY));
					if (bytes != null) {
						List<String> files = new ArrayList<>(
								((Map<String, Long>)SerializationUtils.deserialize(bytes)).keySet());
						Map<String, List<String>> segmentsMap = new HashMap<>();
						Splitter splitter = Splitter.on("/");
						for (String file: files) {
							segmentsMap.put(file, splitter.splitToList(file));
						}
						files.sort(new Comparator<String>() {

							@Override
							public int compare(String o1, String o2) {
								return PathUtils.compare(segmentsMap.get(o1), segmentsMap.get(o2));
							}
							
						});
						return files;
					} else {
						return new ArrayList<>();
					}
				}
			});
			filesCache.put(project.getId(), files);
		}
		return files;
	}
	
	private byte[] getContributionKey(String email, String file) {
		return (email + " " + file).getBytes(Charsets.UTF_8);
	}
	
	@Override
	public int getContributions(ProjectFacade project, UserFacade user, String path) {
		if (user.getEmail() != null) {
			Environment env = getEnv(project.getId().toString());
			Store store = getStore(env, CONTRIBUTIONS_STORE);
			return env.computeInReadonlyTransaction(new TransactionalComputable<Integer>() {

				@Override
				public Integer compute(Transaction tx) {
					byte[] contributionKey = getContributionKey(user.getEmail(), path);
					byte[] bytes = getBytes(store.get(tx, new ArrayByteIterable(contributionKey)));
					if (bytes != null)
						return ByteBuffer.wrap(bytes).getInt();
					else
						return 0;
				}
			});
		} else {
			return 0;
		}
	}
	
	@Override
	public Set<ObjectId> getDescendants(Project project, final ObjectId ancestor) {
		Environment env = getEnv(project.getId().toString());
		final Store store = getStore(env, COMMITS_STORE);

		return env.computeInReadonlyTransaction(new TransactionalComputable<Set<ObjectId>>() {

			@Override
			public Set<ObjectId> compute(Transaction txn) {
				Set<ObjectId> descendants = new HashSet<>();
				
				// use stack instead of recursion to avoid StackOverflowException
				Stack<ObjectId> stack = new Stack<>();
				descendants.add(ancestor);
				stack.add(ancestor);
				while (!stack.isEmpty()) {
					ObjectId current = stack.pop();
					byte[] keyBytes = new byte[20];
					current.copyRawTo(keyBytes, 0);
					byte[] valueBytes = getBytes(store.get(txn, new ArrayByteIterable(keyBytes)));
					if (valueBytes != null) {
						if (valueBytes.length % 20 == 0) {
							for (int i=0; i<valueBytes.length/20; i++) {
								ObjectId child = ObjectId.fromRaw(valueBytes, i*20);
								if (!descendants.contains(child)) {
									descendants.add(child);
									stack.push(child);
								}
							}
						} else { 
							/*
							 * skip the leading byte, which tells whether or not the commit 
							 * has been processed
							 */
							for (int i=0; i<(valueBytes.length-1)/20; i++) {
								ObjectId child = ObjectId.fromRaw(valueBytes, i*20+1);
								if (!descendants.contains(child)) {
									descendants.add(child);
									stack.push(child);
								}
							}
						}
					}
				}
				
				return descendants;
			}
			
		});
	}

	@Override
	public Set<ObjectId> getChildren(Project project, final ObjectId parent) {
		Environment env = getEnv(project.getId().toString());
		final Store store = getStore(env, COMMITS_STORE);

		return env.computeInReadonlyTransaction(new TransactionalComputable<Set<ObjectId>>() {

			@Override
			public Set<ObjectId> compute(Transaction txn) {
				Set<ObjectId> children = new HashSet<>();
				
				byte[] keyBytes = new byte[20];
				parent.copyRawTo(keyBytes, 0);
				byte[] valueBytes = getBytes(store.get(txn, new ArrayByteIterable(keyBytes)));
				if (valueBytes != null) {
					if (valueBytes.length % 20 == 0) {
						for (int i=0; i<valueBytes.length/20; i++) {
							children.add(ObjectId.fromRaw(valueBytes, i*20));
						}
					} else { 
						/*
						 * skip the leading byte, which tells whether or not the commit 
						 * has been processed
						 */
						for (int i=0; i<(valueBytes.length-1)/20; i++) {
							children.add(ObjectId.fromRaw(valueBytes, i*20+1));
						}
					}
				}
				return children;
			}
			
		});
	}

	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			dao.doAfterCommit(new Runnable() {

				@Override
				public void run() {
					Long projectId = event.getEntity().getId();
					removeEnv(projectId.toString());
					filesCache.remove(projectId);
					commitCountCache.remove(projectId);
					authorsCache.remove(projectId);
				}
				
			});
		}
	}
	
	private BatchWorker getBatchWorker(Project project) {
		Long projectId = project.getId();
		return new BatchWorker("project-" + projectId + "-collectCommitInfo") {

			@Override
			public void doWork(Collection<Prioritized> works) {
				unitOfWork.call(new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						Project project = projectManager.load(projectId);
						List<RevCommit> commits = new ArrayList<>();
						for (Object work: works)
							commits.add(((CollectingWork)work).getCommit());
						commits.sort(Comparator.comparing(RevCommit::getCommitTime));
						for (RevCommit commit: commits) {
							doCollect(project, commit.copy(), true);
						}
						return null;
					}
					
				});
			}
			
		};		
	}
	
	private void collect(Project project) {
		List<RevCommit> commits = new ArrayList<>();
		try (RevWalk revWalk = new RevWalk(project.getRepository())) {
			Collection<Ref> refs = new ArrayList<>();
			refs.addAll(project.getRepository().getRefDatabase().getRefs(Constants.R_HEADS).values());
			refs.addAll(project.getRepository().getRefDatabase().getRefs(Constants.R_TAGS).values());

			for (Ref ref: refs) {
				RevObject revObj = revWalk.peel(revWalk.parseAny(ref.getObjectId()));
				if (revObj instanceof RevCommit) {
					RevCommit commit = (RevCommit) revObj;
					Environment env = getEnv(project.getId().toString());
					Store commitsStore = getStore(env, COMMITS_STORE);
					boolean collected = env.computeInReadonlyTransaction(new TransactionalComputable<Boolean>() {
						
						@Override
						public Boolean compute(Transaction txn) {
							byte[] keyBytes = new byte[20];
							commit.copyRawTo(keyBytes, 0);
							ByteIterable key = new ArrayByteIterable(keyBytes);
							byte[] valueBytes = getBytes(commitsStore.get(txn, key));
							return valueBytes != null && valueBytes.length % 20 != 0;
						}
						
					});
					if (!collected) 
						commits.add(commit);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		commits.sort(Comparator.comparing(RevCommit::getCommitTime));
		for (RevCommit commit: commits) {
			CollectingWork work = new CollectingWork(PRIORITY, commit);
			batchWorkManager.submit(getBatchWorker(project), work);
		}
	}

	@Sessional
	@Listen
	public void on(SystemStarted event) {
		for (Project project: projectManager.findAll()) {
			checkVersion(project.getId().toString());
			collect(project);
		}
	}
	
	@Listen
	public void on(RefUpdated event) {
		if (!event.getNewObjectId().equals(ObjectId.zeroId())) {
			try (RevWalk revWalk = new RevWalk(event.getProject().getRepository())) {
				RevCommit commit = GitUtils.parseCommit(revWalk, event.getNewObjectId());
				if (commit != null) {
					CollectingWork work = new CollectingWork(PRIORITY, commit);
					batchWorkManager.submit(getBatchWorker(event.getProject()), work);
				}
			}
		}
	}

	@Override
	public int getCommitCount(Project project) {
		Integer commitCount = commitCountCache.get(project.getId());
		if (commitCount == null) {
			Environment env = getEnv(project.getId().toString());
			Store store = getStore(env, DEFAULT_STORE);

			commitCount = env.computeInReadonlyTransaction(new TransactionalComputable<Integer>() {

				@Override
				public Integer compute(Transaction txn) {
					byte[] bytes = getBytes(store.get(txn, COMMIT_COUNT_KEY));
					if (bytes != null) {
						return (Integer) SerializationUtils.deserialize(bytes);
					} else {
						return 0;
					}
				}
			});
			commitCountCache.put(project.getId(), commitCount);
		}
		return commitCount;
	}

	static class CollectingWork extends Prioritized {
		
		private final RevCommit commit;
		
		public CollectingWork(int priority, RevCommit commit) {
			super(priority);
			this.commit = commit;
		}

		public RevCommit getCommit() {
			return commit;
		}
		
	}

	@Override
	public void cloneInfo(Project source, Project target) {
		BackupStrategy backupStrategy = getEnv(source.getId().toString()).getBackupStrategy();
		try {
			File targetDir = getEnvDir(target.getId().toString());
			backupStrategy.beforeBackup();
			try {
				for (FileDescriptor descriptor: backupStrategy.listFiles()) {
					FileUtils.copyFileToDirectory(descriptor.getFile(), targetDir);
				}
			} finally {
				backupStrategy.afterBackup();
			}
			writeVersion(target.getId().toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	@Override
	public ObjectId getLastCommit(Project project) {
		Environment env = getEnv(project.getId().toString());
		Store defaultStore = getStore(env, DEFAULT_STORE);

		return env.computeInTransaction(new TransactionalComputable<ObjectId>() {
			
			@Override
			public ObjectId compute(Transaction txn) {
				byte[] bytes = getBytes(defaultStore.get(txn, LAST_COMMIT_KEY));
				return bytes != null? ObjectId.fromRaw(bytes): null;
			}
			
		});
	}

	@Override
	protected File getEnvDir(String envKey) {
		File infoDir = new File(storageManager.getProjectInfoDir(Long.valueOf(envKey)), INFO_DIR);
		if (!infoDir.exists()) 
			FileUtils.createDir(infoDir);
		return infoDir;
	}

	@Override
	protected long getLogFileSize() {
		return LOG_FILE_SIZE;
	}

	@Override
	protected int getEnvVersion() {
		return INFO_VERSION;
	}

}
