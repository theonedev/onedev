package io.onedev.server.ee.xsearch;

import io.onedev.commons.jsymbol.Symbol;
import io.onedev.commons.jsymbol.SymbolExtractor;
import io.onedev.commons.jsymbol.SymbolExtractorRegistry;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.DefaultBranchChanged;
import io.onedev.server.event.project.ProjectDeleted;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.search.code.NGramAnalyzer;
import io.onedev.server.util.ContentDetector;
import io.onedev.server.util.WriterRunnable;
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
import org.eclipse.jgit.lib.Repository;
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
import java.io.Serializable;
import java.util.List;

import static io.onedev.server.search.code.FieldConstants.*;
import static io.onedev.server.search.code.IndexConstants.MAX_INDEXABLE_SIZE;
import static io.onedev.server.search.code.IndexConstants.NGRAM_SIZE;
import static java.lang.Integer.MAX_VALUE;
import static org.apache.lucene.document.LongPoint.newExactQuery;
import static org.apache.lucene.search.BooleanClause.Occur.MUST;

@Singleton
public class DefaultCodeIndexManager implements CodeIndexManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultCodeIndexManager.class);

	private static final int INDEXING_PRIORITY = 100;

	private static final int DATA_VERSION = 1;
	
	private final ProjectManager projectManager;
	
	private final SessionManager sessionManager;
	
	private final ClusterManager clusterManager;
	
	private final CodeSearchManager searchManager;
	
	private final BatchWorkManager batchWorkManager;

	@Inject 
	public DefaultCodeIndexManager(ProjectManager projectManager, SessionManager sessionManager, 
								   ClusterManager clusterManager, BatchWorkManager batchWorkManager, 
								   CodeSearchManager searchManager) {
		this.projectManager = projectManager;
		this.sessionManager = sessionManager;
		this.clusterManager = clusterManager;
		this.batchWorkManager = batchWorkManager;
		this.searchManager = searchManager;
	}
	
	@Sessional
	@Listen
	public void on(RefUpdated event) {
		var project = event.getProject();
		var branchName = GitUtils.ref2branch(event.getRefName());
		if (branchName != null && branchName.equals(project.getDefaultBranch())
				&& !event.getNewCommitId().equals(ObjectId.zeroId())) {
			batchWorkManager.submit(
					getBatchWorker(project.getId()),
					new IndexWork(INDEXING_PRIORITY));
		}
	}

	@Sessional
	@Listen
	public void on(DefaultBranchChanged event) {
		batchWorkManager.submit(
				getBatchWorker(event.getProject().getId()), 
				new IndexWork(INDEXING_PRIORITY));		
	}
	
	@Sessional
	@Listen
	public void on(SystemStarted event) {
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

		for (var projectId: projectManager.getActiveIds()) {
			batchWorkManager.submit(
					getBatchWorker(projectId),
					new IndexWork(INDEXING_PRIORITY));
		}
	}

	@Sessional
	@Listen
	public void on(ProjectDeleted event) {
		Long projectId = event.getProjectId();
		clusterManager.submitToAllServers(() -> {
			doWithWriter(writer -> writer.deleteDocuments(newExactQuery(PROJECT_ID.name(), projectId)));
			return null;
		});
	}
	
	private BatchWorker getBatchWorker(Long projectId) {
		return new BatchWorker("project-" + projectId + "-indexDefaultBranch", MAX_VALUE) {

			@Override
			public void doWorks(List<Prioritized> works) {
				sessionManager.run(() -> {
					var project = projectManager.load(projectId);
					var defaultBranch = project.getDefaultBranch();
					if (defaultBranch != null) {
						logger.debug("Indexing default branch of project {}", project.getPath());
						try (Directory directory = FSDirectory.open(getIndexDir().toPath())) {
							if (DirectoryReader.indexExists(directory)) {
								try (IndexReader reader = DirectoryReader.open(directory)) {
									IndexSearcher searcher = new IndexSearcher(reader);
									doIndex(project, directory, searcher);
								}
							} else {
								doIndex(project, directory, null);
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				});
			}

		};
	}

	private File getIndexDir() {
		return new File(OneDev.getIndexDir(), "code");
	}

	private String getIndexVersion(String filePatterns) {
		return DigestUtils.md5Hex(DATA_VERSION + ";" + SymbolExtractorRegistry.getVersion() + ";" + filePatterns);
	}

	private synchronized void doWithWriter(WriterRunnable runnable) {
		File indexDir = getIndexDir();
		try (Directory directory = FSDirectory.open(indexDir.toPath());) {
			var writerConfig = new IndexWriterConfig(new NGramAnalyzer(NGRAM_SIZE, NGRAM_SIZE));
			writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			try (IndexWriter writer = new IndexWriter(directory, writerConfig)) {
				try {
					runnable.run(writer);
					writer.commit();
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

	private void doIndex(Project project, Directory directory, @Nullable IndexSearcher searcher) {
		var defaultBranch = project.getDefaultBranch();
		if (defaultBranch != null) {
			logger.debug("Indexing default branch (project: {})", project.getPath());
			doWithWriter(writer -> {
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
							var queryBuilder = new BooleanQueryBuilder();
							queryBuilder = new BooleanQueryBuilder();
							queryBuilder.add(META.getTermQuery(BLOB.name()), MUST);
							queryBuilder.add(newExactQuery(PROJECT_ID.name(), project.getId()), MUST);
							queryBuilder.add(BLOB_PATH.getTermQuery(blobPath), MUST);
							var query = queryBuilder.build();
							if (treeWalk.getTreeCount() != 1)
								writer.deleteDocuments(query);
							if ((treeWalk.getRawMode(0) & FileMode.TYPE_MASK) == FileMode.TYPE_FILE) {
								ObjectId blobId = treeWalk.getObjectId(0);
								indexBlob(writer, project.getId(), repository, blobId, blobPath);
							}
						}
					}

					// record last commit so that we only need to indexing changed files for subsequent commits
					Document document = new Document();
					document.add(new StringField(META.name(), LAST_COMMIT.name(), Field.Store.NO));
					document.add(new LongPoint(PROJECT_ID.name(), project.getId()));
					document.add(new StoredField(LAST_COMMIT_INDEX_VERSION.name(), indexVersion));
					document.add(new StoredField(LAST_COMMIT_HASH.name(), commitId.getName()));
					writer.updateDocument(META.getTerm(LAST_COMMIT.name()), document);
				}
			});
		}
	}

	private void indexBlob(IndexWriter writer, Long projectId, Repository repository, 
						   ObjectId blobId, String blobPath) throws IOException {
		Document document = new Document();

		document.add(new LongPoint(PROJECT_ID.name(), projectId));
		document.add(new StoredField(PROJECT_ID.name(), projectId));
		document.add(new StringField(BLOB_PATH.name(), blobPath, Field.Store.YES));
		String blobName = blobPath;
		if (blobPath.indexOf('/') != -1)
			blobName = StringUtils.substringAfterLast(blobPath, "/");

		document.add(new StringField(BLOB_NAME.name(), blobName.toLowerCase(), 
				Field.Store.NO));
		document.add(new StoredField(BLOB_HASH.name(), blobId.name()));

		ObjectLoader objectLoader = repository.open(blobId);
		if (objectLoader.getSize() <= MAX_INDEXABLE_SIZE) {
			byte[] bytes = objectLoader.getCachedBytes();
			String content = ContentDetector.convertToText(bytes, blobName);
			if (content != null) {
				document.add(new TextField(BLOB_TEXT.name(), content, Field.Store.NO));
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
	
	private static class IndexWork extends Prioritized {

		public IndexWork(int priority) {
			super(priority);
		}

	}
	
}
