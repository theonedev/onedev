package com.pmease.gitplex.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FS;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.lang.Analyzers;
import com.pmease.commons.util.Charsets;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.Repository;

@Singleton
public class DefaultSearchManager implements SearchManager {

	private final StorageManager storageManager;
	
	private final Analyzers analyzers;
	
	private final Map<Long, Optional<SearcherManager>> searcherManagers = new ConcurrentHashMap<>();
	
	@Inject
	public DefaultSearchManager(EventBus eventBus, StorageManager storageManager, Analyzers analyzers) {
		eventBus.register(this);
		this.storageManager = storageManager;
		this.analyzers = analyzers;
	}
	
	@Nullable
	private SearcherManager getSearcherManager(Repository repository) {
		try {
			Optional<SearcherManager> searcherManager = searcherManagers.get(repository.getId());
			if (searcherManager == null) synchronized (searcherManagers) {
				searcherManager = searcherManagers.get(repository.getId());
				if (searcherManager == null) {
					Directory directory = FSDirectory.open(storageManager.getIndexDir(repository));
					if (DirectoryReader.indexExists(directory))
						searcherManager = Optional.of(new SearcherManager(directory, null));
					else 
						searcherManager = Optional.<SearcherManager>absent();
					searcherManagers.put(repository.getId(), searcherManager);
				}
			}
			return searcherManager.get();
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}
	
	@Subscribe
	public void indexRemoving(IndexRemoving event) {
		Repository repository = event.getRepository();
		if (searcherManagers.containsKey(repository.getId())) synchronized (searcherManagers) {
			Optional<SearcherManager> searcherManager = searcherManagers.get(repository.getId());
			if (searcherManager != null) {
				if (searcherManager.isPresent()) {
					try {
						searcherManager.get().close();
					} catch (IOException e) {
						Throwables.propagate(e);
					}
				}
				searcherManagers.remove(repository.getId());
			}
		}
	}
	
	@Override
	public List<SearchHit> search(Repository repository, String commitHash,
			Query query, SearchHit after, int count) {
		Preconditions.checkArgument(GitUtils.isHash(commitHash));
		SearcherManager searcherManager = getSearcherManager(repository);
		if (searcherManager != null) {
			try {
				searcherManager.maybeRefresh();
				final IndexSearcher searcher = searcherManager.acquire();
				try {
					final Git git = Git.wrap(RepositoryCache.open(FileKey.exact(repository.git().repoDir(), FS.DETECTED)));
					AnyObjectId commitId = git.getRepository().resolve(commitHash);
					final RevTree revTree = new RevWalk(git.getRepository()).parseCommit(commitId).getTree();
					final List<SearchHit> hits = new ArrayList<>();
					final Map<String, String> blobHashes = new HashMap<>();
					searcher.search(query, new Collector() {

						private int docBase;
						
						@Override
						public void setScorer(Scorer scorer) throws IOException {
						}

						@Override
						public void collect(int doc) throws IOException {
							Document document = searcher.doc(docBase + doc);
							String blobHash = document.get(FieldConstants.BLOB_HASH.name());
							String blobPath = document.get(FieldConstants.BLOB_PATH.name());
							
							String blobHashOfCommit = blobHashes.get(blobPath);
							if (blobHashOfCommit == null) {
								TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), blobPath, revTree);									
								
								if (treeWalk == null) 
									blobHashOfCommit = "";
								else 
									blobHashOfCommit = treeWalk.getObjectId(0).name();
								
								blobHashes.put(blobPath, blobHashOfCommit);
							}
							if (blobHashOfCommit.equals(blobHash)) {
								byte[] bytes = git.getRepository().open(ObjectId.fromString(blobHash)).getCachedBytes();
								String fileContent = new String(bytes, Charsets.detectFrom(bytes));
								analyzers.analyze(fileContent, blobPath);
								hits.add(new SearchHit(blobPath, 0, 0, null, null));
							}
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
					
					return hits;
				} finally {
					searcherManager.release(searcher);
				}
			} catch (IOException e) {
				throw Throwables.propagate(e);
			}
		} else {
			return new ArrayList<>();
		}
		
		/*
		File indexDir = storageManager.getIndexDir(repository);
		
		try (Directory directory = FSDirectory.open(indexDir)) {
			if (DirectoryReader.indexExists(directory)) {
				try (IndexReader reader = DirectoryReader.open(directory)) {
//					final Git git = Git.open(repository.git().repoDir());
					final Git git = Git.wrap(RepositoryCache.open(FileKey.exact(repository.git().repoDir(), FS.DETECTED)));
					AnyObjectId commitId = git.getRepository().resolve(commitHash);
					final RevTree revTree = new RevWalk(git.getRepository()).parseCommit(commitId).getTree();
					try {
						final List<SearchHit> hits = new ArrayList<>();
						final Map<String, String> blobHashes = new HashMap<>();
						final IndexSearcher searcher = new IndexSearcher(reader);
						searcher.search(query, new Collector() {

							private int docBase;
							
							@Override
							public void setScorer(Scorer scorer) throws IOException {
							}

							@Override
							public void collect(int doc) throws IOException {
								Document document = searcher.doc(docBase + doc);
								String blobHash = document.get(FieldConstants.BLOB_HASH.name());
								String blobPath = document.get(FieldConstants.BLOB_PATH.name());
								
								String blobHashOfCommit = blobHashes.get(blobPath);
								if (blobHashOfCommit == null) {
									TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), blobPath, revTree);									
									
									if (treeWalk == null) 
										blobHashOfCommit = "";
									else 
										blobHashOfCommit = treeWalk.getObjectId(0).name();
									
									blobHashes.put(blobPath, blobHashOfCommit);
								}
								if (blobHashOfCommit.equals(blobHash)) {
									byte[] bytes = git.getRepository().open(ObjectId.fromString(blobHash)).getCachedBytes();
									String fileContent = new String(bytes, Charsets.detectFrom(bytes));
									analyzers.analyze(fileContent, blobPath);
									hits.add(new SearchHit(blobPath, 0, 0, null, null));
								}
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
						
						return hits;
					} catch (Exception e) {
						throw Throwables.propagate(e);
					} finally {
						//git.close();
					}					
				}
			} else {
				return new ArrayList<>();
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		*/
	}

}
