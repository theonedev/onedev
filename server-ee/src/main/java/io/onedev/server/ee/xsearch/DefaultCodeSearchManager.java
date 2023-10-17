package io.onedev.server.ee.xsearch;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.ee.NoSubscriptionException;
import io.onedev.server.ee.xsearch.match.BlobMatch;
import io.onedev.server.ee.xsearch.query.BlobQuery;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStopped;
import io.onedev.server.search.code.query.TooGeneralQueryException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.util.Map.Entry.comparingByKey;

@Singleton
public class DefaultCodeSearchManager implements CodeSearchManager, Serializable {
	
	private final ClusterManager clusterManager;
	
	private final ProjectManager projectManager;
	
	private final SubscriptionManager subscriptionManager;
	
	private volatile SearcherManager searcherManager;
	
	@Inject
	public DefaultCodeSearchManager(ClusterManager clusterManager, ProjectManager projectManager, 
									SubscriptionManager subscriptionManager) {
		this.clusterManager = clusterManager;
		this.projectManager = projectManager;
		this.subscriptionManager = subscriptionManager;
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
	public List<BlobMatch> search(BlobQuery query) throws TooGeneralQueryException {
		if (subscriptionManager.isSubscriptionActive()) {
			List<BlobMatch> matches = new ArrayList<>();
			var projectIdsByServer = projectManager.groupByActiveServers(query.getApplicableProjectIds());

			var entries = new ArrayList<>(clusterManager.runOnServers(projectIdsByServer.keySet(), (ClusterTask<List<BlobMatch>>) () -> {
				var searcherManager = getSearcherManager();
				if (searcherManager != null) {
					try {
						IndexSearcher searcher = searcherManager.acquire();
						try {
							var innerMatches = new ArrayList<BlobMatch>();
							var projectIds = projectIdsByServer.get(clusterManager.getLocalServerAddress());
							searcher.search(query.asLuceneQuery(projectIds), new SimpleCollector() {

								private LeafReaderContext context;

								@Override
								public void collect(int doc) throws IOException {
									Document document = searcher.doc(context.docBase + doc);
									if (innerMatches.size() < query.getCount()) {
										var match = query.matches(document);
										if (match != null)
											innerMatches.add(match);
									} else {
										throw new CollectionTerminatedException();
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
							return innerMatches;
						} finally {
							searcherManager.release(searcher);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				} else {
					return new ArrayList<>();
				}
			}).entrySet());

			entries.sort(comparingByKey());
			for (var result : entries)
				matches.addAll(result.getValue());

			if (matches.size() > query.getCount())
				matches = matches.subList(0, query.getCount());
			return matches;
		} else {
			throw new NoSubscriptionException();
		}
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
	public void on(SystemStopped event) {
		if (searcherManager != null) {
			try {
				searcherManager.close();
			} catch (IOException e) {
				throw ExceptionUtils.unchecked(e);
			}
		}
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(CodeSearchManager.class);
	}
	
}
