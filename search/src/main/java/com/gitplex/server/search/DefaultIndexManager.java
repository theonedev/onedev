package com.gitplex.server.search;

import static com.gitplex.server.search.FieldConstants.BLOB_HASH;
import static com.gitplex.server.search.FieldConstants.BLOB_INDEX_VERSION;
import static com.gitplex.server.search.FieldConstants.BLOB_NAME;
import static com.gitplex.server.search.FieldConstants.BLOB_PATH;
import static com.gitplex.server.search.FieldConstants.BLOB_PRIMARY_SYMBOLS;
import static com.gitplex.server.search.FieldConstants.BLOB_SECONDARY_SYMBOLS;
import static com.gitplex.server.search.FieldConstants.BLOB_SYMBOL_LIST;
import static com.gitplex.server.search.FieldConstants.BLOB_TEXT;
import static com.gitplex.server.search.FieldConstants.COMMIT_HASH;
import static com.gitplex.server.search.FieldConstants.COMMIT_INDEX_VERSION;
import static com.gitplex.server.search.FieldConstants.LAST_COMMIT;
import static com.gitplex.server.search.FieldConstants.LAST_COMMIT_HASH;
import static com.gitplex.server.search.FieldConstants.LAST_COMMIT_INDEX_VERSION;
import static com.gitplex.server.search.FieldConstants.META;
import static com.gitplex.server.search.IndexConstants.MAX_INDEXABLE_SIZE;
import static com.gitplex.server.search.IndexConstants.NGRAM_SIZE;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
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

import com.gitplex.launcher.loader.Listen;
import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.jsymbol.ExtractException;
import com.gitplex.jsymbol.Symbol;
import com.gitplex.jsymbol.SymbolExtractor;
import com.gitplex.jsymbol.SymbolExtractorRegistry;
import com.gitplex.server.event.RefUpdated;
import com.gitplex.server.manager.BatchWorkManager;
import com.gitplex.server.manager.DepotManager;
import com.gitplex.server.manager.StorageManager;
import com.gitplex.server.model.Depot;
import com.gitplex.server.persistence.UnitOfWork;
import com.gitplex.server.util.BatchWorker;
import com.gitplex.server.util.ContentDetector;
import com.gitplex.server.util.IndexResult;
import com.gitplex.server.util.concurrent.Prioritized;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

@Singleton
public class DefaultIndexManager implements IndexManager {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultIndexManager.class);

	private static final int UI_INDEXING_PRIORITY = 10;
	
	private static final int BACKEND_INDEXING_PRIORITY = 50;
	
	private static final int INDEX_VERSION = 1;
	
	private final StorageManager storageManager;
	
	private final BatchWorkManager batchWorkManager;
	
	private final UnitOfWork unitOfWork;
	
	private final DepotManager depotManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultIndexManager(ListenerRegistry listenerRegistry, StorageManager storageManager, 
			BatchWorkManager batchWorkManager, UnitOfWork unitOfWork, DepotManager depotManager) {
		this.listenerRegistry = listenerRegistry;
		this.storageManager = storageManager;
		this.batchWorkManager = batchWorkManager;
		this.unitOfWork = unitOfWork;
		this.depotManager = depotManager;
	}

	private String getCommitIndexVersion(final IndexSearcher searcher, AnyObjectId commitId) throws IOException {
		final AtomicReference<String> indexVersion = new AtomicReference<>(null);
		
		searcher.search(COMMIT_HASH.query(commitId.getName()), new Collector() {

			private int docBase;
			
			@Override
			public void setScorer(Scorer scorer) throws IOException {
			}

			@Override
			public void collect(int doc) throws IOException {
				indexVersion.set(searcher.doc(docBase+doc).get(COMMIT_INDEX_VERSION.name()));
			}

			@Override
			public void setNextReader(AtomicReaderContext context) throws IOException {
				docBase = context.docBase;
			}

			@Override
			public boolean acceptsDocsOutOfOrder() {
				return true;
			}
			
		});
		return indexVersion.get();
	}
	
	private IndexResult index(Repository repository, AnyObjectId commitId, 
			IndexWriter writer, final IndexSearcher searcher) throws Exception {
		try (	RevWalk revWalk = new RevWalk(repository); 
				TreeWalk treeWalk = new TreeWalk(repository)) {
			treeWalk.addTree(revWalk.parseCommit(commitId).getTree());
			treeWalk.setRecursive(true);
			
			if (searcher != null) {
				TopDocs topDocs = searcher.search(META.query(LAST_COMMIT.name()), 1);
				if (topDocs.scoreDocs.length != 0) {
					Document doc = searcher.doc(topDocs.scoreDocs[0].doc);
					String lastCommitAnalyzersVersion = doc.get(LAST_COMMIT_INDEX_VERSION.name());
					if (lastCommitAnalyzersVersion.equals(SymbolExtractorRegistry.getVersion())) {
						String lastCommitHash = doc.get(LAST_COMMIT_HASH.name());
						ObjectId lastCommitId = repository.resolve(lastCommitHash);
						if (repository.hasObject(lastCommitId)) { 
							treeWalk.addTree(revWalk.parseCommit(lastCommitId).getTree());
							treeWalk.setFilter(TreeFilter.ANY_DIFF);
						}
					}
				}
			}
	
			int indexed = 0;
			int checked = 0;
			while (treeWalk.next()) {
				if ((treeWalk.getRawMode(0) & FileMode.TYPE_MASK) == FileMode.TYPE_FILE 
						&& (treeWalk.getTreeCount() == 1 || !treeWalk.idEqual(0, 1))) {
					ObjectId blobId = treeWalk.getObjectId(0);
					String blobPath = treeWalk.getPathString();
					String blobName = treeWalk.getNameString();
					
					BooleanQuery query = new BooleanQuery();
					query.add(BLOB_HASH.query(blobId.name()), Occur.MUST);
					query.add(BLOB_PATH.query(blobPath), Occur.MUST);
					
					final AtomicReference<String> blobIndexVersionRef = new AtomicReference<>(null);
					if (searcher != null) {
						searcher.search(query, new Collector() {
	
							private AtomicReaderContext context;
	
							@Override
							public void setScorer(Scorer scorer) throws IOException {
							}
	
							@Override
							public void collect(int doc) throws IOException {
								blobIndexVersionRef.set(searcher.doc(context.docBase+doc).get(BLOB_INDEX_VERSION.name()));
							}
	
							@Override
							public void setNextReader(AtomicReaderContext context) throws IOException {
								this.context = context;
							}
	
							@Override
							public boolean acceptsDocsOutOfOrder() {
								return true;
							}
							
						});
						checked++;
					}
	
					SymbolExtractor<Symbol> extractor = SymbolExtractorRegistry.getExtractor(blobName);
					String currentBlobIndexVersion = getCurrentBlobIndexVersion(extractor);
					String blobIndexVersion = blobIndexVersionRef.get();
					if (blobIndexVersion != null) {
						if (currentBlobIndexVersion != null) {
							if (!blobIndexVersion.equals(currentBlobIndexVersion)) {
								writer.deleteDocuments(query);
								indexBlob(writer, repository, extractor, blobId, blobPath);
								indexed++;
							}
						} else {
							writer.deleteDocuments(query);
						}
					} else if (currentBlobIndexVersion != null) {
						indexBlob(writer, repository, extractor, blobId, blobPath);
						indexed++;
					}
				}
			}
	
			// record current commit so that we know which commit has been indexed
			Document document = new Document();
			document.add(new StringField(COMMIT_HASH.name(), commitId.getName(), Store.NO));
			document.add(new StoredField(COMMIT_INDEX_VERSION.name(), getCurrentCommitIndexVersion()));
			writer.updateDocument(COMMIT_HASH.term(commitId.getName()), document);
			
			// record last commit so that we only need to indexing changed files for subsequent commits
			document = new Document();
			document.add(new StringField(META.name(), LAST_COMMIT.name(), Store.NO));
			document.add(new StoredField(LAST_COMMIT_INDEX_VERSION.name(), SymbolExtractorRegistry.getVersion()));
			document.add(new StoredField(LAST_COMMIT_HASH.name(), commitId.getName()));
			writer.updateDocument(META.term(LAST_COMMIT.name()), document);
			
			return new IndexResult(checked, indexed);
		}
	}
	
	private void indexBlob(IndexWriter writer, Repository repository, 
			SymbolExtractor<Symbol> extractor, ObjectId blobId, String blobPath) throws IOException {
		Document document = new Document();
		
		document.add(new StoredField(BLOB_INDEX_VERSION.name(), getCurrentBlobIndexVersion(extractor)));
		document.add(new StringField(BLOB_HASH.name(), blobId.name(), Store.NO));
		document.add(new StringField(BLOB_PATH.name(), blobPath, Store.YES));
		
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
					try {
						List<Symbol> symbols = extractor.extract(blobName, content);
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
					} catch (ExtractException e) {
						logger.debug("Error extracting symbols from blob (hash:" + blobId.name() + ", path:" + blobPath + ")", e);
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
	
	private IndexWriterConfig newIndexWriterConfig() {
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, new NGramAnalyzer(NGRAM_SIZE, NGRAM_SIZE));
		config.setOpenMode(OpenMode.CREATE_OR_APPEND);
		return config;
	}
	
	private BatchWorker getBatchWorker(Depot depot) {
		Long depotId = depot.getId();
		return new BatchWorker("depot-" + depotId + "-indexBlob", 1) {

			@Override
			public void doWork(Collection<Prioritized> works) {
				Preconditions.checkState(works.size() == 1);
				unitOfWork.call(new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						Depot depot = depotManager.load(depotId);
						IndexWork indexWork = (IndexWork) works.iterator().next();
						doIndex(depot, indexWork.getCommitId());
						return null;
					}
					
				});
			}
			
		};
	}
	
	private IndexResult doIndex(Depot depot, ObjectId commit) {
		IndexResult indexResult;
		File indexDir = storageManager.getIndexDir(depot);
		try (Directory directory = FSDirectory.open(indexDir)) {
			if (DirectoryReader.indexExists(directory)) {
				try (IndexReader reader = DirectoryReader.open(directory)) {
					IndexSearcher searcher = new IndexSearcher(reader);
					if (getCurrentCommitIndexVersion().equals(getCommitIndexVersion(searcher, commit))) {
						return new IndexResult(0, 0);
					} else {
						try (IndexWriter writer = new IndexWriter(directory, newIndexWriterConfig())) {
							logger.debug("Indexing commit (repository: {}, commit: {})", depot.getFQN(), commit.getName());
							try {
								indexResult = index(depot.getRepository(), commit, writer, searcher);
								writer.commit();
							} catch (Exception e) {
								writer.rollback();
								throw Throwables.propagate(e);
							}
						}
					}
				}
			} else {
				try (IndexWriter writer = new IndexWriter(directory, newIndexWriterConfig())) {
					try {
						logger.debug("Indexing commit (repository: {}, commit: {})", depot.getFQN(), commit.getName());
						indexResult = index(depot.getRepository(), commit, writer, null);
						writer.commit();
					} catch (Exception e) {
						writer.rollback();
						throw Throwables.propagate(e);
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		listenerRegistry.post(new CommitIndexed(depot, commit.copy()));
		
		logger.debug("Commit indexed (repository: {}, commit: {}, checked blobs: {}, indexed blobs: {})", 
				depot.getFQN(), commit.name(), indexResult.getChecked(), indexResult.getIndexed());
		
		return indexResult;
	}

	private String getCurrentCommitIndexVersion() {
		return INDEX_VERSION + ";" + SymbolExtractorRegistry.getVersion();
	}
	
	private String getCurrentBlobIndexVersion(SymbolExtractor<Symbol> extractor) {
		if (extractor != null)
			return INDEX_VERSION + ";" + extractor.getVersion();
		else
			return String.valueOf(INDEX_VERSION);
	}

	@Override
	public boolean isIndexed(Depot depot, ObjectId commit) {
		File indexDir = storageManager.getIndexDir(depot);
		try (Directory directory = FSDirectory.open(indexDir)) {
			if (DirectoryReader.indexExists(directory)) {
				try (IndexReader reader = DirectoryReader.open(directory)) {
					IndexSearcher searcher = new IndexSearcher(reader);
					return getCurrentCommitIndexVersion().equals(getCommitIndexVersion(searcher, commit));
				}
			} else {
				return false;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Listen
	public void on(RefUpdated event) {
		// only index branches at back end, tags will be indexed on demand from GUI 
		// as many tags might be pushed all at once when the repository is imported 
		if (event.getRefName().startsWith(Constants.R_HEADS) && !event.getNewObjectId().equals(ObjectId.zeroId())) {
			IndexWork work = new IndexWork(BACKEND_INDEXING_PRIORITY, event.getNewObjectId());
			batchWorkManager.submit(getBatchWorker(event.getDepot()), work);
		}
	}

	@Override
	public void indexAsync(Depot depot, ObjectId commit) {
		int priority;
		if (RequestCycle.get() != null)
			priority = UI_INDEXING_PRIORITY;
		else
			priority = BACKEND_INDEXING_PRIORITY;
		IndexWork work = new IndexWork(priority, commit);
		batchWorkManager.submit(getBatchWorker(depot), work);
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
