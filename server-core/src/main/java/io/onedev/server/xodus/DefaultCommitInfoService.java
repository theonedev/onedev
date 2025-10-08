package io.onedev.server.xodus;

import static io.onedev.server.util.DateUtils.toLocalDate;
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.PathUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TarUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.service.EmailAddressService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.UserService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.project.ActiveServerChanged;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.event.project.issue.IssueCommitsAttached;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.git.GitContribution;
import io.onedev.server.git.GitContributor;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.command.FileChange;
import io.onedev.server.git.command.ListFileChangesCommand;
import io.onedev.server.git.command.ListFilesCommand;
import io.onedev.server.git.command.ListNumStatsCommand;
import io.onedev.server.git.command.LogCommand;
import io.onedev.server.git.command.LogCommit;
import io.onedev.server.git.command.RevListCommand;
import io.onedev.server.git.command.RevListCommand.Order;
import io.onedev.server.git.command.RevListOptions;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.util.ElementPumper;
import io.onedev.server.util.NameAndEmail;
import io.onedev.server.util.Pair;
import io.onedev.server.util.ProgrammingLanguageDetector;
import io.onedev.server.util.concurrent.BatchWorkExecutionService;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;
import io.onedev.server.util.facade.EmailAddressFacade;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.patternset.PatternSet;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalComputable;

@Singleton
public class DefaultCommitInfoService extends AbstractEnvironmentService
		implements CommitInfoService, Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DefaultCommitInfoService.class);

	private static final int INFO_VERSION = 19;

	private static final long LOG_FILE_SIZE = 256 * 1024;

	private static final int COLLECT_BATCH_SIZE = 10000;

	private static final int MAX_HISTORY_PATHS = 100;

	private static final int MAX_COMMIT_FILES = 100;

	private static final String INFO_DIR = "commit";

	private static final String DEFAULT_STORE = "default";

	private static final String COMMITS_STORE = "commits";

	private static final String FIX_COMMITS_STORE = "fixCommits";
	
	private static final String COMMIT_COUNTS_STORE = "commitCounts";

	private static final String HISTORY_PATHS_STORE = "historyPaths";

	private static final String PATH_TO_INDEX_STORE = "pathToIndex";

	private static final String INDEX_TO_PATH_STORE = "indexToPath";

	private static final String USER_TO_INDEX_STORE = "userToIndex";

	private static final String INDEX_TO_USER_STORE = "indexToUser";

	private static final String EMAIL_TO_INDEX_STORE = "emailToIndex";

	private static final String DAILY_CONTRIBUTIONS_STORE = "dailyContributions";

	private static final String USER_COMMITS_STORE = "userCommits";

	private static final ByteIterable NEXT_PATH_INDEX_KEY = new StringByteIterable("nextPathIndex");

	private static final ByteIterable NEXT_USER_INDEX_KEY = new StringByteIterable("nextUserIndex");

	private static final ByteIterable NEXT_EMAIL_INDEX_KEY = new StringByteIterable("nextEmailIndex");

	private static final ByteIterable LAST_COMMIT_KEY = new StringByteIterable("lastCommit");

	private static final ByteIterable LAST_COMMIT_OF_LINE_STATS_KEY = new StringByteIterable("lastCommitOfLineStats");

	private static final ByteIterable LAST_COMMIT_OF_CONTRIBUTIONS_KEY = new StringByteIterable("lastCommitOfContributions");

	private static final ByteIterable LAST_COMMIT_OF_FILES_KEY = new StringByteIterable("lastCommitOfFiles");

	private static final ByteIterable LINE_STATS_KEY = new StringByteIterable("lineStats");

	private static final ByteIterable USERS_KEY = new StringByteIterable("users");

	private static final ByteIterable FILES_KEY = new StringByteIterable("files");

	private static final ByteIterable FILE_COUNT_KEY = new StringByteIterable("fileCount");

	private static final ByteIterable OVERALL_CONTRIBUTIONS_KEY = new StringByteIterable("overallContributions");

	private static final ByteIterable COMMIT_COUNT_KEY = new StringByteIterable("commitCount");

	private static final int UPDATE_PRIORITY = 100;

	private static final int CHECK_PRIORITY = 200;

	private final BatchWorkExecutionService batchWorkExecutionService;

	private final ProjectService projectService;

	private final SessionService sessionService;

	private final ClusterService clusterService;

	private final UserService userService;

	private final EmailAddressService emailAddressService;

	private final IssueService issueService;
	
	private final ListenerRegistry listenerRegistry;

	private final Map<Long, List<String>> filesCache = new ConcurrentHashMap<>();

	private final Map<Long, Integer> fileCountCache = new ConcurrentHashMap<>();

	private final Map<Long, Integer> totalCommitCountCache = new ConcurrentHashMap<>();

	private final Map<Long, Pair<Set<NameAndEmail>, Set<String>>> usersCache = new ConcurrentHashMap<>();

	@Inject
	public DefaultCommitInfoService(ProjectService projectService,
									BatchWorkExecutionService batchWorkExecutionService, SessionService sessionService,
									EmailAddressService emailAddressService, UserService userService,
									ClusterService clusterService, ListenerRegistry listenerRegistry,
									IssueService issueService) {
		this.projectService = projectService;
		this.batchWorkExecutionService = batchWorkExecutionService;
		this.sessionService = sessionService;
		this.emailAddressService = emailAddressService;
		this.userService = userService;
		this.clusterService = clusterService;
		this.listenerRegistry = listenerRegistry;
		this.issueService = issueService;
	}

	private boolean isCommitCollected(byte[] commitBytes) {
		/*
		 * Collected commits stores an additional byte to differentiate from those not collected but with parent
		 * information stored
		 */
		return commitBytes != null && commitBytes.length % 20 != 0;
	}

	private void doCollect(Project project, ObjectId commitId, String refName) {
		logger.debug("Collecting commit information (project: {}, ref: {})...", project.getPath(), refName);

		Environment env = getEnv(project.getId().toString());
		Store defaultStore = getStore(env, DEFAULT_STORE);
		Store commitsStore = getStore(env, COMMITS_STORE);
		Store commitCountsStore = getStore(env, COMMIT_COUNTS_STORE);
		Store historyPathsStore = getStore(env, HISTORY_PATHS_STORE);
		Store pathToIndexStore = getStore(env, PATH_TO_INDEX_STORE);
		Store indexToPathStore = getStore(env, INDEX_TO_PATH_STORE);
		Store userToIndexStore = getStore(env, USER_TO_INDEX_STORE);
		Store emailToIndexStore = getStore(env, EMAIL_TO_INDEX_STORE);
		Store indexToUserStore = getStore(env, INDEX_TO_USER_STORE);
		Store fixCommitsStore = getStore(env, FIX_COMMITS_STORE);

		Repository repository = projectService.getRepository(project.getId());

		Pair<byte[], ObjectId> result = env.computeInTransaction(txn -> {
			var users = usersCache.get(project.getId());
			if (users == null) {
				byte[] userBytes = readBytes(defaultStore, txn, USERS_KEY);
				if (userBytes != null) {
					Set<NameAndEmail> nameAndEmails = SerializationUtils.deserialize(userBytes);
					users = new Pair<>(nameAndEmails,
							nameAndEmails.stream().map(NameAndEmail::getEmailAddress).collect(Collectors.toSet()));
				} else {
					users = new Pair<>(new HashSet<>(), new HashSet<>());
				}
				usersCache.put(project.getId(), users);
			}

			ByteIterable commitKey = new CommitByteIterable(commitId);
			byte[] commitBytes = readBytes(commitsStore, txn, commitKey);

			ObjectId lastCommitId;
			byte[] lastCommitBytes = readBytes(defaultStore, txn, LAST_COMMIT_KEY);
			if (lastCommitBytes != null) {
				lastCommitId = ObjectId.fromRaw(lastCommitBytes);
				try {
					if (!repository.getObjectDatabase().has(lastCommitId))
						lastCommitId = null;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				lastCommitId = null;
			}

			return new Pair<>(commitBytes, lastCommitId);
		});

		if (!isCommitCollected(result.getLeft())) {
			processCommitRange(project, commitId, result.getRight(), (untilCommitId, sinceCommitId) -> env.executeInTransaction(txn -> {
				AtomicInteger totalCommitCount = new AtomicInteger(readInt(defaultStore, txn, COMMIT_COUNT_KEY, 0));

				NextIndex nextIndex = new NextIndex();
				nextIndex.user = readInt(defaultStore, txn, NEXT_USER_INDEX_KEY, 0);
				nextIndex.email = readInt(defaultStore, txn, NEXT_EMAIL_INDEX_KEY, 0);
				nextIndex.path = readInt(defaultStore, txn, NEXT_PATH_INDEX_KEY, 0);

				Map<Long, Integer> commitCountCache = new HashMap<>();

				var users = usersCache.get(project.getId());				
				var usersChanged = new AtomicBoolean(false);

				new ElementPumper<LogCommit>() {

					@Override
					public void generate(Consumer<LogCommit> consumer) {
						List<String> revisions = new ArrayList<>();
						revisions.add(untilCommitId.name());

						if (sinceCommitId != null)
							revisions.add("^" + sinceCommitId.name());

						var options = new RevListOptions().revisions(revisions);
						EnumSet<LogCommand.Field> fields = EnumSet.allOf(LogCommand.Field.class);
						fields.remove(LogCommand.Field.LINE_CHANGES);
						new LogCommand(projectService.getGitDir(project.getId())) {
							
							@Override
							protected void consume(LogCommit commit) {
								consumer.accept(commit);
							}

						}.options(options).fields(fields).run();
					}

					@Override
					public void process(LogCommit currentCommit) {
						ObjectId currentCommitId = ObjectId.fromString(currentCommit.getHash());
						ByteIterable currentCommitKey = new CommitByteIterable(currentCommitId);
						byte[] currentCommitBytes = readBytes(commitsStore, txn, currentCommitKey);

						if (!isCommitCollected(currentCommitBytes)) {
							totalCommitCount.incrementAndGet();

							byte[] newCurrentCommitBytes;
							if (currentCommitBytes == null) {
								newCurrentCommitBytes = new byte[1];
							} else {
								newCurrentCommitBytes = new byte[1 + currentCommitBytes.length];
								System.arraycopy(currentCommitBytes, 0, newCurrentCommitBytes, 1, currentCommitBytes.length);
							}

							commitsStore.put(txn, currentCommitKey, new ArrayByteIterable(newCurrentCommitBytes));

							for (String parentCommitHash : currentCommit.getParentHashes()) {
								ByteIterable parentCommitKey = new CommitByteIterable(ObjectId.fromString(parentCommitHash));
								byte[] parentCommitBytes = readBytes(commitsStore, txn, parentCommitKey);
								byte[] newParentCommitBytes;
								if (parentCommitBytes != null) {
									newParentCommitBytes = new byte[parentCommitBytes.length + 20];
									System.arraycopy(parentCommitBytes, 0, newParentCommitBytes, 0, parentCommitBytes.length);
								} else {
									newParentCommitBytes = new byte[20];
								}
								currentCommitId.copyRawTo(newParentCommitBytes, newParentCommitBytes.length - 20);
								commitsStore.put(txn, parentCommitKey, new ArrayByteIterable(newParentCommitBytes));
							}

							String commitMessage = currentCommit.getSubject();
							if (currentCommit.getBody() != null)
								commitMessage += "\n\n" + currentCommit.getBody();

							Repository innerRepository = projectService.getRepository(project.getId());
							for (Long issueId : project.parseFixedIssueIds(commitMessage)) {
								ByteIterable key = new LongByteIterable(issueId);
								var fixingCommits = readCommits(fixCommitsStore, txn, key);
								fixingCommits.add(currentCommitId);
								writeCommits(fixCommitsStore, txn, key, fixingCommits);
								
								key  = new StringByteIterable(issueId + ".head");
								fixingCommits = readCommits(fixCommitsStore, txn, key);

								boolean addNextCommit = true;
								for (Iterator<ObjectId> it = fixingCommits.iterator(); it.hasNext(); ) {
									ObjectId fixCommit = it.next();
									if (GitUtils.isMergedInto(innerRepository, null, fixCommit, currentCommitId)) {
										it.remove();
									} else if (GitUtils.isMergedInto(innerRepository, null, currentCommitId, fixCommit)) {
										addNextCommit = false;
										break;
									}
								}
								if (addNextCommit)
									fixingCommits.add(currentCommitId);
								writeCommits(fixCommitsStore, txn, key, fixingCommits);
								
								listenerRegistry.post(new IssueCommitsAttached(issueService.load(issueId)));
							}
							
							if (currentCommit.getCommitter() != null) {
								if (users.getLeft().add(new NameAndEmail(currentCommit.getCommitter())))
									usersChanged.set(true);
								users.getRight().add(currentCommit.getCommitter().getEmailAddress());
							}

							if (currentCommit.getAuthor() != null) {
								NameAndEmail nameAndEmail = new NameAndEmail(currentCommit.getAuthor());
								if (users.getLeft().add(nameAndEmail))
									usersChanged.set(true);
								users.getRight().add(nameAndEmail.getEmailAddress());

								ByteIterable authorKey = new ArrayByteIterable(SerializationUtils.serialize(nameAndEmail));
								int userIndex = readInt(userToIndexStore, txn, authorKey, -1);
								if (userIndex == -1) {
									userIndex = nextIndex.user++;
									writeInt(userToIndexStore, txn, authorKey, userIndex);
									indexToUserStore.put(txn, new IntByteIterable(userIndex), authorKey);
								}

								ByteIterable emailKey = new StringByteIterable(nameAndEmail.getEmailAddress());
								int emailIndex = readInt(emailToIndexStore, txn, emailKey, -1);
								if (emailIndex == -1) {
									emailIndex = nextIndex.email++;
									writeInt(emailToIndexStore, txn, emailKey, emailIndex);
								}

								for (FileChange change : currentCommit.getFileChanges()) {
									for (String path : change.getPaths()) {
										int pathIndex = getPathIndex(pathToIndexStore, indexToPathStore, txn,
												nextIndex, path);
										updateCommitCount(commitCountsStore, txn, commitCountCache, emailIndex, pathIndex);
										while (path.contains("/")) {
											path = StringUtils.substringBeforeLast(path, "/");
											pathIndex = getPathIndex(pathToIndexStore, indexToPathStore, txn,
													nextIndex, path);
											updateCommitCount(commitCountsStore, txn, commitCountCache, emailIndex, pathIndex);
										}
										pathIndex = getPathIndex(pathToIndexStore, indexToPathStore, txn,
												nextIndex, "");
										updateCommitCount(commitCountsStore, txn, commitCountCache, emailIndex, pathIndex);
									}
								}
							}

							for (FileChange change : currentCommit.getFileChanges()) {
								if (change.getOldPath() != null && change.getNewPath() != null
										&& !change.getOldPath().equals(change.getNewPath())) {
									int pathIndex = getPathIndex(pathToIndexStore, indexToPathStore, txn,
											nextIndex, change.getNewPath());
									ByteIterable pathKey = new IntByteIterable(pathIndex);
									Set<Integer> historyPathIndexes = new HashSet<>();
									byte[] bytesOfHistoryPaths = readBytes(historyPathsStore, txn, pathKey);
									if (bytesOfHistoryPaths == null) {
										bytesOfHistoryPaths = new byte[0];
										int pos = 0;
										for (int i = 0; i < bytesOfHistoryPaths.length / Integer.SIZE; i++) {
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
													new byte[bytesOfHistoryPaths.length + Integer.SIZE];
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
						}
					}

				}.pump();

				writeInt(defaultStore, txn, COMMIT_COUNT_KEY, totalCommitCount.get());
				totalCommitCountCache.remove(project.getId());

				writeInt(defaultStore, txn, NEXT_USER_INDEX_KEY, nextIndex.user);
				writeInt(defaultStore, txn, NEXT_EMAIL_INDEX_KEY, nextIndex.email);
				writeInt(defaultStore, txn, NEXT_PATH_INDEX_KEY, nextIndex.path);

				if (usersChanged.get()) {
					var userBytes = SerializationUtils.serialize((Serializable) users.getLeft());
					defaultStore.put(txn, USERS_KEY, new ArrayByteIterable(userBytes));
				}

				for (Map.Entry<Long, Integer> entry : commitCountCache.entrySet())
					writeInt(commitCountsStore, txn, new LongByteIterable(entry.getKey()), entry.getValue());

				defaultStore.put(txn, LAST_COMMIT_KEY, new CommitByteIterable(untilCommitId));
			}));
		}

		if (GitUtils.branch2ref(project.getDefaultBranch()).equals(refName)) {
			collectLineStats(project, commitId);
			collectContributions(project, commitId);
			collectFiles(project, commitId);
		}

		logger.debug("Collected commit information (project: {}, ref: {})", project.getPath(), refName);
	}

	private void collectContributions(Project project, ObjectId commitId) {
		Environment env = getEnv(project.getId().toString());
		Store defaultStore = getStore(env, DEFAULT_STORE);
		Store userToIndexStore = getStore(env, USER_TO_INDEX_STORE);
		Store dailyContributionsStore = getStore(env, DAILY_CONTRIBUTIONS_STORE);
		Store userCommitsStore = getStore(env, USER_COMMITS_STORE);

		Repository repository = projectService.getRepository(project.getId());
		PatternSet filePatterns = PatternSet.parse(project.findCodeAnalysisPatterns());

		ObjectId lastCommitId = env.computeInTransaction(txn -> {
			ObjectId innerLastCommitId;
			byte[] lastCommitBytes = readBytes(defaultStore, txn, LAST_COMMIT_OF_CONTRIBUTIONS_KEY);
			if (lastCommitBytes != null) {
				innerLastCommitId = ObjectId.fromRaw(lastCommitBytes);
				try {
					if (!repository.getObjectDatabase().has(innerLastCommitId))
						innerLastCommitId = null;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				innerLastCommitId = null;
			}
			return innerLastCommitId;
		});

		processCommitRange(project, commitId, lastCommitId, (untilCommitId, sinceCommitId) -> env.executeInTransaction(txn -> {
			Map<Integer, GitContribution> overallContributions =
					deserializeDailyContributions(readBytes(defaultStore, txn, OVERALL_CONTRIBUTIONS_KEY));

			Map<Integer, Map<Integer, GitContribution>> dailyContributionsCache = new HashMap<>();

			Map<String, Map<ObjectId, Long>> userCommitsCache = new HashMap<>();

			new ElementPumper<LogCommit>() {

				@Override
				public void generate(Consumer<LogCommit> consumer) {
					List<String> revisions = new ArrayList<>();
					revisions.add(untilCommitId.name());

					if (sinceCommitId != null)
						revisions.add("^" + sinceCommitId.name());

					var options = new RevListOptions().revisions(revisions);
					
					EnumSet<LogCommand.Field> fields = EnumSet.of(
							LogCommand.Field.AUTHOR,
							LogCommand.Field.COMMIT_DATE,
							LogCommand.Field.PARENTS,
							LogCommand.Field.LINE_CHANGES);

					new LogCommand(projectService.getGitDir(project.getId())) {

						@Override
						protected void consume(LogCommit commit) {
							consumer.accept(commit);
						}

					}.options(options).fields(fields).run();
				}

				@Override
				public void process(LogCommit currentCommit) {
					if (currentCommit.getCommitDate() != null && currentCommit.getParentHashes().size() <= 1) {
						int day = (int) toLocalDate(currentCommit.getCommitDate(), ZoneId.systemDefault()).toEpochDay();
						updateContrib(overallContributions, day, currentCommit, filePatterns);

						if (currentCommit.getAuthor() != null) {
							NameAndEmail author = new NameAndEmail(currentCommit.getAuthor());
							ByteIterable authorKey = new ArrayByteIterable(SerializationUtils.serialize(author));
							int userIndex = readInt(userToIndexStore, txn, authorKey, -1);
							Preconditions.checkState(userIndex != -1);

							Map<Integer, GitContribution> contribsOnDay = dailyContributionsCache.get(day);
							if (contribsOnDay == null) {
								contribsOnDay = deserializeDailyContributions(readBytes(
										dailyContributionsStore, txn, new IntByteIterable(day)));
								dailyContributionsCache.put(day, contribsOnDay);
							}
							updateContrib(contribsOnDay, userIndex, currentCommit, filePatterns);

							var userCommits = userCommitsCache.get(author.getEmailAddress());
							if (userCommits == null) {
								userCommits = deserializeUserCommits(readBytes(userCommitsStore, txn, new StringByteIterable(author.getEmailAddress())));
								userCommitsCache.put(author.getEmailAddress(), userCommits);
							}
							userCommits.put(ObjectId.fromString(currentCommit.getHash()), currentCommit.getAuthor().getWhen().getTime());
						}
					}
				}

			}.pump();

			for (Map.Entry<Integer, Map<Integer, GitContribution>> entry : dailyContributionsCache.entrySet()) {
				byte[] bytesOfContributionsOnDay = serializeDailyContributions(entry.getValue());
				dailyContributionsStore.put(txn, new IntByteIterable(entry.getKey()),
						new ArrayByteIterable(bytesOfContributionsOnDay));
			}

			for (Map.Entry<String, Map<ObjectId, Long>> entry : userCommitsCache.entrySet()) {
				userCommitsStore.put(txn, new StringByteIterable(entry.getKey()), 
						new ArrayByteIterable(serializeUserCommits(entry.getValue())));
			}

			defaultStore.put(txn, OVERALL_CONTRIBUTIONS_KEY,
					new ArrayByteIterable(serializeDailyContributions(overallContributions)));

			defaultStore.put(txn, LAST_COMMIT_OF_CONTRIBUTIONS_KEY, new CommitByteIterable(untilCommitId));
		}));
	}

	private void collectFiles(Project project, ObjectId commitId) {
		Environment env = getEnv(project.getId().toString());
		Store defaultStore = getStore(env, DEFAULT_STORE);

		Repository repository = projectService.getRepository(project.getId());

		ObjectId lastCommitId = env.computeInTransaction(txn -> {
			ObjectId innerLastCommitId;
			byte[] lastCommitBytes = readBytes(defaultStore, txn, LAST_COMMIT_OF_FILES_KEY);
			if (lastCommitBytes != null) {
				innerLastCommitId = ObjectId.fromRaw(lastCommitBytes);
				try {
					if (!repository.getObjectDatabase().has(innerLastCommitId))
						innerLastCommitId = null;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				innerLastCommitId = null;
			}
			return innerLastCommitId;
		});

		if (lastCommitId == null) {
			env.executeInTransaction(txn -> {
				File gitDir = projectService.getGitDir(project.getId());
				Collection<String> files = new ListFilesCommand(gitDir, commitId.name()).run();

				byte[] bytesOfFiles = SerializationUtils.serialize((Serializable) files);
				defaultStore.put(txn, FILES_KEY, new ArrayByteIterable(bytesOfFiles));
				writeInt(defaultStore, txn, FILE_COUNT_KEY, files.size());
				defaultStore.put(txn, LAST_COMMIT_OF_FILES_KEY, new CommitByteIterable(commitId));
				filesCache.remove(project.getId());
				fileCountCache.remove(project.getId());
			});
		} else {
			env.executeInTransaction(txn -> {
				Collection<String> files;
				byte[] bytesOfFiles = readBytes(defaultStore, txn, FILES_KEY);
				if (bytesOfFiles != null)
					files = SerializationUtils.deserialize(bytesOfFiles);
				else
					files = new HashSet<>();

				boolean filesChanged = false;
				ListFileChangesCommand command = new ListFileChangesCommand(
						projectService.getGitDir(project.getId()),
						lastCommitId.name(), commitId.name());
				for (FileChange change : command.run()) {
					if (change.getOldPath() == null && change.getNewPath() != null) {
						files.add(change.getNewPath());
						filesChanged = true;
					} else if (change.getOldPath() != null && change.getNewPath() == null) {
						files.remove(change.getOldPath());
						filesChanged = true;
					}
				}

				if (filesChanged) {
					bytesOfFiles = SerializationUtils.serialize((Serializable) files);
					defaultStore.put(txn, FILES_KEY, new ArrayByteIterable(bytesOfFiles));
					writeInt(defaultStore, txn, FILE_COUNT_KEY, files.size());
					defaultStore.put(txn, LAST_COMMIT_OF_FILES_KEY, new CommitByteIterable(commitId));
					filesCache.remove(project.getId());
					fileCountCache.remove(project.getId());
				}
			});
		}
	}

	private void collectLineStats(Project project, ObjectId commitId) {
		Environment env = getEnv(project.getId().toString());
		Store defaultStore = getStore(env, DEFAULT_STORE);

		Repository repository = projectService.getRepository(project.getId());

		ObjectId lastCommitId = env.computeInTransaction(txn -> {
			byte[] lastCommitBytes = readBytes(defaultStore, txn, LAST_COMMIT_OF_LINE_STATS_KEY);
			if (lastCommitBytes != null) {
				try {
					ObjectId innerLastCommitId = ObjectId.fromRaw(lastCommitBytes);
					if (repository.getObjectDatabase().has(innerLastCommitId)
							&& GitUtils.isMergedInto(repository, null, innerLastCommitId, commitId)) {
						return innerLastCommitId;
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			return null;
		});

		PatternSet filePatterns = PatternSet.parse(project.findCodeAnalysisPatterns());
		if (lastCommitId == null) {
			env.executeInTransaction(txn -> {
				Map<Integer, Map<String, Integer>> lineStats = new HashMap<>();

				new ElementPumper<LogCommit>() {

					@Override
					public void generate(Consumer<LogCommit> consumer) {
						List<String> revisions = new ArrayList<>();
						revisions.add(commitId.name());

						EnumSet<LogCommand.Field> fields = EnumSet.of(
								LogCommand.Field.COMMIT_DATE,
								LogCommand.Field.LINE_CHANGES);

						var options = new RevListOptions().revisions(revisions).firstParent(true);
						new LogCommand(projectService.getGitDir(project.getId())) {

							@Override
							protected void consume(LogCommit commit) {
								consumer.accept(commit);
							}

						}.options(options).noRenames(true).fields(fields).run();
					}

					@Override
					public void process(LogCommit currentCommit) {
						updateLineStats(txn, currentCommit, lineStats, filePatterns);
					}

				}.pump();

				byte[] bytesOfLineStats = SerializationUtils.serialize((Serializable) lineStats);
				defaultStore.put(txn, LINE_STATS_KEY, new ArrayByteIterable(bytesOfLineStats));

				defaultStore.put(txn, LAST_COMMIT_OF_LINE_STATS_KEY, new CommitByteIterable(commitId));
			});
		} else {
			env.executeInTransaction(txn -> {
				Map<Integer, Map<String, Integer>> lineStats;
				byte[] bytesOfLineStats = readBytes(defaultStore, txn, LINE_STATS_KEY);
				if (bytesOfLineStats != null) 
					lineStats = SerializationUtils.deserialize(bytesOfLineStats);
				else 
					lineStats = new HashMap<>();

				ListNumStatsCommand command = new ListNumStatsCommand(
						projectService.getGitDir(project.getId()),
						lastCommitId.name(), commitId.name(), true);
				List<FileChange> fileChanges = command.run();
				RevCommit revCommit = project.getRevCommit(commitId, true);
				LogCommit gitCommit = new LogCommit(revCommit.name(), null, null, revCommit.getAuthorIdent(),
						revCommit.getCommitterIdent().getWhen(), null, null, fileChanges);

				updateLineStats(txn, gitCommit, lineStats, filePatterns);

				bytesOfLineStats = SerializationUtils.serialize((Serializable) lineStats);
				defaultStore.put(txn, LINE_STATS_KEY, new ArrayByteIterable(bytesOfLineStats));

				defaultStore.put(txn, LAST_COMMIT_OF_LINE_STATS_KEY, new CommitByteIterable(commitId));
			});
		}
	}

	private void processCommitRange(Project project, ObjectId untilCommitId,
									@Nullable ObjectId sinceCommitId, CommitRangeProcessor commitRangeProcessor) {
		RevListCommand revList = new RevListCommand(projectService.getGitDir(project.getId()));
		List<String> revisions = new ArrayList<>();
		revisions.add(untilCommitId.name());
		if (sinceCommitId != null)
			revisions.add("^" + sinceCommitId.name());
		revList.options().revisions(revisions).order(Order.TOPO);

		List<ObjectId> historyIds = new ArrayList<>();
		for (String commitHash : revList.run())
			historyIds.add(ObjectId.fromString(commitHash));

		revList = new RevListCommand(projectService.getGitDir(project.getId()));
		revList.options().revisions(revisions).firstParent(true);

		Set<ObjectId> firstParentIds = new HashSet<>();
		for (String commitHash : revList.run())
			firstParentIds.add(ObjectId.fromString(commitHash));

		/*
		 * Instead of collecting information of main branch all at once, we identify some
		 * intermediate commits and collect the information using these intermediate commits
		 * multiple times for two reasons:
		 * 1. Use less memory
		 * 2. Commit Exodus transaction sooner so user can use auto-completion when search
		 * commits even if collection is not done yet
		 */
		List<ObjectId> intermediateCommitIds = new ArrayList<>();
		int count = 0;
		for (ObjectId historyId : historyIds) {
			count++;
			/*
			 * Only use intermediate commits that are part of first parent chain. This
			 * makes sure that subsequent intermediate commits are always ancestor of
			 * current intermediate commit (after reverse done below), to avoid
			 * collecting some commits multiple times
			 */
			if (count > COLLECT_BATCH_SIZE && firstParentIds.contains(historyId)) {
				intermediateCommitIds.add(historyId);
				count = 0;
			}
		}

		Collections.reverse(intermediateCommitIds);
		intermediateCommitIds.add(untilCommitId);

		for (ObjectId intermediateCommitId : intermediateCommitIds) {
			commitRangeProcessor.process(intermediateCommitId, sinceCommitId);
			sinceCommitId = intermediateCommitId;
		}
	}

	private void updateLineStats(Transaction txn, LogCommit currentCommit, Map<Integer, Map<String, Integer>> lineStats,
								 PatternSet filePatterns) {
		int day = (int) toLocalDate(currentCommit.getCommitDate(), ZoneId.systemDefault()).toEpochDay();

		Map<String, Integer> lineStatsOnDay = lineStats.get(day);
		if (lineStatsOnDay == null) {
			lineStatsOnDay = new HashMap<>();
			lineStats.put(day, lineStatsOnDay);
		}

		Map<String, Integer> languageLines = new HashMap<>();
		for (FileChange change : currentCommit.getFileChanges()) {
			if (change.matches(filePatterns)) {
				int lines = change.getAdditions() - change.getDeletions();
				if (lines != 0 && StringUtils.isNotBlank(change.getNewExtension())) {
					String language = ProgrammingLanguageDetector.getLanguageForExtension(change.getNewExtension());
					if (language != null) {
						Integer accumulatedLines = languageLines.get(language);
						if (accumulatedLines != null)
							lines += accumulatedLines;
						languageLines.put(language, lines);
					}
				}
			}
		}

		for (Map.Entry<String, Integer> entry : languageLines.entrySet()) {
			String language = entry.getKey();
			Integer lines = entry.getValue();
			Integer accumulatedLines = lineStatsOnDay.get(language);
			if (accumulatedLines != null)
				lines += accumulatedLines;
			lineStatsOnDay.put(language, lines);
		}
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

	private void updateCommitCount(Store store, Transaction txn,
								   Map<Long, Integer> commitCountCache, int emailIndex, int pathIndex) {
		long commitCountKey = ((long) emailIndex << 32) | pathIndex;

		Integer commitCountOfPathByEmail = commitCountCache.get(commitCountKey);
		if (commitCountOfPathByEmail == null)
			commitCountOfPathByEmail = readInt(store, txn, new LongByteIterable(commitCountKey), 0);
		commitCountOfPathByEmail++;
		commitCountCache.put(commitCountKey, commitCountOfPathByEmail);
	}

	@Override
	public List<NameAndEmail> getUsers(Long projectId) {
		return projectService.runOnActiveServer(projectId, new ClusterTask<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<NameAndEmail> call() {
				var users = usersCache.get(projectId);
				if (users != null) {
					var sortedUsers = new ArrayList<>(users.getLeft());
					Collections.sort(sortedUsers);
					return sortedUsers;
				} else {
					return new ArrayList<>();
				}
			}

		});

	}

	@Override
	public List<String> getFiles(Long projectId) {
		return projectService.runOnActiveServer(projectId, new ClusterTask<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<String> call() {
				List<String> files = filesCache.get(projectId);
				if (files == null) {
					Environment env = getEnv(projectId.toString());
					Store store = getStore(env, DEFAULT_STORE);

					files = env.computeInReadonlyTransaction(txn -> {
						byte[] bytes = readBytes(store, txn, FILES_KEY);
						if (bytes != null) {
							@SuppressWarnings("unchecked")
							List<String> innerFiles = new ArrayList<>((Collection<String>) SerializationUtils.deserialize(bytes));
							Map<String, List<String>> segmentsMap = new HashMap<>();
							Splitter splitter = Splitter.on("/");
							for (String file : innerFiles)
								segmentsMap.put(file, splitter.splitToList(file));
							innerFiles.sort((o1, o2) -> PathUtils.compare(segmentsMap.get(o1), segmentsMap.get(o2)));
							return innerFiles;
						} else {
							return new ArrayList<>();
						}
					});
					filesCache.put(projectId, files);
				}
				return files;
			}

		});

	}

	@Override
	public Map<Integer, Map<String, Integer>> getLineIncrements(Long projectId) {
		return projectService.runOnActiveServer(projectId, new ClusterTask<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Map<Integer, Map<String, Integer>> call() throws Exception {
				Environment env = getEnv(projectId.toString());
				Store store = getStore(env, DEFAULT_STORE);

				return env.computeInReadonlyTransaction(txn -> {
					Map<Integer, Map<String, Integer>> lineIncrements = new HashMap<>();
					byte[] bytes = readBytes(store, txn, LINE_STATS_KEY);
					if (bytes != null) {
						@SuppressWarnings("unchecked")
						var storedMap = (Map<Integer, Map<String, Integer>>) SerializationUtils.deserialize(bytes);
						for (var entry : storedMap.entrySet())
							lineIncrements.put(entry.getKey(), entry.getValue());
					}
					return lineIncrements;
				});
			}

		});

	}

	private void updateContrib(Map<Integer, GitContribution> contributions, int key, LogCommit commit,
									PatternSet filePatterns) {
		GitContribution contribution = contributions.get(key);
		if (contribution != null) {
			contribution = new GitContribution(
					contribution.getCommits() + 1,
					contribution.getAdditions() + commit.getAdditions(filePatterns),
					contribution.getDeletions() + commit.getDeletions(filePatterns));
		} else {
			contribution = new GitContribution(
					1, commit.getAdditions(filePatterns), commit.getDeletions(filePatterns));
		}
		contributions.put(key, contribution);
	}

	private int getCommitCount(Long projectId, Collection<EmailAddressFacade> emailAddresses,
							   String path) {
		Environment env = getEnv(projectId.toString());
		Store emailToIndexStore = getStore(env, EMAIL_TO_INDEX_STORE);
		Store pathToIndexStore = getStore(env, PATH_TO_INDEX_STORE);
		Store commitCountStore = getStore(env, COMMIT_COUNTS_STORE);

		return env.computeInReadonlyTransaction(txn -> {
			AtomicInteger count = new AtomicInteger(0);
			emailAddresses.stream().filter(it -> it.isVerified()).forEach(it -> {
				int emailIndex = readInt(emailToIndexStore, txn, new StringByteIterable(it.getValue()), -1);
				if (emailIndex != -1) {
					int pathIndex = readInt(pathToIndexStore, txn, new StringByteIterable(path), -1);
					if (pathIndex != -1) {
						long commitCountKey = ((long) emailIndex << 32) | pathIndex;
						count.addAndGet(readInt(commitCountStore, txn, new LongByteIterable(commitCountKey), 0));
					}
				}
			});
			return count.get();
		});
	}

	@Override
	public Collection<ObjectId> getDescendants(Long projectId, Collection<ObjectId> ancestors) {
		return projectService.runOnActiveServer(projectId, new ClusterTask<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Collection<ObjectId> call() {
				Environment env = getEnv(projectId.toString());
				final Store store = getStore(env, COMMITS_STORE);

				return env.computeInReadonlyTransaction(txn -> {
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
								for (int i = 0; i < valueBytes.length / 20; i++) {
									ObjectId child = ObjectId.fromRaw(valueBytes, i * 20);
									if (!descendants.contains(child)) {
										descendants.add(child);
										stack.push(child);
									}
								}
							} else {
								for (int i = 0; i < (valueBytes.length - 1) / 20; i++) {
									ObjectId child = ObjectId.fromRaw(valueBytes, i * 20 + 1);
									if (!descendants.contains(child)) {
										descendants.add(child);
										stack.push(child);
									}
								}
							}
						}
					}

					return descendants;
				});
			}

		});
	}

	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Long projectId = event.getEntity().getId();
			String activeServer = projectService.getActiveServer(projectId, false);
			if (activeServer != null) {
				clusterService.runOnServer(activeServer, () -> {
					removeEnv(projectId.toString());
					filesCache.remove(projectId);
					totalCommitCountCache.remove(projectId);
					fileCountCache.remove(projectId);
					usersCache.remove(projectId);

					return null;
				});
			}
		}
	}

	private BatchWorker getBatchWorker(Long projectId) {
		return new BatchWorker("project-" + projectId + "-collectCommitInfo") {

			@Override
			public void doWorks(List<Prioritized> works) {
				sessionService.run(() -> {
					Project project = projectService.load(projectId);
					List<CollectingWork> collectingWorks = new ArrayList<>();
					for (Object work : works) {
						if (work instanceof CollectingWork) {
							collectingWorks.add((CollectingWork) work);
						} else if (work instanceof CheckingWork) {
							try (RevWalk revWalk = new RevWalk(projectService.getRepository(projectId))) {
								Collection<Ref> refs = new ArrayList<>();
								refs.addAll(projectService.getRepository(projectId).getRefDatabase()
										.getRefsByPrefix(Constants.R_HEADS));
								refs.addAll(projectService.getRepository(projectId).getRefDatabase()
										.getRefsByPrefix(Constants.R_TAGS));

								for (Ref ref : refs) {
									RevObject revObj;
									try {
										revObj = revWalk.peel(revWalk.parseAny(ref.getObjectId()));
									} catch (MissingObjectException e) {
										var message = String.format("%s (project id: %d, ref: %s)", e.getMessage(),
												projectId, ref.getName());
										throw new ExplicitException(message);
									}
									if (revObj instanceof RevCommit) {
										RevCommit commit = (RevCommit) revObj;
										collectingWorks.add(new CollectingWork(CHECK_PRIORITY, commit.copy(),
												commit.getCommitTime(), ref.getName()));
									}
								}
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					}
					collectingWorks.sort(new CommitTimeComparator());

					for (CollectingWork work : collectingWorks)
						doCollect(project, work.getCommitId(), work.getRefName());
				});
			}

		};
	}

	@Sessional
	@Listen
	public void on(SystemStarted event) {
		for (var projectId: projectService.getActiveIds()) {
			checkVersion(getEnvDir(projectId.toString()));
			batchWorkExecutionService.submit(getBatchWorker(projectId), new CheckingWork(CHECK_PRIORITY));
		}
	}
	
	@Sessional
	@Listen
	public void on(ActiveServerChanged event) {
		for (var projectId: event.getProjectIds()) {
			checkVersion(getEnvDir(projectId.toString()));
			batchWorkExecutionService.submit(getBatchWorker(projectId), new CheckingWork(CHECK_PRIORITY));
		}
	}
	
	@Sessional
	@Listen
	public void on(RefUpdated event) {
		if (!event.getNewCommitId().equals(ObjectId.zeroId())
				&& (event.getRefName().startsWith(Constants.R_HEADS)
				|| event.getRefName().startsWith(Constants.R_TAGS))) {
			Repository repository = projectService.getRepository(event.getProject().getId());
			try (RevWalk revWalk = new RevWalk(repository)) {
				RevCommit commit = GitUtils.parseCommit(revWalk, event.getNewCommitId());
				if (commit != null) {
					CollectingWork work = new CollectingWork(UPDATE_PRIORITY, commit.copy(), 
							commit.getCommitTime(), event.getRefName());
					batchWorkExecutionService.submit(getBatchWorker(event.getProject().getId()), work);
				}
			}
		}
	}

	@Sessional
	@Override
	public int getCommitCount(Long projectId) {
		return projectService.runOnActiveServer(projectId, new ClusterTask<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Integer call() {
				Integer commitCount = totalCommitCountCache.get(projectId);
				if (commitCount == null) {
					Environment env = getEnv(projectId.toString());
					Store store = getStore(env, DEFAULT_STORE);

					commitCount = env.computeInReadonlyTransaction(txn -> readInt(store, txn, COMMIT_COUNT_KEY, 0));
					totalCommitCountCache.put(projectId, commitCount);
				}
				return commitCount;
			}

		});
	}

	@Override
	public int getFileCount(Long projectId) {
		return projectService.runOnActiveServer(projectId, new ClusterTask<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Integer call() {
				Integer fileCount = fileCountCache.get(projectId);
				if (fileCount == null) {
					Environment env = getEnv(projectId.toString());
					Store store = getStore(env, DEFAULT_STORE);

					fileCount = env.computeInReadonlyTransaction(txn -> readInt(store, txn, FILE_COUNT_KEY, 0));
					fileCountCache.put(projectId, fileCount);
				}
				return fileCount;
			}

		});
	}

	static class CheckingWork extends Prioritized {

		public CheckingWork(int priority) {
			super(priority);
		}

	}

	static class CollectingWork extends Prioritized {

		private final String refName;

		private final ObjectId commitId;
		
		private final int commitTime;

		public CollectingWork(int priority, ObjectId commitId, int commitTime, String refName) {
			super(priority);
			this.commitId = commitId;
			this.commitTime = commitTime;
			this.refName = refName;
		}

		public ObjectId getCommitId() {
			return commitId;
		}

		public int getCommitTime() {
			return commitTime;
		}

		public String getRefName() {
			return refName;
		}

	}

	static class CommitTimeComparator implements Comparator<CollectingWork> {

		@Override
		public int compare(CollectingWork o1, CollectingWork o2) {
			return o1.getCommitTime() - o2.getCommitTime();
		}

	}

	@Sessional
	@Override
	public void cloneInfo(Long sourceProjectId, Long targetProjectId) {
		String sourceActiveServer = projectService.getActiveServer(sourceProjectId, true);
		if (sourceActiveServer.equals(clusterService.getLocalServerAddress())) {
			export(sourceProjectId, getEnvDir(targetProjectId.toString()));
		} else {
			Client client = ClientBuilder.newClient();
			try {
				String serverUrl = clusterService.getServerUrl(sourceActiveServer);
				WebTarget target = client.target(serverUrl)
						.path("~api/cluster/commit-info")
						.queryParam("projectId", sourceProjectId);
				Invocation.Builder builder = target.request();
				builder.header(HttpHeaders.AUTHORIZATION,
						KubernetesHelper.BEARER + " " + clusterService.getCredential());
				try (Response response = builder.get()) {
					KubernetesHelper.checkStatus(response);
					try (var is = response.readEntity(InputStream.class)) {
						TarUtils.untar(is, getEnvDir(targetProjectId.toString()), false);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			} finally {
				client.close();
			}
		}
	}

	@Override
	public void export(Long projectId, File targetDir) {
		export(projectId.toString(), targetDir);
	}

	@Override
	protected File getEnvDir(String envKey) {
		File infoDir = new File(projectService.getInfoDir(Long.valueOf(envKey)), INFO_DIR);
		FileUtils.createDir(infoDir);
		return infoDir;
	}

	@Sessional
	@Override
	public Collection<String> getHistoryPaths(Long projectId, String path) {
		return projectService.runOnActiveServer(projectId, new ClusterTask<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Collection<String> call() {
				Environment env = getEnv(projectId.toString());
				Store historyPathsStore = getStore(env, HISTORY_PATHS_STORE);
				Store pathToIndexStore = getStore(env, PATH_TO_INDEX_STORE);
				Store indexToPathStore = getStore(env, INDEX_TO_PATH_STORE);

				return env.computeInReadonlyTransaction(new TransactionalComputable<>() {

					private Collection<String> getPaths(Transaction txn, Set<Integer> pathIndexes) {
						Set<String> paths = new HashSet<>();
						for (int pathIndex : pathIndexes) {
							byte[] pathBytes = readBytes(indexToPathStore, txn, new IntByteIterable(pathIndex));
							if (pathBytes != null)
								paths.add(new String(pathBytes, StandardCharsets.UTF_8));
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
								for (int eachPathIndex : pathIndexes) {
									byte[] bytesOfHistoryPaths =
											readBytes(historyPathsStore, txn, new IntByteIterable(eachPathIndex));
									if (bytesOfHistoryPaths != null) {
										int pos = 0;
										for (int i = 0; i < bytesOfHistoryPaths.length / Integer.BYTES; i++) {
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

		});

	}

	@Sessional
	@Override
	public Map<Integer, GitContribution> getOverallContributions(Long projectId) {
		return projectService.runOnActiveServer(projectId, new ClusterTask<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Map<Integer, GitContribution> call() {
				Environment env = getEnv(projectId.toString());
				Store store = getStore(env, DEFAULT_STORE);

				return env.computeInReadonlyTransaction(txn -> {
					Map<Integer, GitContribution> overallContributions = new HashMap<>();
					for (Map.Entry<Integer, GitContribution> entry :
							deserializeDailyContributions(readBytes(store, txn, OVERALL_CONTRIBUTIONS_KEY)).entrySet()) {
						overallContributions.put(entry.getKey(), entry.getValue());
					}
					return overallContributions;
				});
			}

		});

	}

	@Sessional
	@Override
	public List<GitContributor> getTopContributors(Long projectId, int top,
												   GitContribution.Type type, int fromDay, int toDay) {
		return projectService.runOnActiveServer(projectId, new ClusterTask<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<GitContributor> call() {
				Environment env = getEnv(projectId.toString());
				Store defaultStore = getStore(env, DEFAULT_STORE);
				Store indexToUserStore = getStore(env, INDEX_TO_USER_STORE);
				Store dailyContributionsStore = getStore(env, DAILY_CONTRIBUTIONS_STORE);

				return env.computeInReadonlyTransaction(new TransactionalComputable<List<GitContributor>>() {

					@Nullable
					private NameAndEmail getUser(Transaction txn, Map<Integer, Optional<NameAndEmail>> users,
												 Integer userIndex) {
						Optional<NameAndEmail> userOpt = users.get(userIndex);
						if (userOpt == null) {
							byte[] userBytes = readBytes(indexToUserStore, txn, new IntByteIterable(userIndex));
							if (userBytes != null) {
								NameAndEmail user = SerializationUtils.deserialize(userBytes);
								EmailAddressFacade emailAddress = emailAddressService.findFacadeByValue(user.getEmailAddress());
								if (emailAddress != null && emailAddress.isVerified()) {
									Long ownerId = emailAddress.getOwnerId();
									UserFacade owner = userService.findFacadeById(ownerId);
									if (owner != null) {
										EmailAddressFacade primaryEmailAddress = emailAddressService.findPrimaryFacade(ownerId);
										if (primaryEmailAddress != null && primaryEmailAddress.isVerified())
											user = new NameAndEmail(owner.getDisplayName(), primaryEmailAddress.getValue());
										else
											user = new NameAndEmail(owner.getDisplayName(), emailAddress.getValue());
									} else {
										userOpt = Optional.empty();
									}
								}
								userOpt = Optional.of(user);
							} else {
								userOpt = Optional.empty();
							}
							users.put(userIndex, userOpt);
						}
						return userOpt.orElse(null);
					}

					@Override
					public List<GitContributor> compute(Transaction txn) {
						Map<Integer, GitContribution> overallContributions =
								deserializeDailyContributions(readBytes(defaultStore, txn, OVERALL_CONTRIBUTIONS_KEY));
						Map<NameAndEmail, GitContribution> totalContributions = new HashMap<>();
						Map<Integer, Optional<NameAndEmail>> users = new HashMap<>();
						Map<Integer, Map<Integer, GitContribution>> contribsByDay = new LinkedHashMap<>();

						for (int dayValue : overallContributions.keySet()) {
							if (dayValue >= fromDay && dayValue <= toDay) {
								ByteIterable dayKey = new IntByteIterable(dayValue);
								Map<Integer, GitContribution> contribsOnDay =
										deserializeDailyContributions(readBytes(dailyContributionsStore, txn, dayKey));
								contribsByDay.put(dayValue, contribsOnDay);
								for (Map.Entry<Integer, GitContribution> entry : contribsOnDay.entrySet()) {
									NameAndEmail user = getUser(txn, users, entry.getKey());
									if (user != null) {
										GitContribution totalContrib = totalContributions.get(user);
										if (totalContrib == null) {
											totalContrib = entry.getValue();
										} else {
											totalContrib = new GitContribution(
													totalContrib.getCommits() + entry.getValue().getCommits(),
													totalContrib.getAdditions() + entry.getValue().getAdditions(),
													totalContrib.getDeletions() + entry.getValue().getDeletions());
										}
										totalContributions.put(user, totalContrib);
									}
								}
							}
						}

						List<NameAndEmail> topUsers = new ArrayList<>(totalContributions.keySet());
						topUsers.sort((o1, o2) -> {
							if (type == GitContribution.Type.COMMITS)
								return totalContributions.get(o2).getCommits() - totalContributions.get(o1).getCommits();
							else if (type == GitContribution.Type.ADDITIONS)
								return totalContributions.get(o2).getAdditions() - totalContributions.get(o1).getAdditions();
							else
								return totalContributions.get(o2).getDeletions() - totalContributions.get(o1).getDeletions();
						});

						if (top < topUsers.size())
							topUsers = topUsers.subList(0, top);

						Set<NameAndEmail> topUserSet = new HashSet<>(topUsers);

						Map<NameAndEmail, Map<Integer, Integer>> userContributions = new HashMap<>();

						for (Map.Entry<Integer, Map<Integer, GitContribution>> dayEntry : contribsByDay.entrySet()) {
							for (Map.Entry<Integer, GitContribution> userEntry : dayEntry.getValue().entrySet()) {
								NameAndEmail user = getUser(txn, users, userEntry.getKey());
								if (user != null && topUserSet.contains(user)) {
									Map<Integer, Integer> contribsOfUser = userContributions.get(user);
									if (contribsOfUser == null) {
										contribsOfUser = new HashMap<>();
										userContributions.put(user, contribsOfUser);
									}
									if (type == GitContribution.Type.COMMITS)
										contribsOfUser.put(dayEntry.getKey(), userEntry.getValue().getCommits());
									else if (type == GitContribution.Type.ADDITIONS)
										contribsOfUser.put(dayEntry.getKey(), userEntry.getValue().getAdditions());
									else
										contribsOfUser.put(dayEntry.getKey(), userEntry.getValue().getDeletions());
								}
							}
						}

						List<GitContributor> topContributors = new ArrayList<>();

						for (NameAndEmail user : topUsers) {
							topContributors.add(new GitContributor(user.asPersonIdent(),
									totalContributions.get(user), userContributions.get(user)));
						}

						return topContributors;
					}

				});
			}

		});

	}

	@Sessional
	@Override
	public Map<Long, Map<ObjectId, Long>> getUserCommits(User user, Date fromDate, Date toDate) {		
		var emailAddresses = user.getEmailAddresses().stream()
				.filter(it -> it.isVerified())
				.map(it -> it.getValue())
				.collect(toSet());
						
		var userCommits = new HashMap<Long, Map<ObjectId, Long>>();
		Map<String, Map<Long, Map<ObjectId, Long>>> result = clusterService.runOnAllServers(new ClusterTask<>() {

			@Override
			public Map<Long, Map<ObjectId, Long>> call() {
				var localServer = clusterService.getLocalServerAddress();
				var userCommits = new HashMap<Long, Map<ObjectId, Long>>();
				for (var entry: usersCache.entrySet()) {
					var projectId = entry.getKey();					
					if (localServer.equals(projectService.getActiveServer(projectId, false)) 
							&& emailAddresses.stream().anyMatch(entry.getValue().getRight()::contains)) {
						var project = projectService.findFacade(projectId);
						if (project != null && project.isCodeManagement() && project.getForkedFromId() == null) {
							Environment env = getEnv(projectId.toString());
							Store userCommitsStore = getStore(env, USER_COMMITS_STORE);

							userCommits.put(projectId, env.computeInReadonlyTransaction(new TransactionalComputable<Map<ObjectId, Long>>() {

								@Override
								public Map<ObjectId, Long> compute(Transaction txn) {
									var userCommits = new HashMap<ObjectId, Long>();
									for (var emailAddress : emailAddresses) {
										deserializeUserCommits(
												readBytes(userCommitsStore, txn, new StringByteIterable(emailAddress)))
												.entrySet().forEach(it -> {
													if (it.getValue() >= fromDate.getTime()
															&& it.getValue() <= toDate.getTime())
														userCommits.put(it.getKey(), it.getValue());
												});
									}
									return userCommits;
								}

							}));
						}
					}
				}
				return userCommits;
			}

		});
		for (var entry: result.entrySet()) 
			userCommits.putAll(entry.getValue());
		return userCommits;
	}	

	private Map<Integer, GitContribution> deserializeDailyContributions(byte[] bytes) {
		if (bytes != null) {
			Map<Integer, GitContribution> contributions = new HashMap<>();
			int pos = 0;
			for (int i = 0; i < bytes.length / 4 / Integer.BYTES; i++) {
				int key = ByteBuffer.wrap(bytes, pos, Integer.BYTES).getInt();
				pos += Integer.BYTES;
				int commits = ByteBuffer.wrap(bytes, pos, Integer.BYTES).getInt();
				pos += Integer.BYTES;
				int additions = ByteBuffer.wrap(bytes, pos, Integer.BYTES).getInt();
				pos += Integer.BYTES;
				int deletions = ByteBuffer.wrap(bytes, pos, Integer.BYTES).getInt();
				pos += Integer.BYTES;
				contributions.put(key, new GitContribution(commits, additions, deletions));
			}
			return contributions;
		} else {
			return new HashMap<>();
		}
	}

	private Map<ObjectId, Long> deserializeUserCommits(byte[] bytes) {
		if (bytes != null) {
			Map<ObjectId, Long> userCommits = new HashMap<>();
			int pos = 0;
			for (int i = 0; i < bytes.length / (20 + Long.BYTES); i++) {
				ObjectId key = ObjectId.fromRaw(bytes, pos);
				pos += 20;
				long value = ByteBuffer.wrap(bytes, pos, Long.BYTES).getLong();
				pos += Long.BYTES;
				userCommits.put(key, value);
			}
			return userCommits;
		} else {
			return new HashMap<>();
		}
	}

	private byte[] serializeDailyContributions(Map<Integer, GitContribution> dailyContributions) {
		byte[] bytes = new byte[dailyContributions.size() * Integer.BYTES * 4];
		int pos = 0;
		for (Map.Entry<Integer, GitContribution> entry : dailyContributions.entrySet()) {
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

	private byte[] serializeUserCommits(Map<ObjectId, Long> userCommits) {
		byte[] bytes = new byte[userCommits.size() * (20 + Long.BYTES)];
		int pos = 0;
		for (Map.Entry<ObjectId, Long> entry : userCommits.entrySet()) {
			entry.getKey().copyRawTo(bytes, pos);
			pos += 20;
			byte[] valueBytes = ByteBuffer.allocate(Long.BYTES).putLong(entry.getValue()).array();
			System.arraycopy(valueBytes, 0, bytes, pos, Long.BYTES);
			pos += Long.BYTES;
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

	@Override
	public Collection<ObjectId> getFixCommits(Long projectId, Long issueId, boolean headOnly) {
		return projectService.runOnActiveServer(projectId, new ClusterTask<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Collection<ObjectId> call() {
				Environment env = getEnv(projectId.toString());
				Store store = getStore(env, FIX_COMMITS_STORE);

				return env.computeInTransaction(txn -> {
					var repository = OneDev.getInstance(ProjectService.class).getRepository(projectId);
					var refCommitIds = new HashSet<>();
					for (var ref: GitUtils.getCommitRefs(repository, Constants.R_HEADS)) 
						refCommitIds.add(ref.getPeeledObj().getId());
					for (var ref: GitUtils.getCommitRefs(repository, Constants.R_TAGS)) 
						refCommitIds.add(ref.getPeeledObj().getId());
					var fixCommitIds = new ArrayList<ObjectId>();
					ByteIterable key;
					if (headOnly)
						key = new StringByteIterable(issueId + ".head");
					else  
						key = new LongByteIterable(issueId);
					for (var commitId: readCommits(store, txn, key)) {
						if (refCommitIds.contains(commitId)) {
							fixCommitIds.add(commitId);
						} else {
							var descendants = getDescendants(projectId, Sets.newHashSet(commitId));		
							descendants.retainAll(refCommitIds);
							if (!descendants.isEmpty())
								fixCommitIds.add(commitId);
						}
					}
					return fixCommitIds;
				});
			}

		});

	}

	@Override
	public List<Long> sortUsersByContribution(Map<Long, Collection<EmailAddressFacade>> userEmails,
											  Long projectId, Collection<String> files) {
		return projectService.runOnActiveServer(projectId, new ClusterTask<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<Long> call() {
				if (userEmails.size() <= 1)
					return new ArrayList<>(userEmails.keySet());

				Map<Long, Long> commitCounts = new HashMap<>();
				for (var userId : userEmails.keySet())
					commitCounts.put(userId, 0L);

				int count = 0;
				for (String path : files) {
					int addedCommitCount = addCommitCounts(projectId, commitCounts, userEmails, path);
					while (addedCommitCount == 0) {
						if (path.contains("/")) {
							path = StringUtils.substringBeforeLast(path, "/");
							addedCommitCount = addCommitCounts(projectId, commitCounts, userEmails, path);
						} else {
							addCommitCounts(projectId, commitCounts, userEmails, "");
							break;
						}
					}
					if (++count >= MAX_COMMIT_FILES)
						break;
				}

				var userIds = new ArrayList<>(commitCounts.keySet());
				userIds.sort((o1, o2) -> {
					if (commitCounts.get(o1) < commitCounts.get(o2))
						return 1;
					else
						return -1;
				});
				return userIds;
			}

		});

	}

	private int addCommitCounts(Long projectId, Map<Long, Long> commitCounts,
								Map<Long, Collection<EmailAddressFacade>> userEmails, String path) {
		int addedCommitCount = 0;
		for (var entry : commitCounts.entrySet()) {
			var userId = entry.getKey();
			int commitCount = getCommitCount(projectId, userEmails.get(userId), path);
			entry.setValue(entry.getValue() + commitCount);
			addedCommitCount += commitCount;
		}
		return addedCommitCount;
	}

	private static class NextIndex {
		int user;

		int email;

		int path;
	}

	private static interface CommitRangeProcessor {

		void process(ObjectId untilCommitId, @Nullable ObjectId sinceCommitId);

	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(CommitInfoService.class);
	}

}
