package com.pmease.gitplex.search;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
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
import com.google.common.eventbus.Subscribe;
import com.pmease.commons.git.GitUtils;
import com.pmease.gitplex.core.events.SystemStopping;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.query.BlobQuery;

@Singleton
public class DefaultSearchManager implements SearchManager, IndexListener {

	private final StorageManager storageManager;
	
	private final Map<Long, SearcherManager> searcherManagers = new ConcurrentHashMap<>();
	
	@Inject
	public DefaultSearchManager(StorageManager storageManager) {
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
	public void search(Repository repository, String commitHash, final BlobQuery query) {
		Preconditions.checkArgument(GitUtils.isHash(commitHash));
		query.getHits().clear();
		
		SearcherManager searcherManager = getSearcherManager(repository);
		if (searcherManager != null) {
			try {
				final IndexSearcher searcher = searcherManager.acquire();
				try {
					final org.eclipse.jgit.lib.Repository repo = 
							RepositoryCache.open(FileKey.exact(repository.git().repoDir(), FS.DETECTED));
					try {
						final RevTree revTree = new RevWalk(repo).parseCommit(repo.resolve(commitHash)).getTree();
						final Set<String> checkedBlobPaths = new HashSet<>();
						
						searcher.search(query, new Collector() {
	
							private AtomicReaderContext context;
							
							@Override
							public void setScorer(Scorer scorer) throws IOException {
							}
	
							@Override
							public void collect(int doc) throws IOException {
								if (query.getHits().size() < query.getCount()) {
									BinaryDocValues cachedBlobPaths = FieldCache.DEFAULT.getTerms(
											context.reader(), FieldConstants.BLOB_PATH.name(), false);
									String blobPath = cachedBlobPaths.get(doc).utf8ToString();
									
									if (!checkedBlobPaths.contains(blobPath)) {
										TreeWalk treeWalk = TreeWalk.forPath(repo, blobPath, revTree);									
										if (treeWalk != null) 
											query.hit(treeWalk);
										checkedBlobPaths.add(blobPath);
									}
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
						
					} finally {
						repo.close();
					}
				} finally {
					searcherManager.release(searcher);
				}
			} catch (IOException e) {
				throw Throwables.propagate(e);
			}
		}
	}

	@Override
	public void commitIndexed(Repository repository, String commitHash) {
		try {
			getSearcherManager(repository).maybeRefresh();
		} catch (IOException e) {
			Throwables.propagate(e);
		}
	}

	@Override
	public void removingIndex(Repository repository) {
		synchronized (searcherManagers) {
			SearcherManager searcherManager = searcherManagers.get(repository.getId());
			if (searcherManager != null) {
				try {
					searcherManager.close();
				} catch (IOException e) {
					Throwables.propagate(e);
				}
				searcherManagers.remove(repository.getId());
			}
		}
	}

}
