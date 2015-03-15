package com.pmease.gitplex.search;

import static com.pmease.gitplex.search.FieldConstants.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.pmease.commons.lang.AnalyzeResult;
import com.pmease.commons.lang.Analyzers;
import com.pmease.commons.util.Charsets;
import com.pmease.commons.util.LockUtils;
import com.pmease.gitplex.core.manager.IndexManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.Repository;

@Singleton
public class DefaultIndexManager implements IndexManager {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultIndexManager.class);
	
	private static final int MAX_INDEXABLE_SIZE = 1024*1024;
	
	private final StorageManager storageManager;
	
	private final Analyzers analyzers;
	
	@Inject
	public DefaultIndexManager(StorageManager storageManager, Analyzers analyzers) {
		this.storageManager = storageManager;
		this.analyzers = analyzers;
	}

	private IndexWriterConfig newWriterConfig() {
		Analyzer analyzer = new StandardAnalyzer();					
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
	
	private void index(org.eclipse.jgit.lib.Repository repo, String commitHash, 
			IndexWriter writer, IndexSearcher searcher) throws Exception {
		RevWalk revWalk = new RevWalk(repo);
		TreeWalk treeWalk = new TreeWalk(repo);
		
		treeWalk.addTree(revWalk.parseCommit(repo.resolve(commitHash)));
		treeWalk.setRecursive(true);
		
		if (searcher != null) {
			if (analyzers.getVersion().equals(getAnalyzersVersion(searcher, commitHash)))
				return;
			
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

		while (treeWalk.next()) {
			if ((treeWalk.getRawMode(0) & FileMode.TYPE_MASK) == FileMode.TYPE_FILE 
					&& (treeWalk.getTreeCount() == 1 || !treeWalk.idEqual(0, 1))) {
				ObjectId blobId = treeWalk.getObjectId(0);
				String path = treeWalk.getPathString();
				
				String blobAnalyzerVersion;
				
				BooleanQuery query = new BooleanQuery();
				query.add(BLOB_HASH.query(blobId.name()), Occur.MUST);
				query.add(BLOB_PATH.query(path), Occur.MUST);
				TopDocs topDocs = searcher.search(query, 1);
				if (topDocs.scoreDocs.length != 0)
					blobAnalyzerVersion = searcher.doc(topDocs.scoreDocs[0].doc).get(BLOB_ANALYZER_VERSION.name());
				else
					blobAnalyzerVersion = null;

				String currentAnalyzerVersion = analyzers.getVersion(path);

				if (blobAnalyzerVersion != null) {
					if (currentAnalyzerVersion != null) {
						if (!blobAnalyzerVersion.equals(currentAnalyzerVersion)) {
							writer.deleteDocuments(query);
							indexBlob(writer, repo, blobId, path);
						}
					} else {
						writer.deleteDocuments(query);
					}
				} else if (currentAnalyzerVersion != null) {
					indexBlob(writer, repo, blobId, path);
				}
			}
		}

		// record current commit so that we know which commit has been indexed
		Document document = new Document();
		document.add(new StringField(COMMIT_HASH.name(), commitHash, Store.YES));
		document.add(new StringField(COMMIT_ANALYZERS_VERSION.name(), analyzers.getVersion(), Store.YES));
		writer.updateDocument(new Term(COMMIT_HASH.name(), commitHash), document);
		
		// record last commit so that we only need to indexing changed files for subsequent commits
		document = new Document();
		document.add(new StringField(META.name(), LAST_COMMIT.name(), Store.YES));
		document.add(new StringField(LAST_COMMIT_ANALYZERS_VERSION.name(), analyzers.getVersion(), Store.YES));
		document.add(new StringField(LAST_COMMIT_HASH.name(), commitHash, Store.YES));
		writer.updateDocument(new Term(META.name(), LAST_COMMIT.name()), document);
	}
	
	private void indexBlob(IndexWriter writer, org.eclipse.jgit.lib.Repository repo, 
			ObjectId blobId, String path) throws IOException {
		ObjectLoader objectLoader = repo.open(blobId);
		if (objectLoader.getSize() <= MAX_INDEXABLE_SIZE) {
			byte[] bytes = objectLoader.getCachedBytes();
			Charset charset = Charsets.detectFrom(bytes);
			if (charset != null) {
				String content = new String(bytes, charset);
				AnalyzeResult analyzeResult = analyzers.analyze(content, path);
				Preconditions.checkNotNull(analyzeResult);
				Document document = new Document();
				document.add(new StringField(BLOB_HASH.name(), blobId.name(), Store.YES));
				document.add(new StringField(BLOB_PATH.name(), path, Store.YES));
				document.add(new Field(
						BLOB_SYMBOLS.name(), 
						new LangTokenStream(analyzeResult.getSymbols()), 
						StringField.TYPE_NOT_STORED));
				if (analyzeResult.getOutline() != null) {
					document.add(new Field(
							BLOB_DEFS_SYMBOLS.name(), 
							new LangTokenStream(analyzeResult.getOutline().getSymbols()), 
							StringField.TYPE_NOT_STORED));
				}
				writer.addDocument(document);
			} else {
				logger.debug("File '{}' is not indexed as its charset can not be detected.", path);
			}
		} else {
			logger.debug("File '{}' is not indexed as it is too large.", path);
		}
	}
	
	@Override
	public void index(final Repository repository, final String commitHash) {
		LockUtils.call("index:" + repository.getId(), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				File indexDir = storageManager.getIndexDir(repository);
				try (Directory directory = FSDirectory.open(indexDir)) {
					if (indexDir.exists()) {
						try (IndexReader reader = DirectoryReader.open(directory)) {
							IndexSearcher searcher = new IndexSearcher(reader);
							try (IndexWriter writer = new IndexWriter(directory, newWriterConfig())) {
								Git git = Git.open(repository.git().repoDir());
								try {
									index(git.getRepository(), commitHash, writer, searcher);
									writer.commit();
								} catch (Exception e) {
									writer.rollback();
									Throwables.propagate(e);
								} finally {
									git.close();
								}
							}
						}
					} else {
						try (IndexWriter writer = new IndexWriter(directory, newWriterConfig())) {
							Git git = Git.open(repository.git().repoDir());
							try {
								index(git.getRepository(), commitHash, writer, null);
								writer.commit();
							} catch (Exception e) {
								writer.rollback();
								Throwables.propagate(e);
							} finally {
								git.close();
							}
						}
					}
				}
				return null;
			}
			
		});
	}

	@Override
	public String getAnalyzersVersion(Repository repository, String commitHash) {
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

}
