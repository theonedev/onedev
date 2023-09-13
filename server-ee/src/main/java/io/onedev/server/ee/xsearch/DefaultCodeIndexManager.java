package io.onedev.server.ee.xsearch;

import com.google.common.base.Splitter;
import io.onedev.commons.jsymbol.Symbol;
import io.onedev.commons.jsymbol.SymbolExtractor;
import io.onedev.commons.jsymbol.SymbolExtractorRegistry;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.project.ActiveServerChanged;
import io.onedev.server.event.project.DefaultBranchChanged;
import io.onedev.server.event.project.ProjectDeleted;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.Setting;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.search.code.NGramAnalyzer;
import io.onedev.server.util.ContentDetector;
import io.onedev.server.util.WriterCallable;
import io.onedev.server.util.concurrent.BatchWorkManager;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;
import io.onedev.server.util.lucene.BooleanQueryBuilder;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.onedev.server.git.GitUtils.ref2branch;
import static io.onedev.server.search.code.FieldConstants.*;
import static io.onedev.server.search.code.IndexConstants.*;
import static java.lang.Integer.MAX_VALUE;
import static org.apache.lucene.document.LongPoint.newExactQuery;
import static org.apache.lucene.search.BooleanClause.Occur.MUST;

@Singleton
public class DefaultCodeIndexManager implements CodeIndexManager, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultCodeIndexManager.class);

	private static final int INDEXING_PRIORITY = 50;

	private static final int DATA_VERSION = 1;
	
	private final ProjectManager projectManager;
	
	private final SessionManager sessionManager;
	
	private final ClusterManager clusterManager;
	
	private final CodeSearchManager searchManager;
	
	private final BatchWorkManager batchWorkManager;
	
	private final SubscriptionManager subscriptionManager;
	
	private final TransactionManager transactionManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final AtomicInteger indexings = new AtomicInteger(0);

	@Inject 
	public DefaultCodeIndexManager(ProjectManager projectManager, SessionManager sessionManager, 
								   ClusterManager clusterManager, BatchWorkManager batchWorkManager, 
								   CodeSearchManager searchManager, SubscriptionManager subscriptionManager, 
								   TransactionManager transactionManager, ListenerRegistry listenerRegistry) {
		this.projectManager = projectManager;
		this.sessionManager = sessionManager;
		this.clusterManager = clusterManager;
		this.batchWorkManager = batchWorkManager;
		this.searchManager = searchManager;
		this.subscriptionManager = subscriptionManager;
		this.transactionManager = transactionManager;
		this.listenerRegistry = listenerRegistry;
	}
	
	@Sessional
	@Listen
	public void on(RefUpdated event) {
		var project = event.getProject();
		var branchName = ref2branch(event.getRefName());
		if (branchName != null && branchName.equals(project.getDefaultBranch())
				&& !event.getNewCommitId().equals(ObjectId.zeroId())) {
			batchWorkManager.submit(getBatchWorker(), new IndexWork(INDEXING_PRIORITY, project.getId()));
		}
	}

	@Sessional
	@Listen
	public void on(DefaultBranchChanged event) {
		batchWorkManager.submit(getBatchWorker(), new IndexWork(INDEXING_PRIORITY, event.getProject().getId()));
	}
	
	@Listen
	public void on(SystemStarting event) {
		File indexDir = getIndexDir();
		FileUtils.createDir(indexDir);
		try (Directory directory = FSDirectory.open(indexDir.toPath())) {
			if (DirectoryReader.indexExists(directory)) {
				try (IndexReader reader = DirectoryReader.open(directory)) {
				} catch (IndexFormatTooOldException e) {
					FileUtils.cleanDir(indexDir);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Listen
	public void on(SystemStarted event) {
		for (var projectId : projectManager.getActiveIds()) {
			batchWorkManager.submit(getBatchWorker(), new IndexWork(INDEXING_PRIORITY, projectId));
		}
	}
	
	@Listen
	public void on(ActiveServerChanged event) {
		for (var projectId : event.getProjectIds()) {
			batchWorkManager.submit(getBatchWorker(), new IndexWork(INDEXING_PRIORITY, projectId));
		}
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Setting) {
			Setting setting = (Setting) event.getEntity();
			if (setting.getKey() == Setting.Key.SUBSCRIPTION_DATA) {
				transactionManager.runAfterCommit(() -> {
					clusterManager.submitToAllServers(() -> {
						for (var projectId : projectManager.getActiveIds()) {
							batchWorkManager.submit(getBatchWorker(), new IndexWork(INDEXING_PRIORITY, projectId));
						}
						return null;
					});
				});
			}
		}
	}	

	@Listen
	public void on(ProjectDeleted event) {
		Long projectId = event.getProjectId();
		clusterManager.submitToAllServers(() -> {
			callWithWriter(writer -> writer.deleteDocuments(newExactQuery(PROJECT_ID.name(), projectId)));
			return null;
		});
	}
	
	private BatchWorker getBatchWorker() {
		return new BatchWorker("indexDefaultBranch", MAX_VALUE) {

			@Override
			public void doWorks(List<Prioritized> works) {
				if (subscriptionManager.isSubscriptionActive()) {
					callWithWriter(writer -> {
						sessionManager.run(() -> {
							boolean indexStatusChanged = indexings.getAndIncrement() == 0;
							try {
								if (indexStatusChanged)
									listenerRegistry.post(new CodeIndexStatusChanged());
								Collection<Long> projectIds = new HashSet<>();
								for (var work: works) 
									projectIds.add(((IndexWork) work).projectId);
								for (var projectId: projectIds) {
									var project = projectManager.load(projectId);
									try (Directory directory = FSDirectory.open(getIndexDir().toPath())) {
										if (DirectoryReader.indexExists(directory)) {
											try (IndexReader reader = DirectoryReader.open(directory)) {
												IndexSearcher searcher = new IndexSearcher(reader);
												doIndex(project, writer, searcher);
											}
										} else {
											doIndex(project, writer, null);
										}
									} catch (IOException e) {
										throw new RuntimeException(e);
									}
								}
							} finally {
								if (indexings.decrementAndGet() == 0)
									listenerRegistry.post(new CodeIndexStatusChanged());
							}
						});
						return null;
					});
				}
			}

		};
	}

	private File getIndexDir() {
		return new File(OneDev.getIndexDir(), "code");
	}

	private String getIndexVersion(String filePatterns) {
		return DigestUtils.md5Hex(DATA_VERSION + ";" + SymbolExtractorRegistry.getVersion() + ";" + filePatterns);
	}

	private <T> T callWithWriter(WriterCallable<T> callable) {
		File indexDir = getIndexDir();
		try (Directory directory = FSDirectory.open(indexDir.toPath());) {
			var writerConfig = new IndexWriterConfig(new NGramAnalyzer(NGRAM_SIZE, NGRAM_SIZE));
			writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			try (IndexWriter writer = new IndexWriter(directory, writerConfig)) {
				try {
					var result = callable.call(writer);
					writer.commit();
					return result;
				} catch (Exception e) {
					writer.rollback();
					throw ExceptionUtils.unchecked(e);
				} finally {
					searchManager.indexUpdated();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void doIndex(Project project, IndexWriter writer, @Nullable IndexSearcher searcher) throws IOException {
		var defaultBranch = project.getDefaultBranch();
		if (defaultBranch != null) {
			logger.debug("Indexing default branch (project: {})...", project.getPath());
			var repository = projectManager.getRepository(project.getId());
			try (RevWalk revWalk = new RevWalk(repository);
				 TreeWalk treeWalk = new TreeWalk(repository)) {
				var commitId = repository.resolve(GitUtils.branch2ref(defaultBranch));
				treeWalk.addTree(revWalk.parseCommit(commitId).getTree());
				treeWalk.setRecursive(true);
				var codeAnalysisPatterns = project.findCodeAnalysisPatterns();
				var indexVersion = getIndexVersion(codeAnalysisPatterns);
				if (searcher != null) {
					var queryBuilder = new BooleanQueryBuilder();
					queryBuilder.add(META.getTermQuery(LAST_COMMIT.name()), MUST);
					queryBuilder.add(newExactQuery(PROJECT_ID.name(), project.getId()), MUST);
					var topDocs = searcher.search(queryBuilder.build(), 1);
					if (topDocs.scoreDocs.length != 0) {
						var doc = searcher.doc(topDocs.scoreDocs[0].doc);
						String lastCommitIndexVersion = doc.get(LAST_COMMIT_INDEX_VERSION.name());
						queryBuilder = new BooleanQueryBuilder();
						queryBuilder.add(META.getTermQuery(BLOB.name()), MUST);
						queryBuilder.add(newExactQuery(PROJECT_ID.name(), project.getId()), MUST);
						var query = queryBuilder.build();
						if (lastCommitIndexVersion.equals(indexVersion)) {
							String lastCommitHash = doc.get(LAST_COMMIT_HASH.name());
							ObjectId lastCommitId = ObjectId.fromString(lastCommitHash);
							if (repository.getObjectDatabase().has(lastCommitId)) {
								treeWalk.addTree(revWalk.parseCommit(lastCommitId).getTree());
								treeWalk.setFilter(TreeFilter.ANY_DIFF);
							} else {
								writer.deleteDocuments(query);
							}
						} else {
							writer.deleteDocuments(query);
						}
					}
				}

				var patternSet = PatternSet.parse(codeAnalysisPatterns);
				Matcher matcher = new PathMatcher();
				while (treeWalk.next()) {
					var blobPath = treeWalk.getPathString();
					if (patternSet.matches(matcher, blobPath)) {
						if (treeWalk.getTreeCount() != 1) {
							var queryBuilder = new BooleanQueryBuilder();
							queryBuilder = new BooleanQueryBuilder();
							queryBuilder.add(META.getTermQuery(BLOB.name()), MUST);
							queryBuilder.add(newExactQuery(PROJECT_ID.name(), project.getId()), MUST);
							queryBuilder.add(BLOB_PATH.getTermQuery(blobPath), MUST);
							writer.deleteDocuments(queryBuilder.build());
						}
						
						if ((treeWalk.getRawMode(0) & FileMode.TYPE_MASK) == FileMode.TYPE_FILE) {
							ObjectId blobId = treeWalk.getObjectId(0);
							Document document = new Document();

							document.add(new StringField(META.name(), BLOB.name(), Field.Store.NO));
							document.add(new LongPoint(PROJECT_ID.name(), project.getId()));
							document.add(new StoredField(PROJECT_ID.name(), project.getId()));
							document.add(new StringField(BLOB_PATH.name(), blobPath, Field.Store.YES));
							String blobName = GitUtils.getBlobName(blobPath);

							document.add(new StringField(BLOB_NAME.name(), blobName.toLowerCase(),
									Field.Store.NO));
							document.add(new StoredField(BLOB_HASH.name(), blobId.name()));

							ObjectLoader objectLoader = repository.open(blobId);
							if (objectLoader.getSize() <= MAX_INDEXABLE_BLOB_SIZE) {
								byte[] bytes = objectLoader.getCachedBytes();
								String content = ContentDetector.convertToText(bytes, blobName);
								if (content != null) {
									for (var line: Splitter.on('\n').split(content)) {
										if (line.length() <= MAX_INDEXABLE_LINE_LEN)
											document.add(new TextField(BLOB_TEXT.name(), line, Field.Store.NO));
									}
									SymbolExtractor<Symbol> extractor = SymbolExtractorRegistry.getExtractor(blobName);
									if (extractor != null) {
										List<Symbol> symbols = null;
										try {
											symbols = extractor.extract(blobName, StringUtils.removeBOM(content));
										} catch (Exception e) {
											logger.trace("Can not extract symbols from blob (hash:" + blobId.name() + ", path:" + blobPath + ")", e);
										}
										if (symbols != null) {
											for (Symbol symbol: symbols) {
												String fieldValue = symbol.getName();
												if (fieldValue != null && symbol.isSearchable()) {
													fieldValue = fieldValue.toLowerCase();

													String fieldName;
													if (symbol.isPrimary())
														fieldName = BLOB_PRIMARY_SYMBOLS.name();
													else
														fieldName = BLOB_SECONDARY_SYMBOLS.name();
													document.add(new StringField(fieldName, fieldValue, Field.Store.NO));
												}
											}
											byte[] bytesOfSymbols = SerializationUtils.serialize((Serializable) symbols);
											document.add(new StoredField(BLOB_SYMBOL_LIST.name(), bytesOfSymbols));
										}
									}
								} else {
									logger.debug("Ignore content of binary file '{}'.", blobPath);
								}
							} else {
								logger.debug("Ignore content of large file '{}'.", blobPath);
							}
							writer.addDocument(document);
						}
					}
				}

				// record last commit so that we only need to indexing changed files for subsequent commits
				var queryBuilder = new BooleanQueryBuilder();
				queryBuilder = new BooleanQueryBuilder();
				queryBuilder.add(META.getTermQuery(LAST_COMMIT.name()), MUST);
				queryBuilder.add(newExactQuery(PROJECT_ID.name(), project.getId()), MUST);
				writer.deleteDocuments(queryBuilder.build());

				Document document = new Document();
				document.add(new StringField(META.name(), LAST_COMMIT.name(), Field.Store.NO));
				document.add(new LongPoint(PROJECT_ID.name(), project.getId()));
				document.add(new StoredField(LAST_COMMIT_INDEX_VERSION.name(), indexVersion));
				document.add(new StoredField(LAST_COMMIT_HASH.name(), commitId.getName()));
				writer.addDocument(document);
			}
			logger.debug("Default branch indexed (project: {})", project.getPath());
		}
	}

	@Override
	public boolean isIndexing() {
		return indexings.get() != 0;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(CodeIndexManager.class);
	}
	
	private static class IndexWork extends Prioritized {

		private final Long projectId;
		
		public IndexWork(int priority, Long projectId) {
			super(priority);
			this.projectId = projectId;
		}

	}
	
}
