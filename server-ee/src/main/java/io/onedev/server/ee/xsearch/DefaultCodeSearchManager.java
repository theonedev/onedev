package io.onedev.server.ee.xsearch;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.ee.xsearch.hit.QueryHit;
import io.onedev.server.ee.xsearch.query.BlobQuery;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.search.code.query.TooGeneralQueryException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class DefaultCodeSearchManager implements CodeSearchManager {

	private volatile SearcherManager searcherManager;
	
	private final ClusterManager clusterManager;
	
	@Inject
	public DefaultCodeSearchManager(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	private File getIndexDir() {
		return new File(OneDev.getIndexDir(), "code");
	}
	
	@Nullable
	private SearcherManager getSearcherManager() {
		try {
			SearcherManager searcherManagerCopy = searcherManager;
			if (searcherManagerCopy == null) synchronized (this) {
				searcherManagerCopy = searcherManager;
				if (searcherManagerCopy == null && getIndexDir().exists()) {
					Directory directory = FSDirectory.open(getIndexDir().toPath());
					if (DirectoryReader.indexExists(directory)) {
						searcherManagerCopy = new SearcherManager(directory, null);
						searcherManager = searcherManagerCopy;
					}
				}
			}
			return searcherManagerCopy;
		} catch (IOException e) {
			throw ExceptionUtils.unchecked(e);
		}
	}

	@Override
	public List<QueryHit> search(BlobQuery query) throws InterruptedException, TooGeneralQueryException {
		List<QueryHit> hits = new ArrayList<>();
		for (var entry: clusterManager.runOnAllServers((ClusterTask<List<QueryHit>>) () -> {
			var searcherManager = getSearcherManager();
			if (searcherManager != null) {
				try {
					IndexSearcher searcher = searcherManager.acquire();
					try {
						var innerHits = new ArrayList<QueryHit>();
						searcher.search(query.asLuceneQuery(), new SimpleCollector() {

							private LeafReaderContext context;

							@SuppressWarnings("unchecked")
							@Override
							public void collect(int doc) throws IOException {
								Document document = searcher.doc(context.docBase+doc);
								if (innerHits.size() < query.getCount()) 
									query.collect(document, innerHits);
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
						return innerHits;
					} finally {
						searcherManager.release(searcher);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				return new ArrayList<>();
			}
		}).values()) {
			hits.addAll(entry);
		}
		
		if (hits.size() > query.getCount())
			hits = hits.subList(0, query.getCount());
		return hits;
	}

	@Override
	public void indexUpdated() {
		if (searcherManager != null) {
			try {
				searcherManager.maybeRefresh();
			} catch (IOException e) {
				throw ExceptionUtils.unchecked(e);
			}
		}
	}

	@Listen
	public void on(SystemStopping event) {
		if (searcherManager != null) {
			try {
				searcherManager.close();
			} catch (IOException e) {
				throw ExceptionUtils.unchecked(e);
			}
		}
	}
	
}
