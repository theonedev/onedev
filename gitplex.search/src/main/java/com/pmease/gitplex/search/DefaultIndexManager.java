package com.pmease.gitplex.search;

import static com.pmease.gitplex.search.FieldConstants.BLOB_ANALYZER_VERSION;
import static com.pmease.gitplex.search.FieldConstants.BLOB_CONTENT;
import static com.pmease.gitplex.search.FieldConstants.BLOB_HASH;
import static com.pmease.gitplex.search.FieldConstants.BLOB_PATH;
import static com.pmease.gitplex.search.FieldConstants.BLOB_SYMBOLS;
import static com.pmease.gitplex.search.FieldConstants.COMMIT_ANALYZERS_VERSION;
import static com.pmease.gitplex.search.FieldConstants.COMMIT_HASH;
import static com.pmease.gitplex.search.FieldConstants.LAST_COMMIT;
import static com.pmease.gitplex.search.FieldConstants.LAST_COMMIT_ANALYZERS_VERSION;
import static com.pmease.gitplex.search.FieldConstants.LAST_COMMIT_HASH;
import static com.pmease.gitplex.search.FieldConstants.META;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
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
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.lang.AnalyzeException;
import com.pmease.commons.lang.AnalyzeResult;
import com.pmease.commons.lang.Analyzers;
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
	
	private static final int MAX_INDEXABLE_SIZE = 1024*1024;
	
	private final EventBus eventBus;
	
	private final StorageManager storageManager;
	
	private final Analyzers analyzers; // language analyzers, different from lucene analyzer
	
	@Inject
	public DefaultIndexManager(EventBus eventBus, StorageManager storageManager, Analyzers analyzers) {
		this.eventBus = eventBus;
		eventBus.register(this);
		this.storageManager = storageManager;
		this.analyzers = analyzers;
	}

	private IndexWriterConfig newWriterConfig() {
		PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), 
				ImmutableMap.<String, Analyzer>of(FieldConstants.BLOB_CONTENT.name(), new TriGramAnalyzer()));
		
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LATEST, analyzer);
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		return iwc;
	}
	
	private String getAnalyzersVersion(IndexSearcher searcher, String commitHash) throws IOException {
		TopDocs topDocs = searcher.search(COMMIT_HASH.query(commitHash), 1);
		if (topDocs.scoreDocs.length != 0) 
			return searcher.doc(topDocs.scoreDocs[0].doc).get(COMMIT_ANALYZERS_VERSION.name()); 
		else
			return null;
	}
	
	private IndexResult index(org.eclipse.jgit.lib.Repository repo, String commitHash, 
			IndexWriter writer, IndexSearcher searcher) throws Exception {
		RevWalk revWalk = new RevWalk(repo);
		TreeWalk treeWalk = new TreeWalk(repo);
		
		treeWalk.addTree(revWalk.parseCommit(repo.resolve(commitHash)).getTree());
		treeWalk.setRecursive(true);
		
		if (searcher != null) {
			if (analyzers.getVersion().equals(getAnalyzersVersion(searcher, commitHash)))
				return new IndexResult(0, 0);
			
			TopDocs topDocs = searcher.search(META.query(LAST_COMMIT.name()), 1);
			if (topDocs.scoreDocs.length != 0) {
				Document doc = searcher.doc(topDocs.scoreDocs[0].doc);
				String lastCommitAnalyzersVersion = doc.get(LAST_COMMIT_ANALYZERS_VERSION.name());
				if (lastCommitAnalyzersVersion.equals(analyzers.getVersion())) {
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
				String path = treeWalk.getPathString();
				
				BooleanQuery query = new BooleanQuery();
				query.add(BLOB_HASH.query(blobId.name()), Occur.MUST);
				query.add(BLOB_PATH.query(path), Occur.MUST);
				
				String blobAnalyzerVersion = null;
				
				if (searcher != null) {
					TopDocs topDocs = searcher.search(query, 1);
					if (topDocs.scoreDocs.length != 0)
						blobAnalyzerVersion = searcher.doc(topDocs.scoreDocs[0].doc).get(BLOB_ANALYZER_VERSION.name());
					checked++;
				}

				String currentAnalyzerVersion = analyzers.getVersion(path);

				if (blobAnalyzerVersion != null) {
					if (currentAnalyzerVersion != null) {
						if (!blobAnalyzerVersion.equals(currentAnalyzerVersion)) {
							writer.deleteDocuments(query);
							if (indexBlob(writer, repo, currentAnalyzerVersion, blobId, path))
								indexed++;
						}
					} else {
						writer.deleteDocuments(query);
					}
				} else if (currentAnalyzerVersion != null) {
					if (indexBlob(writer, repo, currentAnalyzerVersion, blobId, path))
						indexed++;
				}
			}
		}

		// record current commit so that we know which commit has been indexed
		Document document = new Document();
		document.add(new StringField(COMMIT_HASH.name(), commitHash, Store.NO));
		document.add(new StoredField(COMMIT_ANALYZERS_VERSION.name(), analyzers.getVersion()));
		writer.updateDocument(COMMIT_HASH.term(commitHash), document);
		
		// record last commit so that we only need to indexing changed files for subsequent commits
		document = new Document();
		document.add(new StringField(META.name(), LAST_COMMIT.name(), Store.NO));
		document.add(new StoredField(LAST_COMMIT_ANALYZERS_VERSION.name(), analyzers.getVersion()));
		document.add(new StoredField(LAST_COMMIT_HASH.name(), commitHash));
		writer.updateDocument(META.term(LAST_COMMIT.name()), document);
		
		return new IndexResult(checked, indexed);
	}
	
	private boolean indexBlob(IndexWriter writer, org.eclipse.jgit.lib.Repository repo, 
			String analyzerVersion, ObjectId blobId, String path) throws IOException {
		ObjectLoader objectLoader = repo.open(blobId);
		if (objectLoader.getSize() <= MAX_INDEXABLE_SIZE) {
			byte[] bytes = objectLoader.getCachedBytes();
			Charset charset = Charsets.detectFrom(bytes);
			if (charset != null) {
				String content = new String(bytes, charset);
				AnalyzeResult analyzeResult;
				try {
					 analyzeResult = analyzers.analyze(content, path);
				} catch (AnalyzeException e) {
					logger.error("Error analyzing (blobId:" + blobId.name() + ", file:" + path + ")", e);
					return false;
				}
				Preconditions.checkNotNull(analyzeResult);
				Document document = new Document();
				document.add(new StringField(BLOB_HASH.name(), blobId.name(), Store.YES));
				document.add(new StringField(BLOB_PATH.name(), path, Store.YES));
				document.add(new TextField(BLOB_CONTENT.name(), content, Store.YES));
				if (analyzeResult.getOutline() != null) {
					document.add(new Field(
							BLOB_SYMBOLS.name(), 
							new LangTokenStream(analyzeResult.getOutline().getSymbols()), 
							TextField.TYPE_NOT_STORED));
				}
				document.add(new StoredField(BLOB_ANALYZER_VERSION.name(), analyzerVersion));
				writer.addDocument(document);
				return true;
			} else {
				logger.debug("Ignoring binary file '{}'.", path);
				return false;
			}
		} else {
			logger.debug("Ignoring too large file '{}'.", path);
			return false;
		}
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
							try (IndexWriter writer = new IndexWriter(directory, newWriterConfig())) {
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
						try (IndexWriter writer = new IndexWriter(directory, newWriterConfig())) {
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
				
				if (indexResult.getIndexed() != 0)
					eventBus.post(new CommitIndexed(repository, commitHash));
				
				return indexResult;
			}
			
		});
	}

	@Override
	public String getAnalyzersVersion(Repository repository, String commitHash) {
		Preconditions.checkArgument(GitUtils.isHash(commitHash));
		File indexDir = storageManager.getIndexDir(repository);
		if (indexDir.exists()) {
			try (Directory directory = FSDirectory.open(indexDir)) {
				try (IndexReader reader = DirectoryReader.open(directory)) {
					return getAnalyzersVersion(new IndexSearcher(reader), commitHash);
				}
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		} else {
			return null;
		}
	}
	
	@Subscribe
	public void repositoryRemoved(RepositoryRemoved event) {
		eventBus.post(new IndexRemoving(event.getRepository()));
		FileUtils.deleteDir(storageManager.getIndexDir(event.getRepository()));
	}

}
