package io.onedev.server.search.code;

import static io.onedev.server.search.code.FieldConstants.BLOB_HASH;
import static io.onedev.server.search.code.FieldConstants.BLOB_INDEX_VERSION;
import static io.onedev.server.search.code.FieldConstants.BLOB_PATH;
import static io.onedev.server.search.code.FieldConstants.BLOB_SYMBOL_LIST;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.SerializationUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.jsymbol.Symbol;
import io.onedev.commons.jsymbol.SymbolExtractorRegistry;
import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.search.code.query.BlobQuery;
import io.onedev.server.storage.StorageManager;

@Singleton
public class DefaultSearchManager implements SearchManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultSearchManager.class);
	
	private final StorageManager storageManager;
	
	private final Map<Long, SearcherManager> searcherManagers = new ConcurrentHashMap<>();
	
	private final IndexManager indexManager;
	
	@Inject
	public DefaultSearchManager(StorageManager storageManager, IndexManager indexManager) {
		this.storageManager = storageManager;
		this.indexManager = indexManager;
	}
	
	@Nullable
	private SearcherManager getSearcherManager(Project project) throws InterruptedException {
		try {
			SearcherManager searcherManager = searcherManagers.get(project.getId());
			if (searcherManager == null) synchronized (searcherManagers) {
				searcherManager = searcherManagers.get(project.getId());
				if (searcherManager == null) {
					Directory directory = FSDirectory.open(storageManager.getProjectIndexDir(project.getId()).toPath());
					if (DirectoryReader.indexExists(directory)) {
						searcherManager = new SearcherManager(directory, null);
						searcherManagers.put(project.getId(), searcherManager);
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
			throw ExceptionUtils.unchecked(e);
		}
	}
	
	@Override
	public List<QueryHit> search(Project project, ObjectId commit, final BlobQuery query) 
			throws InterruptedException {
		List<QueryHit> hits = new ArrayList<>();

		SearcherManager searcherManager = getSearcherManager(project.getForkRoot());
		if (searcherManager != null) {
			try {
				final IndexSearcher searcher = searcherManager.acquire();
				try {
					try (RevWalk revWalk = new RevWalk(project.getRepository())){
						final RevTree revTree = revWalk.parseCommit(commit).getTree();
						final Set<String> checkedBlobPaths = new HashSet<>();
						
						searcher.search(query.asLuceneQuery(), new SimpleCollector() {
	
							private BinaryDocValues blobPathValues;
							
							@Override
							public void collect(int doc) throws IOException {
								if (hits.size() < query.getCount() && !Thread.currentThread().isInterrupted()) {
									Preconditions.checkState(blobPathValues.advanceExact(doc));
									String blobPath = blobPathValues.binaryValue().utf8ToString();
									
									if (!checkedBlobPaths.contains(blobPath)) {
										TreeWalk treeWalk = TreeWalk.forPath(project.getRepository(), blobPath, revTree);									
										if (treeWalk != null)
											query.collect(searcher, treeWalk, hits);
										checkedBlobPaths.add(blobPath);
									}
								}
							}
	
							@Override
							protected void doSetNextReader(LeafReaderContext context) throws IOException {
								blobPathValues  = context.reader().getBinaryDocValues(FieldConstants.BLOB_PATH.name());
							}

							@Override
							public boolean needsScores() {
								return false;
							}
	
						});
					}
				} finally {
					searcherManager.release(searcher);
				}
			} catch (IOException e) {
				throw ExceptionUtils.unchecked(e);
			}
		}
		if (Thread.interrupted())
			throw new InterruptedException();

		return hits;
	}

	@Override
	public List<Symbol> getSymbols(Project project, ObjectId blobId, String blobPath) {
		try {
			SearcherManager searcherManager = getSearcherManager(project.getForkRoot());
			if (searcherManager != null) {
				try {
					IndexSearcher searcher = searcherManager.acquire();
					try {
						return getSymbols(searcher, blobId, blobPath);
					} finally {
						searcherManager.release(searcher);
					}
				} catch (IOException e) {
					throw ExceptionUtils.unchecked(e);
				}
			} else {
				return null;
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List<Symbol> getSymbols(IndexSearcher searcher, ObjectId blobId, String blobPath) {
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		builder.add(BLOB_HASH.query(blobId.name()), Occur.MUST);
		builder.add(BLOB_PATH.query(blobPath), Occur.MUST);
		
		BooleanQuery query = builder.build();
		
		String indexVersion = indexManager.getIndexVersion(SymbolExtractorRegistry.getExtractor(blobPath));
		AtomicReference<List<Symbol>> symbolsRef = new AtomicReference<>(null);
		if (searcher != null) {
			try {
				searcher.search(query, new SimpleCollector() {

					private LeafReaderContext context;

					@SuppressWarnings("unchecked")
					@Override
					public void collect(int doc) throws IOException {
						Document document = searcher.doc(context.docBase+doc);
						if (indexVersion.equals(document.get(BLOB_INDEX_VERSION.name()))) {
							BytesRef bytesRef = document.getBinaryValue(BLOB_SYMBOL_LIST.name());
							if (bytesRef != null) {
								try {
									symbolsRef.set((List<Symbol>) SerializationUtils.deserialize(bytesRef.bytes));
								} catch (Exception e) {
									logger.error("Error deserializing symbols", e);
								}
							}
						}
					}

					@Override
					protected void doSetNextReader(LeafReaderContext context) throws IOException {
						this.context = context;
					}

					@Override
					public boolean needsScores() {
						return false;
					}
					
				});
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		return symbolsRef.get();
	}
	
	@Listen
	public void on(CommitIndexed event) {
		try {
			SearcherManager searcherManager = getSearcherManager(event.getProject().getForkRoot()); 
			if (searcherManager != null)
				searcherManager.maybeRefresh();
		} catch (InterruptedException | IOException e) {
			throw ExceptionUtils.unchecked(e);
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			synchronized (searcherManagers) {
				Long projectId = event.getEntity().getId();						
				SearcherManager searcherManager = searcherManagers.remove(projectId);
				if (searcherManager != null) {
					try {
						searcherManager.close();
					} catch (IOException e) {
						throw ExceptionUtils.unchecked(e);
					}
				}
			}
		}
	}

	@Listen
	public void on(SystemStopping event) {
		synchronized (searcherManagers) {
			for (SearcherManager searcherManager: searcherManagers.values()) {
				try {
					searcherManager.close();
				} catch (IOException e) {
					throw ExceptionUtils.unchecked(e);
				}
			}
			searcherManagers.clear();
		}
	}

}
