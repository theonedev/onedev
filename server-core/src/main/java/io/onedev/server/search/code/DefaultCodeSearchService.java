package io.onedev.server.search.code;

import static io.onedev.server.search.code.FieldConstants.BLOB_HASH;
import static io.onedev.server.search.code.FieldConstants.BLOB_INDEX_VERSION;
import static io.onedev.server.search.code.FieldConstants.BLOB_PATH;
import static io.onedev.server.search.code.FieldConstants.BLOB_SYMBOL_LIST;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CollectionTerminatedException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.jsymbol.Symbol;
import io.onedev.commons.jsymbol.SymbolExtractorRegistry;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.service.ProjectService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.project.CommitIndexed;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.search.code.hit.SymbolHit;
import io.onedev.server.search.code.query.BlobQuery;
import io.onedev.server.search.code.query.SymbolQuery;

@Singleton
public class DefaultCodeSearchService implements CodeSearchService, Serializable {

	private static final int MAX_BLOB_PATH_QUERY_COUNT = 5;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultCodeSearchService.class);
	
	private final Map<Long, SearcherManager> searcherManagers = new ConcurrentHashMap<>();

	@Inject
	private CodeIndexService indexService;

	@Inject
	private ProjectService projectService;

	@Inject
	private ClusterService clusterService;

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(CodeSearchService.class);
	}
	
	@Nullable
	private SearcherManager getSearcherManager(Long projectId) throws InterruptedException {
		try {
			SearcherManager searcherManager = searcherManagers.get(projectId);
			if (searcherManager == null) synchronized (searcherManagers) {
				searcherManager = searcherManagers.get(projectId);
				if (searcherManager == null) {
					Directory directory = FSDirectory.open(projectService.getIndexDir(projectId).toPath());
					if (DirectoryReader.indexExists(directory)) {
						searcherManager = new SearcherManager(directory, null);
						searcherManagers.put(projectId, searcherManager);
					}
				}
			}
			return searcherManager;
		} catch (ClosedByInterruptException e) {
			// catch this exception and convert to normal InterruptedException as 
			// we do not want to throw the original exception to surprise the user
			// when they search by typing fast (and subsequent typing will cancel 
			// search of previous typing by interrupting previous search thread 
			// which may creating the searcher manager if it does not exist yet
			throw new InterruptedException();
		} catch (IOException e) {
			throw ExceptionUtils.unchecked(e);
		}
	}
	
	@Override
	public List<QueryHit> search(Project project, ObjectId commitId, final BlobQuery query) {
		Long projectId = project.getId();
		return projectService.runOnActiveServer(projectId, new ClusterTask<List<QueryHit>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<QueryHit> call() throws Exception {
				return search(projectId, commitId, query);
			}
			
		});

	}
	
	private List<QueryHit> search(Long projectId, ObjectId commitId, final BlobQuery query) 
			throws InterruptedException {
		List<QueryHit> hits = new ArrayList<>();

		SearcherManager searcherManager = getSearcherManager(projectId);
		if (searcherManager != null) {
			try {
				final IndexSearcher searcher = searcherManager.acquire();
				try {
					Repository repository = projectService.getRepository(projectId);
					try (RevWalk revWalk = new RevWalk(repository)){
						final RevTree revTree = revWalk.parseCommit(commitId).getTree();
						final Set<String> checkedBlobPaths = new HashSet<>();
						
						searcher.search(query.asLuceneQuery(), new SimpleCollector() {
	
							private BinaryDocValues blobPathValues;
							
							@Override
							public void collect(int doc) throws IOException {
								if (hits.size() < query.getCount()) {
									Preconditions.checkState(blobPathValues.advanceExact(doc));
									String blobPath = blobPathValues.binaryValue().utf8ToString();
									
									if (!checkedBlobPaths.contains(blobPath)) {
										TreeWalk treeWalk = TreeWalk.forPath(repository, blobPath, revTree);									
										if (treeWalk != null)
											query.collect(searcher, treeWalk, hits);
										checkedBlobPaths.add(blobPath);
									}
								} else {
									throw new CollectionTerminatedException();
								}
							}
	
							@Override
							protected void doSetNextReader(LeafReaderContext context) throws IOException {
								blobPathValues  = context.reader().getBinaryDocValues(FieldConstants.BLOB_PATH.name());
							}

							@Override
							public ScoreMode scoreMode() {
								return ScoreMode.COMPLETE_NO_SCORES;
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
		Long projectId = project.getId();
		
		// Use Java serialization to maintain symbol parent/child relationship relying on object identity comparison 
		byte[] bytes = projectService.runOnActiveServer(projectId, new ClusterTask<byte[]>() {

			private static final long serialVersionUID = 1L;

			@Override
			public byte[] call() throws Exception {
				try {
					SearcherManager searcherManager = getSearcherManager(projectId);
					if (searcherManager != null) {
						try {
							IndexSearcher searcher = searcherManager.acquire();
							try {
								return SerializationUtils.serialize((Serializable) getSymbols(searcher, blobId, blobPath));
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
			
		});
		if (bytes != null)
			return SerializationUtils.deserialize(bytes);
		else
			return null;
	}
	
	@Override
	public List<Symbol> getSymbols(IndexSearcher searcher, ObjectId blobId, String blobPath) {
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		builder.add(BLOB_HASH.getTermQuery(blobId.name()), Occur.MUST);
		builder.add(BLOB_PATH.getTermQuery(blobPath), Occur.MUST);
		
		BooleanQuery query = builder.build();
		
		String indexVersion = indexService.getIndexVersion(SymbolExtractorRegistry.getExtractor(blobPath));
		AtomicReference<List<Symbol>> symbolsRef = new AtomicReference<>(null);
		if (searcher != null) {
			try {
				searcher.search(query, new SimpleCollector() {

					private LeafReaderContext context;

					@Override
					public void collect(int doc) throws IOException {
						Document document = searcher.doc(context.docBase+doc);
						if (indexVersion.equals(document.get(BLOB_INDEX_VERSION.name()))) {
							BytesRef bytesRef = document.getBinaryValue(BLOB_SYMBOL_LIST.name());
							if (bytesRef != null) {
								try {
									symbolsRef.set(SerializationUtils.deserialize(bytesRef.bytes));
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
					public ScoreMode scoreMode() {
						return ScoreMode.COMPLETE_NO_SCORES;
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
			SearcherManager searcherManager = getSearcherManager(event.getProject().getId()); 
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
			Long projectId = event.getEntity().getId();	
			String activeServer = projectService.getActiveServer(projectId, false);
			if (activeServer != null) {
				clusterService.runOnServer(activeServer, () -> {
					synchronized (searcherManagers) {
						SearcherManager searcherManager = searcherManagers.remove(projectId);
						if (searcherManager != null) {
							try {
								searcherManager.close();
							} catch (IOException e) {
								throw ExceptionUtils.unchecked(e);
							}
						}
					}
					return null;
				});
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

	@Nullable
	@Override
	public SymbolHit findPrimarySymbol(Project project, ObjectId commitId, String symbolFQN, String fqnSeparator) {
		var symbolName = symbolFQN;
		var lastIndex = symbolName.lastIndexOf(fqnSeparator);
		if (lastIndex != -1)
			symbolName = symbolName.substring(lastIndex + 1);

		var query = new SymbolQuery.Builder(symbolName).caseSensitive(true).primary(true).count(MAX_BLOB_PATH_QUERY_COUNT).build();
		SymbolHit found = null;
		for (var hit : search(project, commitId, query)) {
			var symbolHit = (SymbolHit) hit;
			if (symbolFQN.equals(symbolHit.getSymbol().getFQN())) {
				if (found == null) {
					found = symbolHit;
				} else {
					logger.warn("Multiple primary symbols matching: " + symbolFQN);
					found = null;
				}
			}
		}
		return found;
	}

}
