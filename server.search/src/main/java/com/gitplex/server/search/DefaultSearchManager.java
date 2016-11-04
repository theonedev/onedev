package com.gitplex.server.search;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.event.depot.DepotDeleted;
import com.gitplex.server.core.event.lifecycle.SystemStopping;
import com.gitplex.server.core.manager.StorageManager;
import com.gitplex.server.search.hit.QueryHit;
import com.gitplex.server.search.query.BlobQuery;
import com.google.common.base.Throwables;
import com.gitplex.commons.hibernate.Transactional;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.commons.loader.Listen;

@Singleton
public class DefaultSearchManager implements SearchManager {

	private final StorageManager storageManager;
	
	private final Dao dao;
	
	private final Map<Long, SearcherManager> searcherManagers = new ConcurrentHashMap<>();
	
	@Inject
	public DefaultSearchManager(Dao dao, StorageManager storageManager) {
		this.dao = dao;
		this.storageManager = storageManager;
	}
	
	@Nullable
	private SearcherManager getSearcherManager(Depot depot) throws InterruptedException {
		try {
			SearcherManager searcherManager = searcherManagers.get(depot.getId());
			if (searcherManager == null) synchronized (searcherManagers) {
				searcherManager = searcherManagers.get(depot.getId());
				if (searcherManager == null) {
					Directory directory = FSDirectory.open(storageManager.getIndexDir(depot));
					if (DirectoryReader.indexExists(directory)) {
						searcherManager = new SearcherManager(directory, null);
						searcherManagers.put(depot.getId(), searcherManager);
					}
				}
			}
			return searcherManager;
		} catch (ClosedByInterruptException e) {
			// catch this exception and convert to normal InterruptedException as 
			// we do not want to throw the original exception to surprise the user
			// when they searches by typing fast (and subsequent typing will cancel 
			// search of previous typing by interrupting previous search thread 
			// which may creating the searcher manager if it does not exist yet
			throw new InterruptedException();
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public List<QueryHit> search(Depot depot, ObjectId commit, final BlobQuery query) 
			throws InterruptedException {
		final List<QueryHit> hits = new ArrayList<>();

		SearcherManager searcherManager = getSearcherManager(depot);
		if (searcherManager != null) {
			try {
				final IndexSearcher searcher = searcherManager.acquire();
				try {
					try (RevWalk revWalk = new RevWalk(depot.getRepository())){
						final RevTree revTree = revWalk.parseCommit(commit).getTree();
						final Set<String> checkedBlobPaths = new HashSet<>();
						
						searcher.search(query.asLuceneQuery(), new Collector() {
	
							private AtomicReaderContext context;
							
							@Override
							public void setScorer(Scorer scorer) throws IOException {
							}
	
							@Override
							public void collect(int doc) throws IOException {
								if (hits.size() < query.getCount() && !Thread.currentThread().isInterrupted()) {
									BinaryDocValues cachedBlobPaths = FieldCache.DEFAULT.getTerms(
											context.reader(), FieldConstants.BLOB_PATH.name(), false);
									String blobPath = cachedBlobPaths.get(doc).utf8ToString();
									
									if (!checkedBlobPaths.contains(blobPath)) {
										TreeWalk treeWalk = TreeWalk.forPath(depot.getRepository(), blobPath, revTree);									
										if (treeWalk != null) 
											query.collect(treeWalk, hits);
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
					}
				} finally {
					searcherManager.release(searcher);
				}
			} catch (IOException e) {
				throw Throwables.propagate(e);
			}
		}
		if (Thread.interrupted())
			throw new InterruptedException();

		Collections.sort(hits);
		
		return hits;
	}

	@Listen
	public void on(CommitIndexed event) {
		try {
			getSearcherManager(event.getDepot()).maybeRefresh();
		} catch (InterruptedException | IOException e) {
			Throwables.propagate(e);
		}
	}

	@Transactional
	@Listen
	public void on(DepotDeleted event) {
		Long depotId = event.getDepot().getId();
		dao.doAfterCommit(new Runnable() {

			@Override
			public void run() {
				synchronized (searcherManagers) {
					SearcherManager searcherManager = searcherManagers.get(depotId);
					if (searcherManager != null) {
						try {
							searcherManager.close();
						} catch (IOException e) {
							Throwables.propagate(e);
						}
						searcherManagers.remove(depotId);
					}
				}
			}
			
		});
	}

	@Listen
	public void on(SystemStopping event) {
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

}
