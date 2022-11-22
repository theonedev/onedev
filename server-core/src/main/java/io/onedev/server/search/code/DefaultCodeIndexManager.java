package io.onedev.server.search.code;

import static io.onedev.server.search.code.FieldConstants.BLOB_HASH;
import static io.onedev.server.search.code.FieldConstants.BLOB_INDEX_VERSION;
import static io.onedev.server.search.code.FieldConstants.BLOB_NAME;
import static io.onedev.server.search.code.FieldConstants.BLOB_PATH;
import static io.onedev.server.search.code.FieldConstants.BLOB_PRIMARY_SYMBOLS;
import static io.onedev.server.search.code.FieldConstants.BLOB_SECONDARY_SYMBOLS;
import static io.onedev.server.search.code.FieldConstants.BLOB_SYMBOL_LIST;
import static io.onedev.server.search.code.FieldConstants.BLOB_TEXT;
import static io.onedev.server.search.code.FieldConstants.COMMIT_HASH;
import static io.onedev.server.search.code.FieldConstants.COMMIT_INDEX_VERSION;
import static io.onedev.server.search.code.FieldConstants.LAST_COMMIT;
import static io.onedev.server.search.code.FieldConstants.LAST_COMMIT_HASH;
import static io.onedev.server.search.code.FieldConstants.LAST_COMMIT_INDEX_VERSION;
import static io.onedev.server.search.code.FieldConstants.META;
import static io.onedev.server.search.code.IndexConstants.MAX_INDEXABLE_SIZE;
import static io.onedev.server.search.code.IndexConstants.NGRAM_SIZE;

import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexFormatTooOldException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.jsymbol.Symbol;
import io.onedev.commons.jsymbol.SymbolExtractor;
import io.onedev.commons.jsymbol.SymbolExtractorRegistry;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.CommitIndexed;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.ContentDetector;
import io.onedev.server.util.IndexResult;
import io.onedev.server.util.concurrent.BatchWorkManager;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;

@Singleton
public class DefaultCodeIndexManager implements CodeIndexManager, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultCodeIndexManager.class);

	private static final int UI_INDEXING_PRIORITY = 10;
	
	private static final int BACKEND_INDEXING_PRIORITY = 50;
	
	private static final int DATA_VERSION = 6;
	
	private final StorageManager storageManager;
	
	private final BatchWorkManager batchWorkManager;
	
	private final SessionManager sessionManager;
	
	private final ProjectManager projectManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultCodeIndexManager(ListenerRegistry listenerRegistry, StorageManager storageManager, 
			BatchWorkManager batchWorkManager, SessionManager sessionManager, ProjectManager projectManager) {
		this.listenerRegistry = listenerRegistry;
		this.storageManager = storageManager;
		this.batchWorkManager = batchWorkManager;
		this.sessionManager = sessionManager;
		this.projectManager = projectManager;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(CodeIndexManager.class);
	}
	
	private String getCommitIndexVersion(final IndexSearcher searcher, AnyObjectId commitId) throws IOException {
		final AtomicReference<String> indexVersion = new AtomicReference<>(null);
		
		searcher.search(COMMIT_HASH.getTermQuery(commitId.getName()), new SimpleCollector() {

			private int docBase;
			
			@Override
			public void collect(int doc) throws IOException {
				indexVersion.set(searcher.doc(docBase+doc).get(COMMIT_INDEX_VERSION.name()));
			}

			@Override
			protected void doSetNextReader(LeafReaderContext context) throws IOException {
				docBase = context.docBase;
			}

			@Override
			public ScoreMode scoreMode() {
				return ScoreMode.COMPLETE_NO_SCORES;
			}

		});
		return indexVersion.get();
	}
	
	private IndexResult index(Repository repository, AnyObjectId commitId, IndexWriter writer, 
			IndexSearcher searcher, PatternSet filePatterns) throws Exception {
		try (	RevWalk revWalk = new RevWalk(repository); 
				TreeWalk treeWalk = new TreeWalk(repository)) {
			treeWalk.addTree(revWalk.parseCommit(commitId).getTree());
			treeWalk.setRecursive(true);
			
			if (searcher != null) {
				TopDocs topDocs = searcher.search(META.getTermQuery(LAST_COMMIT.name()), 1);
				if (topDocs.scoreDocs.length != 0) {
					Document doc = searcher.doc(topDocs.scoreDocs[0].doc);
					String lastCommitIndexVersion = doc.get(LAST_COMMIT_INDEX_VERSION.name());
					if (lastCommitIndexVersion.equals(getIndexVersion())) {
						String lastCommitHash = doc.get(LAST_COMMIT_HASH.name());
						ObjectId lastCommitId = ObjectId.fromString(lastCommitHash);
						if (repository.getObjectDatabase().has(lastCommitId)) { 
							treeWalk.addTree(revWalk.parseCommit(lastCommitId).getTree());
							treeWalk.setFilter(TreeFilter.ANY_DIFF);
						}
					}
				}
			}
	
			Matcher matcher = new PathMatcher();
			int indexed = 0;
			int checked = 0;
			while (treeWalk.next()) {
				if ((treeWalk.getRawMode(0) & FileMode.TYPE_MASK) == FileMode.TYPE_FILE 
						&& (treeWalk.getTreeCount() == 1 || !treeWalk.idEqual(0, 1))) {
					String blobPath = treeWalk.getPathString();
					
					if (filePatterns.matches(matcher, blobPath)) {
						ObjectId blobId = treeWalk.getObjectId(0);
						String blobName = treeWalk.getNameString();
						
						BooleanQuery.Builder builder = new BooleanQuery.Builder();
						builder.add(BLOB_HASH.getTermQuery(blobId.name()), Occur.MUST);
						builder.add(BLOB_PATH.getTermQuery(blobPath), Occur.MUST);
						BooleanQuery query = builder.build();
						
						final AtomicReference<String> blobIndexVersionRef = new AtomicReference<>(null);
						if (searcher != null) {
							searcher.search(query, new SimpleCollector() {
		
								private LeafReaderContext context;
		
								@Override
								public void collect(int doc) throws IOException {
									blobIndexVersionRef.set(searcher.doc(context.docBase+doc).get(BLOB_INDEX_VERSION.name()));
								}
		
								@Override
								protected void doSetNextReader(LeafReaderContext context) throws IOException {
									this.context = context;
								}
		
								@Override
								public ScoreMode scoreMode() {
									return ScoreMode.COMPLETE_NO_SCORES;
								}
								
							});
							checked++;
						}
		
						SymbolExtractor<Symbol> extractor = SymbolExtractorRegistry.getExtractor(blobName);
						String currentBlobIndexVersion = getIndexVersion(extractor);
						String blobIndexVersion = blobIndexVersionRef.get();
						if (blobIndexVersion != null) {
							if (!blobIndexVersion.equals(currentBlobIndexVersion)) {
								writer.deleteDocuments(query);
								indexBlob(writer, repository, extractor, blobId, blobPath);
								indexed++;
							}
						} else {
							indexBlob(writer, repository, extractor, blobId, blobPath);
							indexed++;
						}
					}
					
				}
			}
	
			// record current commit so that we know which commit has been indexed
			Document document = new Document();
			document.add(new StringField(COMMIT_HASH.name(), commitId.getName(), Store.NO));
			document.add(new StoredField(COMMIT_INDEX_VERSION.name(), getIndexVersion()));
			writer.updateDocument(COMMIT_HASH.getTerm(commitId.getName()), document);
			
			// record last commit so that we only need to indexing changed files for subsequent commits
			document = new Document();
			document.add(new StringField(META.name(), LAST_COMMIT.name(), Store.NO));
			document.add(new StoredField(LAST_COMMIT_INDEX_VERSION.name(), getIndexVersion()));
			document.add(new StoredField(LAST_COMMIT_HASH.name(), commitId.getName()));
			writer.updateDocument(META.getTerm(LAST_COMMIT.name()), document);
			
			return new IndexResult(checked, indexed);
		}
	}
	
	private void indexBlob(IndexWriter writer, Repository repository, 
			SymbolExtractor<Symbol> extractor, ObjectId blobId, String blobPath) throws IOException {
		Document document = new Document();
		
		document.add(new StoredField(BLOB_INDEX_VERSION.name(), getIndexVersion(extractor)));
		document.add(new StringField(BLOB_HASH.name(), blobId.name(), Store.NO));
		document.add(new StringField(BLOB_PATH.name(), blobPath, Store.NO));
		document.add(new BinaryDocValuesField(BLOB_PATH.name(), new BytesRef(blobPath.getBytes(StandardCharsets.UTF_8))));
		
		String blobName = blobPath;
		if (blobPath.indexOf('/') != -1) 
			blobName = StringUtils.substringAfterLast(blobPath, "/");
		
		document.add(new StringField(BLOB_NAME.name(), blobName.toLowerCase(), Store.NO));
		
		ObjectLoader objectLoader = repository.open(blobId);
		if (objectLoader.getSize() <= MAX_INDEXABLE_SIZE) {
			byte[] bytes = objectLoader.getCachedBytes();
			String content = ContentDetector.convertToText(bytes, blobName);
			if (content != null) {
				document.add(new TextField(BLOB_TEXT.name(), content, Store.NO));
				
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
								document.add(new StringField(fieldName, fieldValue, Store.NO));
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
	
	private BatchWorker getBatchWorker(Long projectId) {
		return new BatchWorker("project-" + projectId + "-indexBlob", 1) {

			@Override
			public void doWorks(Collection<Prioritized> works) {
				sessionManager.run(new Runnable() {

					@Override
					public void run() {
						Preconditions.checkState(works.size() == 1);

						Project project = projectManager.load(projectId);
						ObjectId commitId = ((IndexWork) works.iterator().next()).getCommitId();
						doIndex(project, commitId);
						
						listenerRegistry.post(new CommitIndexed(project, commitId.copy()));
					}
					
				});
			}
			
		};
	}

	private IndexResult doIndex(Project project, ObjectId commit, Directory directory, IndexSearcher searcher) {
		IndexWriterConfig writerConfig = new IndexWriterConfig(new NGramAnalyzer(NGRAM_SIZE, NGRAM_SIZE));
		writerConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		try (IndexWriter writer = new IndexWriter(directory, writerConfig)) {
			try {
				logger.debug("Indexing commit (project: {}, commit: {})", project.getPath(), commit.getName());
				IndexResult indexResult = index(projectManager.getRepository(project.getId()), 
						commit, writer, searcher, project.findCodeAnalysisPatterns());
				writer.commit();
				return indexResult;
			} catch (Exception e) {
				writer.rollback();
				throw ExceptionUtils.unchecked(e);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private IndexResult doIndex(Project project, ObjectId commit) {
		try (Directory directory = FSDirectory.open(storageManager.getProjectIndexDir(project.getId()).toPath())) {
			if (DirectoryReader.indexExists(directory)) {
				try (IndexReader reader = DirectoryReader.open(directory)) {
					IndexSearcher searcher = new IndexSearcher(reader);
					if (getIndexVersion().equals(getCommitIndexVersion(searcher, commit)))
						return new IndexResult(0, 0);
					else
						return doIndex(project, commit, directory, searcher);
				}
			} else {
				return doIndex(project, commit, directory, null);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getIndexVersion() {
		return DigestUtils.md5Hex(DATA_VERSION + ";" + SymbolExtractorRegistry.getVersion());
	}
	
	@Override
	public String getIndexVersion(SymbolExtractor<Symbol> extractor) {
		String version;
		if (extractor != null)
			version = DATA_VERSION + ";" + extractor.getClass().getName() + ":" + extractor.getVersion();
		else
			version = String.valueOf(DATA_VERSION);
		return DigestUtils.md5Hex(version);
	}

	@Override
	public boolean isIndexed(Long projectId, ObjectId commitId) {
		return projectManager.runOnProjectServer(projectId, new ClusterTask<Boolean>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Boolean call() throws Exception {
				File indexDir = storageManager.getProjectIndexDir(projectId);
				try (Directory directory = FSDirectory.open(indexDir.toPath())) {
					if (DirectoryReader.indexExists(directory)) {
						try (IndexReader reader = DirectoryReader.open(directory)) {
							IndexSearcher searcher = new IndexSearcher(reader);
							return getIndexVersion().equals(getCommitIndexVersion(searcher, commitId));
						}
					} else {
						return false;
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
		});
	}

	@Sessional
	@Listen
	public void on(RefUpdated event) {
		// only index branches at back end, tags will be indexed on demand from GUI 
		// as many tags might be pushed all at once when the repository is imported 
		if (event.getRefName().startsWith(Constants.R_HEADS) 
				&& !event.getNewCommitId().equals(ObjectId.zeroId())) {
			IndexWork work = new IndexWork(BACKEND_INDEXING_PRIORITY, event.getNewCommitId());
			batchWorkManager.submit(getBatchWorker(event.getProject().getId()), work);
		}
	}
	
	@Sessional
	@Listen
	public void on(SystemStarted event) {
		Collection<Long> projectIds = projectManager.getIds();
		for (File file: storageManager.getProjectsDir().listFiles()) {
			Long projectId = Long.valueOf(file.getName());
			if (projectIds.contains(projectId)) {
				File indexDir = storageManager.getProjectIndexDir(projectId);
				if (indexDir.exists()) {
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
			}
		}
	}
	
	@Override
	public void indexAsync(Long projectId, ObjectId commitId) {
		int priority;
		if (RequestCycle.get() != null)
			priority = UI_INDEXING_PRIORITY;
		else
			priority = BACKEND_INDEXING_PRIORITY;
		projectManager.runOnProjectServer(projectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				IndexWork work = new IndexWork(priority, commitId);
				batchWorkManager.submit(getBatchWorker(projectId), work);
				return null;
			}
			
		});
	}
	
	private static class IndexWork extends Prioritized {

		private final ObjectId commitId;
		
		public IndexWork(int priority, ObjectId commitId) {
			super(priority);
			this.commitId = commitId;
		}

		public ObjectId getCommitId() {
			return commitId;
		}
		
	}

}
