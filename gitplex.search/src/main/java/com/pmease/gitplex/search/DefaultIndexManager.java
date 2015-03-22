package com.pmease.gitplex.search;

import static com.pmease.gitplex.search.FieldConstants.BLOB_TEXT;
import static com.pmease.gitplex.search.FieldConstants.BLOB_HASH;
import static com.pmease.gitplex.search.FieldConstants.BLOB_INDEX_VERSION;
import static com.pmease.gitplex.search.FieldConstants.BLOB_PATH;
import static com.pmease.gitplex.search.FieldConstants.BLOB_SYMBOLS;
import static com.pmease.gitplex.search.FieldConstants.COMMIT_HASH;
import static com.pmease.gitplex.search.FieldConstants.COMMIT_INDEX_VERSION;
import static com.pmease.gitplex.search.FieldConstants.LAST_COMMIT;
import static com.pmease.gitplex.search.FieldConstants.LAST_COMMIT_HASH;
import static com.pmease.gitplex.search.FieldConstants.LAST_COMMIT_INDEX_VERSION;
import static com.pmease.gitplex.search.FieldConstants.META;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.lang.ExtractException;
import com.pmease.commons.lang.Extractor;
import com.pmease.commons.lang.Extractors;
import com.pmease.commons.util.Charsets;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.LockUtils;
import com.pmease.gitplex.core.events.RepositoryRemoved;
import com.pmease.gitplex.core.manager.IndexManager;
import com.pmease.gitplex.core.manager.IndexResult;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.Repository;

@Singleton
public class DefaultIndexManager implements IndexManager {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultIndexManager.class);
	
	private static final int INDEX_VERSION = 1;
	
	private final StorageManager storageManager;
	
	private final Set<IndexListener> listeners;
	
	private final Extractors extractors; 
	
	@Inject
	public DefaultIndexManager(EventBus eventBus, Set<IndexListener> listeners, 
			StorageManager storageManager, Extractors extractors) {
		eventBus.register(this);
		this.listeners = listeners;
		this.storageManager = storageManager;
		this.extractors = extractors;
	}

	private String getCommitIndexVersion(final IndexSearcher searcher, String commitHash) throws IOException {
		final AtomicReference<String> indexVersion = new AtomicReference<>(null);
		
		searcher.search(COMMIT_HASH.query(commitHash), new Collector() {

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
	
	private IndexResult index(org.eclipse.jgit.lib.Repository repo, String commitHash, 
			IndexWriter writer, final IndexSearcher searcher) throws Exception {
		RevWalk revWalk = new RevWalk(repo);
		TreeWalk treeWalk = new TreeWalk(repo);
		
		treeWalk.addTree(revWalk.parseCommit(repo.resolve(commitHash)).getTree());
		treeWalk.setRecursive(true);
		
		if (searcher != null) {
			if (getCurrentCommitIndexVersion().equals(getCommitIndexVersion(searcher, commitHash)))
				return new IndexResult(0, 0);
			
			TopDocs topDocs = searcher.search(META.query(LAST_COMMIT.name()), 1);
			if (topDocs.scoreDocs.length != 0) {
				Document doc = searcher.doc(topDocs.scoreDocs[0].doc);
				String lastCommitAnalyzersVersion = doc.get(LAST_COMMIT_INDEX_VERSION.name());
				if (lastCommitAnalyzersVersion.equals(extractors.getVersion())) {
					String lastCommitHash = doc.get(LAST_COMMIT_HASH.name());
					ObjectId lastCommitId = repo.resolve(lastCommitHash);
					if (repo.hasObject(lastCommitId)) { 
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

				Extractor extractor = extractors.getExtractor(blobPath);
				String currentBlobIndexVersion = getCurrentBlobIndexVersion(extractor);
				String blobIndexVersion = blobIndexVersionRef.get();
				if (blobIndexVersion != null) {
					if (currentBlobIndexVersion != null) {
						if (!blobIndexVersion.equals(currentBlobIndexVersion)) {
							writer.deleteDocuments(query);
							indexBlob(writer, repo, extractor, blobId, blobPath);
							indexed++;
						}
					} else {
						writer.deleteDocuments(query);
					}
				} else if (currentBlobIndexVersion != null) {
					indexBlob(writer, repo, extractor, blobId, blobPath);
					indexed++;
				}
			}
		}

		// record current commit so that we know which commit has been indexed
		Document document = new Document();
		document.add(new StringField(COMMIT_HASH.name(), commitHash, Store.NO));
		document.add(new StoredField(COMMIT_INDEX_VERSION.name(), getCurrentCommitIndexVersion()));
		writer.updateDocument(COMMIT_HASH.term(commitHash), document);
		
		// record last commit so that we only need to indexing changed files for subsequent commits
		document = new Document();
		document.add(new StringField(META.name(), LAST_COMMIT.name(), Store.NO));
		document.add(new StoredField(LAST_COMMIT_INDEX_VERSION.name(), extractors.getVersion()));
		document.add(new StoredField(LAST_COMMIT_HASH.name(), commitHash));
		writer.updateDocument(META.term(LAST_COMMIT.name()), document);
		
		return new IndexResult(checked, indexed);
	}
	
	private void indexBlob(IndexWriter writer, org.eclipse.jgit.lib.Repository repo, 
			Extractor extractor, ObjectId blobId, String blobPath) throws IOException {
		Document document = new Document();
		
		document.add(new StoredField(BLOB_INDEX_VERSION.name(), getCurrentBlobIndexVersion(extractor)));
		document.add(new StringField(BLOB_HASH.name(), blobId.name(), Store.NO));
		document.add(new StringField(BLOB_PATH.name(), blobPath, Store.YES));
		
		List<String> symbols = new ArrayList<>();
		if (blobPath.indexOf('/') != -1)
			symbols.add(StringUtils.substringAfterLast(blobPath, "/"));
		else
			symbols.add(blobPath);
		
		ObjectLoader objectLoader = repo.open(blobId);
		if (objectLoader.getSize() <= IndexConstants.MAX_INDEXABLE_SIZE) {
			byte[] bytes = objectLoader.getCachedBytes();
			Charset charset = Charsets.detectFrom(bytes);
			if (charset != null) {
				String content = new String(bytes, charset);
				TokenStream tokens = new NGramTokenizer(new StringReader(content), 
						IndexConstants.TEXT_GRAM_SIZE, IndexConstants.TEXT_GRAM_SIZE);
				tokens = new LowerCaseFilter(tokens);
				document.add(new Field(BLOB_TEXT.name(), tokens, TextField.TYPE_NOT_STORED));
				
				if (extractor != null) {
					try {
						symbols.addAll(extractor.extract(content).getSearchables());
					} catch (ExtractException e) {
						logger.error("Error extracting symbols of blob (hash:" + blobId.name() + ", path:" + blobPath + ")", e);
					}
				} 
			} else {
				logger.debug("Ignore content of binary file '{}'.", blobPath);
			}
		} else {
			logger.debug("Ignore content of large file '{}'.", blobPath);
		}

		for (String symbol: symbols) {
			TokenStream tokens = new NGramTokenizer(new StringReader(symbol), 
					IndexConstants.SYMBOL_GRAM_SIZE, IndexConstants.SYMBOL_GRAM_SIZE);
			tokens = new LowerCaseFilter(tokens);
			document.add(new Field(BLOB_SYMBOLS.name(), tokens, TextField.TYPE_NOT_STORED));
		}
		
		writer.addDocument(document);
	}
	
	private IndexWriterConfig newIndexWriterConfig() {
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, new StandardAnalyzer());
		config.setOpenMode(OpenMode.CREATE_OR_APPEND);
		return config;
	}
	
	@Override
	public IndexResult index(final Repository repository, final String commitHash) {
		Preconditions.checkArgument(GitUtils.isHash(commitHash));
		
		logger.info("Indexing commit '{}' of repository '{}'...", commitHash, repository);
		
		return LockUtils.call("index:" + repository.getId(), new Callable<IndexResult>() {

			@Override
			public IndexResult call() throws Exception {
				IndexResult indexResult;
				File indexDir = storageManager.getIndexDir(repository);
				try (Directory directory = FSDirectory.open(indexDir)) {
					if (DirectoryReader.indexExists(directory)) {
						try (IndexReader reader = DirectoryReader.open(directory)) {
							IndexSearcher searcher = new IndexSearcher(reader);
							try (IndexWriter writer = new IndexWriter(directory, newIndexWriterConfig())) {
								org.eclipse.jgit.lib.Repository repo = 
										RepositoryCache.open(FileKey.exact(repository.git().repoDir(), FS.DETECTED));
								try {
									indexResult = index(repo, commitHash, writer, searcher);
									writer.commit();
								} catch (Exception e) {
									writer.rollback();
									throw Throwables.propagate(e);
								} finally {
									repo.close();
								}
							}
						}
					} else {
						try (IndexWriter writer = new IndexWriter(directory, newIndexWriterConfig())) {
							org.eclipse.jgit.lib.Repository repo = 
									RepositoryCache.open(FileKey.exact(repository.git().repoDir(), FS.DETECTED));
							try {
								indexResult = index(repo, commitHash, writer, null);
								writer.commit();
							} catch (Exception e) {
								writer.rollback();
								throw Throwables.propagate(e);
							} finally {
								repo.close();
							}
						}
					}
				}
				logger.info("Commit {} indexed (checked blobs: {}, indexed blobs: {})", 
						commitHash, indexResult.getChecked(), indexResult.getIndexed());
				
				if (indexResult.getIndexed() != 0) {
					for (IndexListener listener: listeners)
						listener.commitIndexed(repository, commitHash);
				}
				
				return indexResult;
			}
			
		});
	}

	@Subscribe
	public void repositoryRemoved(RepositoryRemoved event) {
		for (IndexListener listener: listeners)
			listener.removingIndex(event.getRepository());
		FileUtils.deleteDir(storageManager.getIndexDir(event.getRepository()));
	}
	
	private String getCurrentCommitIndexVersion() {
		return INDEX_VERSION + ";" + extractors.getVersion();
	}
	
	private String getCurrentBlobIndexVersion(Extractor extractor) {
		if (extractor != null)
			return INDEX_VERSION + ";" + extractor.getVersion();
		else
			return String.valueOf(INDEX_VERSION);
	}
	
}
