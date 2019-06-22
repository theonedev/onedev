package io.onedev.server.cache;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.PathUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.concurrent.Prioritized;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.issue.IssueCommitted;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.git.Contribution;
import io.onedev.server.git.Contributor;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.NameAndEmail;
import io.onedev.server.git.command.FileChange;
import io.onedev.server.git.command.GitCommit;
import io.onedev.server.git.command.LogCommand;
import io.onedev.server.git.command.RevListCommand;
import io.onedev.server.git.command.RevListCommand.Order;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.Day;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.work.BatchWorkManager;
import io.onedev.server.util.work.BatchWorker;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.backup.BackupStrategy;
import jetbrains.exodus.backup.BackupStrategy.FileDescriptor;
import jetbrains.exodus.backup.VirtualFileDescriptor;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalComputable;
import jetbrains.exodus.env.TransactionalExecutable;

@Singleton
public class DefaultCommitInfoManager extends AbstractEnvironmentManager implements CommitInfoManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultCommitInfoManager.class);
	
	private static final int INFO_VERSION = 7;
	
	private static final long LOG_FILE_SIZE = 256*1024;
	
	private static final int COLLECT_BATCH_SIZE = 10000;
	
	private static final int MAX_COLLECTING_FILES = 50000;
	
	private static final int MAX_HISTORY_PATHS = 100;
	
	private static final String INFO_DIR = "commit";
	
	private static final String DEFAULT_STORE = "default";
	
	private static final String COMMITS_STORE = "commits";
	
	private static final String FIX_COMMITS_STORE = "fixCommits";
	
	private static final String EDITS_STORE = "edits";

	private static final String HISTORY_PATHS_STORE = "historyPaths";
	
	private static final String PATH_TO_INDEX_STORE = "pathToIndex";
	
	private static final String INDEX_TO_PATH_STORE = "indexToPath";
	
	private static final String EMAIL_TO_INDEX_STORE = "emailToIndex";
	
	private static final String INDEX_TO_USER_STORE = "indexToUser";
	
	private static final String DAILY_CONTRIBUTIONS_STORE = "dailyContributions";
	
	private static final ByteIterable NEXT_PATH_INDEX_KEY = new StringByteIterable("nextPathIndex");
	
	private static final ByteIterable NEXT_USER_INDEX_KEY = new StringByteIterable("nextUserIndex");
	
	private static final ByteIterable LAST_COMMIT_KEY = new StringByteIterable("lastCommit");
	
	private static final ByteIterable LAST_COMMIT_OF_DEFAULT_BRANCH_KEY = new StringByteIterable("lastCommitOfDefaultBranch");
	
	private static final ByteIterable LINE_STATS_KEY = new StringByteIterable("lineStats");
	
	private static final ByteIterable USERS_KEY = new StringByteIterable("users");
	
	private static final ByteIterable FILES_KEY = new StringByteIterable("files");
	
	private static final ByteIterable OVERALL_CONTRIBUTIONS_KEY = new StringByteIterable("overallContributions");
	
	private static final ByteIterable COMMIT_COUNT_KEY = new StringByteIterable("commitCount");
	
	private static final int PRIORITY = 100;
	
	private static final Map<String, String> PROGRAMMING_LANGUAGES = new HashMap<>();
	
	static {
		PROGRAMMING_LANGUAGES.put("java", "Java");
		
		PROGRAMMING_LANGUAGES.put("h", "C");
		PROGRAMMING_LANGUAGES.put("c", "C");
		
		PROGRAMMING_LANGUAGES.put("h++", "C++");
		PROGRAMMING_LANGUAGES.put("c++", "C++");
		PROGRAMMING_LANGUAGES.put("hpp", "C++");
		PROGRAMMING_LANGUAGES.put("cpp", "C++");
		PROGRAMMING_LANGUAGES.put("hxx", "C++");
		PROGRAMMING_LANGUAGES.put("cxx", "C++");
		PROGRAMMING_LANGUAGES.put("hh", "C++");
		PROGRAMMING_LANGUAGES.put("cc", "C++");
		
		PROGRAMMING_LANGUAGES.put("cob", "Cobol");
		PROGRAMMING_LANGUAGES.put("cpy", "Cobol");
		
		PROGRAMMING_LANGUAGES.put("cs", "CSharp");
		
		PROGRAMMING_LANGUAGES.put("clj", "Clojure");
		PROGRAMMING_LANGUAGES.put("cljc", "Clojure");
		PROGRAMMING_LANGUAGES.put("cljx", "Clojure");
		PROGRAMMING_LANGUAGES.put("cljs", "ClojureScript");
		PROGRAMMING_LANGUAGES.put("gss", "Closure Stylesheets");
		PROGRAMMING_LANGUAGES.put("coffee", "CoffeeScript");
		
		PROGRAMMING_LANGUAGES.put("cl", "Common Lisp");
		PROGRAMMING_LANGUAGES.put("lisp", "Common Lisp");
		PROGRAMMING_LANGUAGES.put("el", "Common Lisp");
		
		PROGRAMMING_LANGUAGES.put("css", "CSS");
		PROGRAMMING_LANGUAGES.put("d", "D");
		PROGRAMMING_LANGUAGES.put("dart", "Dart");
		PROGRAMMING_LANGUAGES.put("dtd", "DTD");
		
		PROGRAMMING_LANGUAGES.put("erl", "Erlang");
		PROGRAMMING_LANGUAGES.put("f", "Fortran");
		PROGRAMMING_LANGUAGES.put("for", "Fortran");
		PROGRAMMING_LANGUAGES.put("f77", "Fortran");
		PROGRAMMING_LANGUAGES.put("f90", "Fortran");
		
		PROGRAMMING_LANGUAGES.put("go", "Go");
		PROGRAMMING_LANGUAGES.put("groovy", "Groovy");
		PROGRAMMING_LANGUAGES.put("gradle", "Groovy");
		
		PROGRAMMING_LANGUAGES.put("hs", "Haskell");
		PROGRAMMING_LANGUAGES.put("aspx", "ASP.NET");
		PROGRAMMING_LANGUAGES.put("html", "HTML");
		PROGRAMMING_LANGUAGES.put("htm", "HTML");
		PROGRAMMING_LANGUAGES.put("jsp", "Java Server Pages");
		PROGRAMMING_LANGUAGES.put("js", "JavaScript");
		
		PROGRAMMING_LANGUAGES.put("json", "JSON");
		PROGRAMMING_LANGUAGES.put("jsx", "JSX");
		
		PROGRAMMING_LANGUAGES.put("kt", "Kotlin");
		PROGRAMMING_LANGUAGES.put("less", "LESS");
		PROGRAMMING_LANGUAGES.put("lua", "Lua");
		PROGRAMMING_LANGUAGES.put("md", "Markdown");
		PROGRAMMING_LANGUAGES.put("mkd", "Markdown");
		PROGRAMMING_LANGUAGES.put("m", "Objective-C");
		PROGRAMMING_LANGUAGES.put("mm", "Objective-C");
		
		PROGRAMMING_LANGUAGES.put("p", "Pascal");
		PROGRAMMING_LANGUAGES.put("pas", "Pascal");
		PROGRAMMING_LANGUAGES.put("pl", "Perl");
		PROGRAMMING_LANGUAGES.put("pm", "Perl");
		
		PROGRAMMING_LANGUAGES.put("php", "PHP");
		PROGRAMMING_LANGUAGES.put("php3", "PHP");
		PROGRAMMING_LANGUAGES.put("php4", "PHP");
		PROGRAMMING_LANGUAGES.put("php5", "PHP");
		PROGRAMMING_LANGUAGES.put("php7", "PHP");
		PROGRAMMING_LANGUAGES.put("phtml", "PHP");
		
		PROGRAMMING_LANGUAGES.put("sql", "SQL");
		PROGRAMMING_LANGUAGES.put("ps1", "PowerShell");
		PROGRAMMING_LANGUAGES.put("psd1", "PowerShell");
		PROGRAMMING_LANGUAGES.put("psm1", "PowerShell");
		PROGRAMMING_LANGUAGES.put("psm1", "PowerShell");
		
		PROGRAMMING_LANGUAGES.put("properties", "Properties");
		PROGRAMMING_LANGUAGES.put("ini", "INI");
		PROGRAMMING_LANGUAGES.put("in", "INI");
		PROGRAMMING_LANGUAGES.put("proto", "ProtoBuf");
		
		PROGRAMMING_LANGUAGES.put("BUILD", "Python");
		PROGRAMMING_LANGUAGES.put("py", "Python");
		PROGRAMMING_LANGUAGES.put("pyw", "Python");
		PROGRAMMING_LANGUAGES.put("bzl", "Python");
		
		PROGRAMMING_LANGUAGES.put("pp", "Puppet");
		PROGRAMMING_LANGUAGES.put("r", "R");
		PROGRAMMING_LANGUAGES.put("R", "R");
		PROGRAMMING_LANGUAGES.put("rb", "Ruby");
		PROGRAMMING_LANGUAGES.put("rs", "Rust");
		
		PROGRAMMING_LANGUAGES.put("sas", "SAS");
		PROGRAMMING_LANGUAGES.put("sass", "Sass");
		PROGRAMMING_LANGUAGES.put("scala", "Scala");
		PROGRAMMING_LANGUAGES.put("scm", "Scheme");
		PROGRAMMING_LANGUAGES.put("ss", "Scheme");
		PROGRAMMING_LANGUAGES.put("scss", "Scss");
		
		PROGRAMMING_LANGUAGES.put("sh", "Shell");
		PROGRAMMING_LANGUAGES.put("ksh", "Shell");
		PROGRAMMING_LANGUAGES.put("bash", "Shell");
		
		PROGRAMMING_LANGUAGES.put("st", "SmallTalk");
		PROGRAMMING_LANGUAGES.put("soy", "Soy");
		PROGRAMMING_LANGUAGES.put("styl", "Stylus");
		PROGRAMMING_LANGUAGES.put("swift", "Swift");
		
		PROGRAMMING_LANGUAGES.put("tex", "LaTeX");
		PROGRAMMING_LANGUAGES.put("tcl", "TCL");
		PROGRAMMING_LANGUAGES.put("ts", "TypeScript");
		PROGRAMMING_LANGUAGES.put("tsx", "TypeScript-JSX");
		PROGRAMMING_LANGUAGES.put("vb", "VB.NET");
		PROGRAMMING_LANGUAGES.put("vbs", "VBScript");
		
		PROGRAMMING_LANGUAGES.put("vue", "Vue.js Component");
		PROGRAMMING_LANGUAGES.put("xml", "XML");
		PROGRAMMING_LANGUAGES.put("xsl", "XML");
		PROGRAMMING_LANGUAGES.put("xsd", "XML");
		PROGRAMMING_LANGUAGES.put("svg", "XML");
		PROGRAMMING_LANGUAGES.put("yaml", "Yaml");
		PROGRAMMING_LANGUAGES.put("yml", "Yaml");
	}
	
	private final StorageManager storageManager;
	
	private final BatchWorkManager batchWorkManager;
	
	private final ProjectManager projectManager;
	
	private final SessionManager sessionManager;
	
	private final ExecutorService executorService;
	
	private final ListenerRegistry listenerRegistry;
	
	private final IssueManager issueManager;
	
	private final Map<Long, List<String>> filesCache = new ConcurrentHashMap<>();
	
	private final Map<Long, Integer> commitCountCache = new ConcurrentHashMap<>();
	
	private final Map<Long, List<NameAndEmail>> usersCache = new ConcurrentHashMap<>();
	
	@Inject
	public DefaultCommitInfoManager(ProjectManager projectManager, StorageManager storageManager, 
			BatchWorkManager batchWorkManager, SessionManager sessionManager, ExecutorService executorService, 
			ListenerRegistry listenerRegistry, IssueManager issueManager) {
		this.projectManager = projectManager;
		this.storageManager = storageManager;
		this.batchWorkManager = batchWorkManager;
		this.sessionManager = sessionManager;
		this.executorService = executorService;
		this.listenerRegistry = listenerRegistry;
		this.issueManager = issueManager;
	}
	
	private boolean isCommitCollected(byte[] commitBytes) {
		/*
		 * Collected commits stores an additional byte to differentiate from those not collected but with parent 
		 * information stored 
		 */
		return commitBytes != null && commitBytes.length % 20 != 0;
	}
	
	private void doCollect(Project project, ObjectId commitId, String refName) {
		Environment env = getEnv(project.getId().toString());
		Store defaultStore = getStore(env, DEFAULT_STORE);
		Store commitsStore = getStore(env, COMMITS_STORE);
		Store editsStore = getStore(env, EDITS_STORE); 
		Store historyPathsStore = getStore(env, HISTORY_PATHS_STORE);
		Store pathToIndexStore = getStore(env, PATH_TO_INDEX_STORE);
		Store indexToPathStore = getStore(env, INDEX_TO_PATH_STORE);
		Store emailToIndexStore = getStore(env, EMAIL_TO_INDEX_STORE);
		Store indexToUserStore = getStore(env, INDEX_TO_USER_STORE);
		Store dailyContributionsStore = getStore(env, DAILY_CONTRIBUTIONS_STORE);	
		Store fixCommitsStore = getStore(env, FIX_COMMITS_STORE);
		
		Repository repository = project.getRepository();

		Collection<Long> fixedIssueNumbers = new HashSet<>();
		
		env.executeInTransaction(new TransactionalExecutable() {
			
			@Override
			public void execute(Transaction txn) {
				ByteIterable commitKey = new CommitByteIterable(commitId);
				byte[] commitBytes = readBytes(commitsStore, txn, commitKey);
				
				if (!isCommitCollected(commitBytes)) {
					int commitCount = readInt(defaultStore, txn, COMMIT_COUNT_KEY, 0);
					
					try (RevWalk revWalk = new RevWalk(project.getRepository())) {
						RevCommit commit = revWalk.lookupCommit(commitId);
						revWalk.markStart(commit);
						
						byte[] lastCommitBytes = readBytes(defaultStore, txn, LAST_COMMIT_KEY);
						if (lastCommitBytes != null) {
							ObjectId lastCommitId = ObjectId.fromRaw(lastCommitBytes);
							if (repository.hasObject(lastCommitId))
								revWalk.markUninteresting(revWalk.lookupCommit(lastCommitId));
						}
						
						RevCommit nextCommit = revWalk.next();
						while (nextCommit != null) {
							ByteIterable nextCommitKey = new CommitByteIterable(nextCommit);
							byte[] nextCommitBytes = readBytes(commitsStore, txn, nextCommitKey);
							
							if (!isCommitCollected(nextCommitBytes)) {
								byte[] newNextCommitBytes;
								if (nextCommitBytes == null) {
									newNextCommitBytes = new byte[1];
								} else {
									newNextCommitBytes = new byte[1+nextCommitBytes.length];
									System.arraycopy(nextCommitBytes, 0, newNextCommitBytes, 1, nextCommitBytes.length);
								}
								
								commitsStore.put(txn, nextCommitKey, new ArrayByteIterable(newNextCommitBytes));
								
								commitCount++;
								
								for (RevCommit parentCommit: nextCommit.getParents()) {
									ByteIterable parentCommitKey = new CommitByteIterable(parentCommit);
									byte[] parentCommitBytes = readBytes(commitsStore, txn, parentCommitKey);
									byte[] newParentCommitBytes;
									if (parentCommitBytes != null) {
										newParentCommitBytes = new byte[parentCommitBytes.length+20];
										System.arraycopy(parentCommitBytes, 0, newParentCommitBytes, 0, parentCommitBytes.length);
									} else {
										newParentCommitBytes = new byte[20];
									}
									nextCommit.copyRawTo(newParentCommitBytes, newParentCommitBytes.length-20);
									commitsStore.put(txn, parentCommitKey, new ArrayByteIterable(newParentCommitBytes));
								}
								
								for (Long issueNumber: IssueUtils.parseFixedIssues(project, nextCommit.getFullMessage())) {
									ByteIterable issueKey = new LongByteIterable(issueNumber);
									Collection<ObjectId> fixCommits = readCommits(fixCommitsStore, txn, issueKey);
									
									boolean addNextCommit = true;
									for (Iterator<ObjectId> it = fixCommits.iterator(); it.hasNext();) {
										ObjectId fixCommit = it.next();
										if (GitUtils.isMergedInto(project.getRepository(), null, fixCommit, nextCommit)) { 
											it.remove();
										} else if (GitUtils.isMergedInto(project.getRepository(), null, nextCommit, fixCommit)) {
											addNextCommit = false;
											break;
										}
									}
									if (addNextCommit) {
										fixCommits.add(nextCommit);
										fixedIssueNumbers.add(issueNumber);
									}
									writeCommits(fixCommitsStore, txn, issueKey, fixCommits);
								}
							}								
							nextCommit = revWalk.next();
						}
						writeInt(defaultStore, txn, COMMIT_COUNT_KEY, commitCount);
						commitCountCache.remove(project.getId());
						
						defaultStore.put(txn, LAST_COMMIT_KEY, new CommitByteIterable(commitId));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}			
			}
			
		});			
		
		for (Long issueNumber: fixedIssueNumbers) {
			Issue issue = issueManager.find(project, issueNumber);
			if (issue != null)
				listenerRegistry.post(new IssueCommitted(issue));
		}

		if (GitUtils.branch2ref(project.getDefaultBranch()).equals(refName)) {
			ObjectId lastCommitId = 
					env.computeInReadonlyTransaction(new TransactionalComputable<ObjectId>() {

				@Override
				public ObjectId compute(Transaction txn) {
					byte[] lastCommitBytes = readBytes(defaultStore, txn, LAST_COMMIT_OF_DEFAULT_BRANCH_KEY);
					if (lastCommitBytes != null) 
						return ObjectId.fromRaw(lastCommitBytes);
					else 
						return null;
				}
				
			});

			if (!commitId.equals(lastCommitId)) {
				RevListCommand revList = new RevListCommand(project.getGitDir());
				List<String> revisions = new ArrayList<>();
				revisions.add(commitId.name());
				if (lastCommitId != null && repository.hasObject(lastCommitId)) 
					revisions.add("^" + lastCommitId.name());
				revList.revisions(revisions).order(Order.TOPO);
				
				List<ObjectId> historyIds = new ArrayList<>();
				for (String commitHash: revList.call(null)) 
					historyIds.add(ObjectId.fromString(commitHash));
				
				revList = new RevListCommand(project.getGitDir());
				revList.order(null).firstParent(true);
				
				Set<ObjectId> firstParentIds = new HashSet<>();
				for (String commitHash: revList.call(null)) 
					firstParentIds.add(ObjectId.fromString(commitHash));

				/*
				 * Instead of collecting information of master branch all at once, we identify some  
				 * intermediate commits and collect the information using these intermediate commits 
				 * multiple times for two reasons:
				 * 1. Use less memory
				 * 2. Commit Exodus transaction sooner so user can use auto-completion when search 
				 * commits even if collection is not done yet
				 */
				List<ObjectId> intermediateIds = new ArrayList<>();
				int count = 0;
				for (ObjectId historyId: historyIds) {
					count++;
					/*
					 * Only use intermediate commits that are part of first parent chain. This 
					 * makes sure that subsequent intermediate commits are always ancestor of 
					 * current intermediate commit (after reverse done below), to avoid 
					 * collecting some commits multiple times
					 */
					if (count > COLLECT_BATCH_SIZE && firstParentIds.contains(historyId)) {
						intermediateIds.add(historyId);
						count = 0;
					}
				}

				Collections.reverse(intermediateIds);
				intermediateIds.add(commitId);
				
				historyIds = null;
				firstParentIds = null;
				
				for(ObjectId currentCommitId: intermediateIds) {
					env.executeInTransaction(new TransactionalExecutable() {
						
						@SuppressWarnings("unchecked")
						@Override
						public void execute(Transaction txn) {
							NextIndex nextIndex = new NextIndex();
							nextIndex.user = readInt(defaultStore, txn, NEXT_USER_INDEX_KEY, 0);
							nextIndex.path = readInt(defaultStore, txn, NEXT_PATH_INDEX_KEY, 0);
							
							Map<Long, Integer> editsCache = new HashMap<>();
							
							Set<NameAndEmail> users;
							byte[] userBytes = readBytes(defaultStore, txn, USERS_KEY);
							if (userBytes != null)
								users = (Set<NameAndEmail>) SerializationUtils.deserialize(userBytes);
							else
								users = new HashSet<>();

							Map<String, Long> files;
							byte[] fileBytes = readBytes(defaultStore, txn, FILES_KEY);
							if (fileBytes != null)
								files = (Map<String, Long>) SerializationUtils.deserialize(fileBytes);
							else
								files = new HashMap<>();
							
							Map<Integer, Map<String, Integer>> lineStats;
							byte[] bytesOfLineStats = readBytes(defaultStore, txn, LINE_STATS_KEY);
							if (bytesOfLineStats != null) {
								lineStats = (Map<Integer, Map<String, Integer>>) SerializationUtils.deserialize(
										bytesOfLineStats);
							} else {
								lineStats = new HashMap<>();
							}
							
							Map<Integer, Map<Integer, Contribution>> dailyContributionsCache = new HashMap<>();
							Map<Integer, Contribution> overallContributions = 
									deserializeContributions(readBytes(defaultStore, txn, OVERALL_CONTRIBUTIONS_KEY));

							/*
							 * Use a synchronous queue to achieve below purpose:
							 * 1. Add commit to Xodus transactional store in the same thread opening the transaction 
							 * as this is required by Xodus
							 * 2. Do not pile up commits to use minimal memory 
							 */
							SynchronousQueue<Optional<GitCommit>> queue = new SynchronousQueue<>(); 
							AtomicReference<Exception> logException = new AtomicReference<>(null);
							
							List<String> revisions = new ArrayList<>();
							revisions.add(currentCommitId.name());

							ObjectId lastCommitId;
							byte[] lastCommitBytes = readBytes(defaultStore, txn, LAST_COMMIT_OF_DEFAULT_BRANCH_KEY);
							if (lastCommitBytes != null) 
								lastCommitId = ObjectId.fromRaw(lastCommitBytes);
							else 
								lastCommitId = null;
							if (lastCommitId != null && repository.hasObject(lastCommitId))
								revisions.add("^" + lastCommitId.name());

							LogCommand log = new LogCommand(project.getGitDir()) {

								@Override
								protected void consume(GitCommit commit) {
									try {
										queue.put(Optional.of(commit));
									} catch (InterruptedException e) {
									}
								}
								
							}.revisions(revisions);
							
							executorService.execute(new Runnable() {

								@Override
								public void run() {
									try {
										log.call(null);
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
							
							try {
								Optional<GitCommit> logCommitOptional = queue.take();
								while (logCommitOptional.isPresent()) {
									GitCommit logCommit = logCommitOptional.get();
									
									if (logCommit.getCommitDate() != null) {
										for (String file: logCommit.getChangedFiles())
											files.put(file, logCommit.getCommitDate().getTime());
									}
									
									if (logCommit.getCommitter() != null)
										users.add(new NameAndEmail(logCommit.getCommitter()));

									if (logCommit.getAuthor() != null) {
										NameAndEmail nameAndEmail = new NameAndEmail(logCommit.getAuthor());
										users.add(nameAndEmail);
										
										String emailAddress = logCommit.getAuthor().getEmailAddress();
										if (StringUtils.isNotBlank(emailAddress)) {
											ByteIterable emailKey = new StringByteIterable(emailAddress);
											int userIndex = readInt(emailToIndexStore, txn, emailKey, -1);
											if (userIndex == -1) {
												userIndex = nextIndex.user++;
												writeInt(emailToIndexStore, txn, emailKey, userIndex);
												indexToUserStore.put(txn, 
														new IntByteIterable(userIndex), 
														new ArrayByteIterable(SerializationUtils.serialize(nameAndEmail)));
											}
											
											if (logCommit.getCommitDate() != null && logCommit.getParentHashes().size() <= 1) {
												int dayValue = new Day(logCommit.getCommitDate()).getValue();
												Map<Integer, Contribution> contributionsOnDay = 
														dailyContributionsCache.get(dayValue);
												if (contributionsOnDay == null) {
													contributionsOnDay = deserializeContributions(readBytes(
															dailyContributionsStore, txn, new IntByteIterable(dayValue)));
													dailyContributionsCache.put(dayValue, contributionsOnDay);
												}
												updateContribution(contributionsOnDay, userIndex, logCommit);
											}
											
											for (FileChange change: logCommit.getFileChanges()) {
												String path = change.getPath();
												int pathIndex = getPathIndex(pathToIndexStore, indexToPathStore, txn, 
														nextIndex, path);
												int edits = change.getAdditions() + change.getDeletions();
												if (edits < 0)
													edits = 100;
												updateEdits(editsStore, txn, editsCache, userIndex, pathIndex, edits);
												while (path.contains("/")) {
													path = StringUtils.substringBeforeLast(path, "/");
													pathIndex = getPathIndex(pathToIndexStore, indexToPathStore, txn, 
															nextIndex, path);
													updateEdits(editsStore, txn, editsCache, userIndex, pathIndex, edits);
												}
												pathIndex = getPathIndex(pathToIndexStore, indexToPathStore, txn, 
														nextIndex, "");
												updateEdits(editsStore, txn, editsCache, userIndex, pathIndex, edits);
											}
										}
									}
									
									if (logCommit.getCommitDate() != null && logCommit.getParentHashes().size() <= 1) {
										int dayValue = new Day(logCommit.getCommitDate()).getValue();
										updateContribution(overallContributions, dayValue, logCommit);
									}
									
									for (FileChange change: logCommit.getFileChanges()) {
										if (change.getOldPath() != null) {
											int pathIndex = getPathIndex(pathToIndexStore, indexToPathStore, txn, 
													nextIndex, change.getPath());
											ByteIterable pathKey = new IntByteIterable(pathIndex);
											Set<Integer> historyPathIndexes = new HashSet<>();
											byte[] bytesOfHistoryPaths = readBytes(historyPathsStore, txn, pathKey);
											if (bytesOfHistoryPaths == null) {
												bytesOfHistoryPaths = new byte[0];
												int pos = 0;
												for (int i=0; i<bytesOfHistoryPaths.length/Integer.SIZE; i++) {
													historyPathIndexes.add(ByteBuffer.wrap(bytesOfHistoryPaths, pos, Integer.SIZE).getInt());
													pos += Integer.SIZE;
												}
											} else {
												historyPathIndexes = new HashSet<>();
											}
											if (historyPathIndexes.size() < MAX_HISTORY_PATHS) {
												int oldPathIndex = getPathIndex(pathToIndexStore, indexToPathStore, txn, 
														nextIndex, change.getOldPath());
												if (!historyPathIndexes.contains(oldPathIndex)) {
													historyPathIndexes.add(oldPathIndex);
													byte[] newBytesOfHistoryPaths = 
															new byte[bytesOfHistoryPaths.length+Integer.SIZE];
													System.arraycopy(bytesOfHistoryPaths, 0, 
															newBytesOfHistoryPaths, 0, bytesOfHistoryPaths.length);
													ByteBuffer buffer = ByteBuffer.wrap(newBytesOfHistoryPaths, 
															bytesOfHistoryPaths.length, Integer.BYTES);
													buffer.putInt(oldPathIndex);
													historyPathsStore.put(txn, pathKey, 
															new ArrayByteIterable(newBytesOfHistoryPaths));
												}
											}
										}
									}		
									
									if (logCommit.getCommitDate() != null && logCommit.getParentHashes().size() <= 1) {
										int day = new Day(logCommit.getCommitDate()).getValue();
										
										Map<String, Integer> lineStatsOnDay = lineStats.get(day);
										if (lineStatsOnDay == null) {
											lineStatsOnDay = new HashMap<>();
											lineStats.put(day, lineStatsOnDay);
										}
										
										Map<String, Integer> languageLines = new HashMap<>();
										for (FileChange change: logCommit.getFileChanges()) {
											int lines = change.getAdditions() - change.getDeletions();
											int lastIndexOfDot = change.getPath().lastIndexOf('.');
											if (lastIndexOfDot != -1 && lines != 0) {
												String fileExt = change.getPath().substring(lastIndexOfDot+1).toLowerCase();
												String language = PROGRAMMING_LANGUAGES.get(fileExt);
												if (language != null) {
													Integer accumulatedLines = languageLines.get(language);
													if (accumulatedLines != null) 
														lines += accumulatedLines;
													languageLines.put(language, lines);
												}
											}
										}
										
										for (Map.Entry<String, Integer> entry: languageLines.entrySet()) {
											String language = entry.getKey();
											Integer lines = entry.getValue();
											Integer accumulatedLines = lineStatsOnDay.get(language);
											if (accumulatedLines != null)
												lines += accumulatedLines;
											lineStatsOnDay.put(language, lines);
										}

									}
									
									logCommitOptional = queue.take();
								}
								if (logException.get() != null)
									throw logException.get();
							} catch (Exception e) {
								throw ExceptionUtils.unchecked(e);
							}
							
							for (Map.Entry<Integer, Map<Integer, Contribution>> entry: dailyContributionsCache.entrySet()) {
								byte[] bytesOfContributionsOnDay = serializeContributions(entry.getValue());
								dailyContributionsStore.put(txn, new IntByteIterable(entry.getKey()), 
										new ArrayByteIterable(bytesOfContributionsOnDay));
							}
							defaultStore.put(txn, OVERALL_CONTRIBUTIONS_KEY, 
									new ArrayByteIterable(serializeContributions(overallContributions)));
							
							bytesOfLineStats = SerializationUtils.serialize((Serializable) lineStats);
							defaultStore.put(txn, LINE_STATS_KEY, new ArrayByteIterable(bytesOfLineStats));
							
							writeInt(defaultStore, txn, NEXT_USER_INDEX_KEY, nextIndex.user);
							writeInt(defaultStore, txn, NEXT_PATH_INDEX_KEY, nextIndex.path);
							
							userBytes = SerializationUtils.serialize((Serializable) users);
							defaultStore.put(txn, USERS_KEY, new ArrayByteIterable(userBytes));
							usersCache.remove(project.getId());
							
							if (files.size() > MAX_COLLECTING_FILES) {
								List<String> fileList = new ArrayList<>(files.keySet());
								fileList.sort((file1, file2)->files.get(file1).compareTo(files.get(file2)));
								for (int i=0; i<fileList.size() - MAX_COLLECTING_FILES; i++)
									files.remove(fileList.get(i));
							}
							fileBytes = SerializationUtils.serialize((Serializable) files);
							defaultStore.put(txn, FILES_KEY, new ArrayByteIterable(fileBytes));
							filesCache.remove(project.getId());
							
							for (Map.Entry<Long, Integer> entry: editsCache.entrySet()) 
								writeInt(editsStore, txn, new LongByteIterable(entry.getKey()), entry.getValue());
							
							defaultStore.put(txn, LAST_COMMIT_OF_DEFAULT_BRANCH_KEY, new CommitByteIterable(currentCommitId));
						}
					});
				}
			}
		}
	}
	
	private void updateContribution(Map<Integer, Contribution> contributions, int key, GitCommit commit) {
		Contribution contribution = contributions.get(key);
		if (contribution != null) {
			contribution = new Contribution(
					contribution.getCommits()+1, 
					contribution.getAdditions()+commit.getAdditions(), 
					contribution.getDeletions()+commit.getDeletions());
		} else {
			contribution = new Contribution(1, commit.getAdditions(), commit.getDeletions());
		}
		contributions.put(key, contribution);
	}
	
	private int getPathIndex(Store pathToIndexStore, Store indexToPathStore, Transaction txn, 
			NextIndex nextIndex, String path) {
		StringByteIterable pathKey = new StringByteIterable(path);
		int pathIndex = readInt(pathToIndexStore, txn, pathKey, -1);
		if (pathIndex == -1) {
			pathIndex = nextIndex.path++;
			writeInt(pathToIndexStore, txn, pathKey, pathIndex);
			indexToPathStore.put(txn, new IntByteIterable(pathIndex), new StringByteIterable(path));
		}
		return pathIndex;
	}
	
	private void updateEdits(Store store, Transaction txn, 
			Map<Long, Integer> editsCache, int userIndex, int pathIndex, int edits) {
		long editsKey = (userIndex<<32)|pathIndex;
		
		Integer editsOfPathByUser = editsCache.get(editsKey);
		if (editsOfPathByUser == null)
			editsOfPathByUser = readInt(store, txn, new LongByteIterable(editsKey), 0);
		editsOfPathByUser += edits;
		editsCache.put(editsKey, editsOfPathByUser);
	}
	
	@Override
	public List<NameAndEmail> getUsers(Project project) {
		List<NameAndEmail> users = usersCache.get(project.getId());
		if (users == null) {
			Environment env = getEnv(project.getId().toString());
			Store store = getStore(env, DEFAULT_STORE);

			users = env.computeInReadonlyTransaction(new TransactionalComputable<List<NameAndEmail>>() {

				@SuppressWarnings("unchecked")
				@Override
				public List<NameAndEmail> compute(Transaction txn) {
					byte[] bytes = readBytes(store, txn, USERS_KEY);
					if (bytes != null) { 
						List<NameAndEmail> users = 
								new ArrayList<>((Set<NameAndEmail>) SerializationUtils.deserialize(bytes));
						Collections.sort(users);
						return users;
					} else { 
						return new ArrayList<>();
					}
				}
				
			});
			usersCache.put(project.getId(), users);
		}
		return users;	
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
					byte[] bytes = readBytes(store, txn, FILES_KEY);
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

	@Override
	public Map<Day, Map<String, Integer>> getLineIncrements(Project project) {
		Environment env = getEnv(project.getId().toString());
		Store store = getStore(env, DEFAULT_STORE);

		return env.computeInReadonlyTransaction(new TransactionalComputable<Map<Day, Map<String, Integer>>>() {

			@Override
			public Map<Day, Map<String, Integer>> compute(Transaction txn) {
				Map<Day, Map<String, Integer>> lineIncrements = new HashMap<>();
				byte[] bytes = readBytes(store, txn, LINE_STATS_KEY);
				if (bytes != null) {
					@SuppressWarnings("unchecked")
					Map<Integer, Map<String, Integer>> storedMap = 
							(Map<Integer, Map<String, Integer>>) SerializationUtils.deserialize(bytes);
					for (Map.Entry<Integer, Map<String, Integer>> entry: storedMap.entrySet())
						lineIncrements.put(new Day(entry.getKey()), entry.getValue());
				} 
				return lineIncrements;
			}
			
		});
	}
	
	@Override
	public int getEdits(ProjectFacade project, UserFacade user, String path) {
		if (user.getEmail() != null) {
			Environment env = getEnv(project.getId().toString());
			Store emailToIndexStore = getStore(env, EMAIL_TO_INDEX_STORE);
			Store pathToIndexStore = getStore(env, PATH_TO_INDEX_STORE);
			Store editsStore = getStore(env, EDITS_STORE);
			return env.computeInReadonlyTransaction(new TransactionalComputable<Integer>() {

				@Override
				public Integer compute(Transaction txn) {
					int userIndex = readInt(emailToIndexStore, txn, new StringByteIterable(user.getEmail()), -1);
					if (userIndex != -1) {
						int pathIndex = readInt(pathToIndexStore, txn, new StringByteIterable(path), -1);
						if (pathIndex != -1) {
							long editsKey = (userIndex<<32)|pathIndex;
							return readInt(editsStore, txn, new LongByteIterable(editsKey), 0);
						} 
					} 
					return 0;
				}
			});
		} else {
			return 0;
		}
	}
	
	@Override
	public Collection<ObjectId> getDescendants(Project project, Collection<ObjectId> ancestors) {
		Environment env = getEnv(project.getId().toString());
		final Store store = getStore(env, COMMITS_STORE);

		return env.computeInReadonlyTransaction(new TransactionalComputable<Set<ObjectId>>() {

			@Override
			public Set<ObjectId> compute(Transaction txn) {
				Set<ObjectId> descendants = new HashSet<>();
				
				// Use stack instead of recursion to avoid StackOverflowException
				Stack<ObjectId> stack = new Stack<>();
				descendants.addAll(ancestors);
				stack.addAll(ancestors);
				while (!stack.isEmpty()) {
					ObjectId current = stack.pop();
					byte[] valueBytes = readBytes(store, txn, new CommitByteIterable(current));
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

	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Long projectId = event.getEntity().getId();
			removeEnv(projectId.toString());
			filesCache.remove(projectId);
			commitCountCache.remove(projectId);
			usersCache.remove(projectId);
		}
	}
	
	private BatchWorker getBatchWorker(Long projectId) {
		return new BatchWorker("project-" + projectId + "-collectCommitInfo") {

			@Override
			public void doWorks(Collection<Prioritized> works) {
				sessionManager.run(new Runnable() {

					@Override
					public void run() {
						Project project = projectManager.load(projectId);
						List<CollectingWork> collectingWorks = new ArrayList<>();
						for (Object work: works)
							collectingWorks.add((CollectingWork)work);
						Collections.sort(collectingWorks, new CommitTimeComparator());
						
						for (CollectingWork work: collectingWorks) {
							logger.debug("Collecting commit information up to ref '{}' in project '{}'...", 
									work.getRefName(), project.getName());
							doCollect(project, work.getCommit().copy(), work.getRefName());
						}
					}
					
				});
			}
			
		};		
	}
	
	private void collect(Project project) {
		List<CollectingWork> works = new ArrayList<>();
		try (RevWalk revWalk = new RevWalk(project.getRepository())) {
			Collection<Ref> refs = new ArrayList<>();
			refs.addAll(project.getRepository().getRefDatabase().getRefsByPrefix(Constants.R_HEADS));
			refs.addAll(project.getRepository().getRefDatabase().getRefsByPrefix(Constants.R_TAGS));

			for (Ref ref: refs) {
				RevObject revObj = revWalk.peel(revWalk.parseAny(ref.getObjectId()));
				if (revObj instanceof RevCommit)
					works.add(new CollectingWork(PRIORITY, (RevCommit) revObj, ref.getName()));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Collections.sort(works, new CommitTimeComparator());
		
		for (CollectingWork work: works)
			batchWorkManager.submit(getBatchWorker(project.getId()), work);
	}

	@Sessional
	@Listen
	public void on(SystemStarted event) {
		for (Project project: projectManager.query()) {
			checkVersion(project.getId().toString());
			collect(project);
		}
	}
	
	@Sessional
	@Listen
	public void on(RefUpdated event) {
		if (!event.getNewCommitId().equals(ObjectId.zeroId()) 
				&& (event.getRefName().startsWith(Constants.R_HEADS) 
						|| event.getRefName().startsWith(Constants.R_TAGS))) {
			try (RevWalk revWalk = new RevWalk(event.getProject().getRepository())) {
				RevCommit commit = GitUtils.parseCommit(revWalk, event.getNewCommitId());
				if (commit != null) {
					CollectingWork work = new CollectingWork(PRIORITY, commit, event.getRefName());
					batchWorkManager.submit(getBatchWorker(event.getProject().getId()), work);
				}
			}
		}
	}

	@Sessional
	@Override
	public int getCommitCount(Project project) {
		Integer commitCount = commitCountCache.get(project.getId());
		if (commitCount == null) {
			Environment env = getEnv(project.getId().toString());
			Store store = getStore(env, DEFAULT_STORE);

			commitCount = env.computeInReadonlyTransaction(new TransactionalComputable<Integer>() {

				@Override
				public Integer compute(Transaction txn) {
					return readInt(store, txn, COMMIT_COUNT_KEY, 0);
				}
			});
			commitCountCache.put(project.getId(), commitCount);
		}
		return commitCount;
	}

	static class CollectingWork extends Prioritized {
		
		private final String refName;
		
		private final RevCommit commit;
		
		public CollectingWork(int priority, RevCommit commit, String refName) {
			super(priority);
			this.commit = commit;
			this.refName = refName;
		}

		public RevCommit getCommit() {
			return commit;
		}

		public String getRefName() {
			return refName;
		}

	}
	
	static class CommitTimeComparator implements Comparator<CollectingWork> {

		@Override
		public int compare(CollectingWork o1, CollectingWork o2) {
			return o1.getCommit().getCommitTime() - o2.getCommit().getCommitTime();
		}
		
	}

	@Sessional
	@Override
	public void cloneInfo(Project source, Project target) {
		BackupStrategy backupStrategy = getEnv(source.getId().toString()).getBackupStrategy();
		try {
			File targetDir = getEnvDir(target.getId().toString());
			backupStrategy.beforeBackup();
			try {
				for (VirtualFileDescriptor descriptor: backupStrategy.getContents()) {
					FileUtils.copyFileToDirectory(((FileDescriptor)descriptor).getFile(), targetDir);
				}
			} finally {
				backupStrategy.afterBackup();
			}
			writeVersion(target.getId().toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected File getEnvDir(String envKey) {
		File infoDir = new File(storageManager.getProjectInfoDir(Long.valueOf(envKey)), INFO_DIR);
		if (!infoDir.exists()) 
			FileUtils.createDir(infoDir);
		return infoDir;
	}
	
	@Sessional
	@Override
	public Collection<String> getHistoryPaths(Project project, String path) {
		Environment env = getEnv(project.getId().toString());
		Store historyPathsStore = getStore(env, HISTORY_PATHS_STORE);
		Store pathToIndexStore = getStore(env, PATH_TO_INDEX_STORE);
		Store indexToPathStore = getStore(env, INDEX_TO_PATH_STORE);
		
		return env.computeInReadonlyTransaction(new TransactionalComputable<Collection<String>>() {

			private Collection<String> getPaths(Transaction txn, Set<Integer> pathIndexes) {
				Set<String> paths = new HashSet<>();
				for (int pathIndex: pathIndexes) {
					byte[] pathBytes = readBytes(indexToPathStore, txn, new IntByteIterable(pathIndex));
					if (pathBytes != null)
						paths.add(new String(pathBytes, Charsets.UTF_8));
				}
				return paths;
			}
			
			@Override
			public Collection<String> compute(Transaction txn) {
				int pathIndex = readInt(pathToIndexStore, txn, new StringByteIterable(path), -1);
				if (pathIndex != -1) {
					Set<Integer> pathIndexes = new HashSet<>();
					pathIndexes.add(pathIndex);
					while (true) {
						Set<Integer> newPathIndexes = new HashSet<>(pathIndexes);
						for (int eachPathIndex: pathIndexes) {
							byte[] bytesOfHistoryPaths = 
									readBytes(historyPathsStore, txn, new IntByteIterable(eachPathIndex));
							if (bytesOfHistoryPaths != null) {
								int pos = 0;
								for (int i=0; i<bytesOfHistoryPaths.length/Integer.BYTES; i++) {
									newPathIndexes.add(ByteBuffer.wrap(bytesOfHistoryPaths, pos, Integer.BYTES).getInt());
									if (newPathIndexes.size() == MAX_HISTORY_PATHS)
										return getPaths(txn, newPathIndexes);
									pos += Integer.BYTES;
								}
							}
						}
						if (pathIndexes.equals(newPathIndexes))
							break;
						else
							pathIndexes = newPathIndexes;
					}
					return getPaths(txn, pathIndexes);
				} else {
					return new HashSet<>();
				}
			}
		});
	}
	
	@Sessional
	@Override
	public Map<Day, Contribution> getOverallContributions(Project project) {
		Environment env = getEnv(project.getId().toString());
		Store store = getStore(env, DEFAULT_STORE);

		return env.computeInReadonlyTransaction(new TransactionalComputable<Map<Day, Contribution>>() {

			@Override
			public Map<Day, Contribution> compute(Transaction txn) {
				Map<Day, Contribution> overallContributions = new HashMap<>();
				for (Map.Entry<Integer, Contribution> entry: 
							deserializeContributions(readBytes(store, txn, OVERALL_CONTRIBUTIONS_KEY)).entrySet()) {
					overallContributions.put(new Day(entry.getKey()), entry.getValue());
				}
				return overallContributions;
			}
			
		});
	}
	
	@Sessional
	@Override
	public List<Contributor> getTopContributors(Project project, int top, Contribution.Type type, 
			Day fromDay, Day toDay) {
		Environment env = getEnv(project.getId().toString());
		Store defaultStore = getStore(env, DEFAULT_STORE);
		Store indexToUserStore = getStore(env, INDEX_TO_USER_STORE);
		Store dailyContributionsStore = getStore(env, DAILY_CONTRIBUTIONS_STORE);
		
		return env.computeInReadonlyTransaction(new TransactionalComputable<List<Contributor>>() {

			@Override
			public List<Contributor> compute(Transaction txn) {
				Map<Integer, Contribution> overallContributions = 
						deserializeContributions(readBytes(defaultStore, txn, OVERALL_CONTRIBUTIONS_KEY));
				Map<Integer, Contribution> totalContributions = new HashMap<>();
				for (int dayValue: overallContributions.keySet()) {
					if (dayValue >= fromDay.getValue() && dayValue <= toDay.getValue()) {
						ByteIterable dayKey = new IntByteIterable(dayValue);
						Map<Integer, Contribution> contributionsOnDay = 
								deserializeContributions(readBytes(dailyContributionsStore, txn, dayKey));
						for (Map.Entry<Integer, Contribution> entry: contributionsOnDay.entrySet()) {
							Integer userIndex = entry.getKey();
							Contribution totalContribution = totalContributions.get(userIndex);
							if (totalContribution == null) {
								totalContribution = entry.getValue();
							} else {
								totalContribution = new Contribution(
										totalContribution.getCommits() + entry.getValue().getCommits(), 
										totalContribution.getAdditions() + entry.getValue().getAdditions(), 
										totalContribution.getDeletions() + entry.getValue().getDeletions());
							}
							totalContributions.put(userIndex, totalContribution);
						}
					}
				}
				
				List<Integer> topUserIndexes = new ArrayList<>(totalContributions.keySet());
				Collections.sort(topUserIndexes, new Comparator<Integer>() {

					@Override
					public int compare(Integer o1, Integer o2) {
						if (type == Contribution.Type.COMMITS)
							return totalContributions.get(o2).getCommits() - totalContributions.get(o1).getCommits();
						else if (type == Contribution.Type.ADDITIONS)
							return totalContributions.get(o2).getAdditions() - totalContributions.get(o1).getAdditions();
						else
							return totalContributions.get(o2).getDeletions() - totalContributions.get(o1).getDeletions();
					}
					
				});

				if (top < topUserIndexes.size())
					topUserIndexes = topUserIndexes.subList(0, top);
				
				Set<Integer> topUserIndexSet = new HashSet<>(topUserIndexes);
				
				Map<Integer, Map<Day, Integer>> userContributions = new HashMap<>();
				
				for (int dayValue: overallContributions.keySet()) {
					if (dayValue >= fromDay.getValue() && dayValue <= toDay.getValue()) {
						ByteIterable dayKey = new IntByteIterable(dayValue);
						Map<Integer, Contribution> contributionsOnDay = 
								deserializeContributions(readBytes(dailyContributionsStore, txn, dayKey));
						Day day = new Day(dayValue);
						for (Map.Entry<Integer, Contribution> entry: contributionsOnDay.entrySet()) {
							Integer userIndex = entry.getKey();
							if (topUserIndexSet.contains(userIndex)) {
								Map<Day, Integer> contributionsByUser = userContributions.get(userIndex);
								if (contributionsByUser == null) {
									contributionsByUser = new HashMap<>();
									userContributions.put(userIndex, contributionsByUser);
								}
								if (type == Contribution.Type.COMMITS)
									contributionsByUser.put(day, entry.getValue().getCommits());
								else if (type == Contribution.Type.ADDITIONS)
									contributionsByUser.put(day, entry.getValue().getAdditions());
								else
									contributionsByUser.put(day, entry.getValue().getDeletions());
							}
						}
					}
				}

				List<Contributor> contributors = new ArrayList<>();
				
				for (int userIndex: topUserIndexes) {
					byte[] userBytes = readBytes(indexToUserStore, txn, new IntByteIterable(userIndex));
					Map<Day, Integer> contributionsByUser = userContributions.get(userIndex);
					if (userBytes != null && contributionsByUser != null) {
						PersonIdent user = ((NameAndEmail)SerializationUtils.deserialize(userBytes)).asPersonIdent();
						contributors.add(new Contributor(user, totalContributions.get(userIndex), contributionsByUser));
					}
				}
				
				return contributors;
			}
			
		});
	}

	private Map<Integer, Contribution> deserializeContributions(byte[] bytes) {
		if (bytes != null) {
			Map<Integer, Contribution> contributions = new HashMap<>();
			int pos = 0;
			for (int i=0; i<bytes.length/4/Integer.BYTES; i++) {
				int key = ByteBuffer.wrap(bytes, pos, Integer.BYTES).getInt();
				pos += Integer.BYTES;
				int commits = ByteBuffer.wrap(bytes, pos, Integer.BYTES).getInt(); 
				pos += Integer.BYTES;
				int additions = ByteBuffer.wrap(bytes, pos, Integer.BYTES).getInt();
				pos += Integer.BYTES;
				int deletions = ByteBuffer.wrap(bytes, pos, Integer.BYTES).getInt(); 
				pos += Integer.BYTES;
				contributions.put(key, new Contribution(commits, additions, deletions));
			}
			return contributions;
		} else {
			return new HashMap<>();
		}
	}
	
	private byte[] serializeContributions(Map<Integer, Contribution> contributions) {
		byte[] bytes = new byte[contributions.size()*Integer.BYTES*4];
		int pos = 0;
		for (Map.Entry<Integer, Contribution> entry: contributions.entrySet()) {
			byte[] keyBytes = ByteBuffer.allocate(Integer.BYTES).putInt(entry.getKey()).array(); 
			System.arraycopy(keyBytes, 0, bytes, pos, Integer.BYTES);
			pos += Integer.BYTES;
			byte[] commitsBytes = ByteBuffer.allocate(Integer.BYTES).putInt(entry.getValue().getCommits()).array();
			System.arraycopy(commitsBytes, 0, bytes, pos, Integer.BYTES);
			pos += Integer.BYTES;
			byte[] additionsBytes = ByteBuffer.allocate(Integer.BYTES).putInt(entry.getValue().getAdditions()).array();
			System.arraycopy(additionsBytes, 0, bytes, pos, Integer.BYTES);
			pos += Integer.BYTES;
			byte[] deletionsBytes = ByteBuffer.allocate(Integer.BYTES).putInt(entry.getValue().getDeletions()).array();
			System.arraycopy(deletionsBytes, 0, bytes, pos, Integer.BYTES);
			pos += Integer.BYTES;
		}
		return bytes;
	}
	
	@Override
	protected long getLogFileSize() {
		return LOG_FILE_SIZE;
	}

	@Override
	protected int getEnvVersion() {
		return INFO_VERSION;
	}

	private static class NextIndex {
		int user;
		
		int path;
	}

	@Override
	public Collection<ObjectId> getFixCommits(Project project, Long issueNumber) {
		Environment env = getEnv(project.getId().toString());
		Store store = getStore(env, FIX_COMMITS_STORE);
		
		return env.computeInTransaction(new TransactionalComputable<Collection<ObjectId>>() {
			
			@Override
			public Collection<ObjectId> compute(Transaction txn) {
				return readCommits(store, txn, new LongByteIterable(issueNumber));
			}
			
		});
		
	}

}
