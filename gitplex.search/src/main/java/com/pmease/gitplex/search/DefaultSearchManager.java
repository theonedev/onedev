package com.pmease.gitplex.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FS;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.lang.Analyzers;
import com.pmease.gitplex.core.events.SystemStopping;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.Repository;

@Singleton
public class DefaultSearchManager implements SearchManager {

	private final StorageManager storageManager;
	
	private final Map<Long, SearcherManager> searcherManagers = new ConcurrentHashMap<>();
	
	@Inject
	public DefaultSearchManager(EventBus eventBus, StorageManager storageManager, Analyzers analyzers) {
		eventBus.register(this);
		this.storageManager = storageManager;
	}
	
	@Nullable
	private SearcherManager getSearcherManager(Repository repository) {
		try {
			SearcherManager searcherManager = searcherManagers.get(repository.getId());
			if (searcherManager == null) synchronized (searcherManagers) {
				searcherManager = searcherManagers.get(repository.getId());
				if (searcherManager == null) {
					Directory directory = FSDirectory.open(storageManager.getIndexDir(repository));
					if (DirectoryReader.indexExists(directory)) {
						searcherManager = new SearcherManager(directory, null);
						searcherManagers.put(repository.getId(), searcherManager);
					}
				}
			}
			return searcherManager;
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}
	
	@Subscribe
	public void indexRemoving(IndexRemoving event) {
		synchronized (searcherManagers) {
			SearcherManager searcherManager = searcherManagers.get(event.getRepository().getId());
			if (searcherManager != null) {
				try {
					searcherManager.close();
				} catch (IOException e) {
					Throwables.propagate(e);
				}
				searcherManagers.remove(event.getRepository().getId());
			}
		}
	}

	@Subscribe
	public void commitIndexed(CommitIndexed event) {
		try {
			getSearcherManager(event.getRepository()).maybeRefresh();
		} catch (IOException e) {
			Throwables.propagate(e);
		}
	}
	
	@Subscribe
	public void systemStopping(SystemStopping event) {
		synchronized (searcherManagers) {
			for (SearcherManager searcherManager: searcherManagers.values()) {
				try {
					searcherManager.close();
				} catch (IOException e) {
					Throwables.propagate(e);
				}
			}
			searcherManagers.clear();
		}
	}
	
	@Override
	public List<SearchHit> search(Repository repository, String commitHash,
			Query query, SearchHit after, int count) {
		Preconditions.checkArgument(GitUtils.isHash(commitHash));
		SearcherManager searcherManager = getSearcherManager(repository);
		if (searcherManager != null) {
			try {
				final IndexSearcher searcher = searcherManager.acquire();
				try {
					final org.eclipse.jgit.lib.Repository repo = 
							RepositoryCache.open(FileKey.exact(repository.git().repoDir(), FS.DETECTED));
					try {
						final RevTree revTree = new RevWalk(repo).parseCommit(repo.resolve(commitHash)).getTree();
						final List<SearchHit> hits = new ArrayList<>();
						final Map<String, String> blobHashes = new HashMap<>();
						final AtomicInteger skipped = new AtomicInteger(0);
						searcher.search(query, new Collector() {
	
							private AtomicReaderContext context;
							
							@Override
							public void setScorer(Scorer scorer) throws IOException {
							}
	
							@Override
							public void collect(int doc) throws IOException {
								if (hits.size() > 20)
									return;
								
								BinaryDocValues cachedBlobHashes = FieldCache.DEFAULT.getTerms(
										context.reader(), FieldConstants.BLOB_HASH.name(), false);
								BinaryDocValues cachedBlobPaths = FieldCache.DEFAULT.getTerms(
										context.reader(), FieldConstants.BLOB_PATH.name(), false);
								
								String blobHash = cachedBlobHashes.get(doc).utf8ToString();
								String blobPath = cachedBlobPaths.get(doc).utf8ToString();
								/*
								Document document = searcher.doc(context.docBase + doc);
								String blobHash = document.get(FieldConstants.BLOB_HASH.name());
								String blobPath = document.get(FieldConstants.BLOB_PATH.name());
								*/
								String blobHashOfCommit = blobHashes.get(blobPath);
								if (blobHashOfCommit == null) {
									TreeWalk treeWalk = TreeWalk.forPath(repo, blobPath, revTree);									
									
									if (treeWalk == null) 
										blobHashOfCommit = "";
									else 
										blobHashOfCommit = treeWalk.getObjectId(0).name();
									
									blobHashes.put(blobPath, blobHashOfCommit);
								}
								if (blobHashOfCommit.equals(blobHash)) {
									hits.add(new SearchHit(blobPath, 0, 0, null, null));
								} else {
									skipped.incrementAndGet();
								}
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
						
						System.out.println();
						System.out.println();
						System.out.println("paths: " + blobHashes.size());
						System.out.println("skipped: " + skipped.get());
						return hits;
					} finally {
						repo.close();
					}
				} finally {
					searcherManager.release(searcher);
				}
			} catch (IOException e) {
				throw Throwables.propagate(e);
			}
		} else {
			return new ArrayList<>();
		}
	}

}
