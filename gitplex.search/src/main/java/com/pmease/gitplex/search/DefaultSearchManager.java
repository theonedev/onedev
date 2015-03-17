package com.pmease.gitplex.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.lang.Analyzers;
import com.pmease.commons.util.Charsets;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.Repository;

@Singleton
public class DefaultSearchManager implements SearchManager {

	private final StorageManager storageManager;
	
	private final Analyzers analyzers;
	
	@Inject
	public DefaultSearchManager(EventBus eventBus, StorageManager storageManager, Analyzers analyzers) {
		eventBus.register(this);
		this.storageManager = storageManager;
		this.analyzers = analyzers;
	}
	
	@Override
	public List<SearchHit> search(Repository repository, String commitHash,
			Query query, SearchHit after, int count) {
		Preconditions.checkArgument(GitUtils.isHash(commitHash));
		File indexDir = storageManager.getIndexDir(repository);
		
		try (Directory directory = FSDirectory.open(indexDir)) {
			if (DirectoryReader.indexExists(directory)) {
				try (IndexReader reader = DirectoryReader.open(directory)) {
					final FileRepository repo = new FileRepository(repository.git().repoDir());
					try {
						final RevTree revTree = new RevWalk(repo).parseCommit(repo.resolve(commitHash)).getTree();
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
									TreeWalk treeWalk = TreeWalk.forPath(repo, blobPath, revTree);									
									
									if (treeWalk == null) 
										blobHashOfCommit = "";
									else 
										blobHashOfCommit = treeWalk.getObjectId(0).name();
									
									blobHashes.put(blobPath, blobHashOfCommit);
								}
								if (blobHashOfCommit.equals(blobHash)) {
									byte[] bytes = repo.open(ObjectId.fromString(blobHash)).getCachedBytes();
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
						repo.close();
					}					
				}
			} else {
				return new ArrayList<>();
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

}
