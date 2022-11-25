package io.onedev.server.search.entitytext;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.da.DanishAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.fi.FinnishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.hu.HungarianAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexFormatTooOldException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.WordUtils;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.ProjectBelonging;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.util.concurrent.BatchWorkManager;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;
public abstract class ProjectTextManager<T extends ProjectBelonging> implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(ProjectTextManager.class);

	private static final String FIELD_TYPE = "type";

	private static final String FIELD_INDEX_VERSION = "indexVersion";

	private static final String FIELD_LAST_ENTITY_ID = "lastEntityId";

	private static final String FIELD_ENTITY_ID = "entityId";

	protected static final String FIELD_PROJECT_ID = "projectId";
	
	private static final int INDEXING_PRIORITY = 20;

	private static final int BATCH_SIZE = 5000;

	private static final CharArraySet STOP_WORDS = new CharArraySet(1000, false);

	static {
		STOP_WORDS.addAll(EnglishAnalyzer.getDefaultStopSet());
		STOP_WORDS.addAll(FrenchAnalyzer.getDefaultStopSet());
		STOP_WORDS.addAll(GermanAnalyzer.getDefaultStopSet());
		STOP_WORDS.addAll(DutchAnalyzer.getDefaultStopSet());
		STOP_WORDS.addAll(ItalianAnalyzer.getDefaultStopSet());
		STOP_WORDS.addAll(DanishAnalyzer.getDefaultStopSet());
		STOP_WORDS.addAll(FinnishAnalyzer.getDefaultStopSet());
		STOP_WORDS.addAll(HungarianAnalyzer.getDefaultStopSet());
		STOP_WORDS.addAll(NorwegianAnalyzer.getDefaultStopSet());
		STOP_WORDS.addAll(PortugueseAnalyzer.getDefaultStopSet());
		STOP_WORDS.addAll(RussianAnalyzer.getDefaultStopSet());
		STOP_WORDS.addAll(SpanishAnalyzer.getDefaultStopSet());
		STOP_WORDS.addAll(SwedishAnalyzer.getDefaultStopSet());
		STOP_WORDS.addAll(SmartChineseAnalyzer.getDefaultStopSet());

		try {
			STOP_WORDS.addAll(WordlistLoader.getSnowballWordSet(IOUtils.getDecodingReader(
					SnowballFilter.class, "english_stop.txt", StandardCharsets.UTF_8)));
		    STOP_WORDS.addAll(WordlistLoader.getWordSet(IOUtils.getDecodingReader(
		    		ProjectTextManager.class, "chinese_stop.txt", StandardCharsets.UTF_8)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private final Class<T> entityClass;

	private final Dao dao;

	private final StorageManager storageManager;

	private final BatchWorkManager batchWorkManager;

	protected final TransactionManager transactionManager;
	
	protected final ProjectManager projectManager;
	
	private final ClusterManager clusterManager;

	private volatile SearcherManager searcherManager;
	
	@SuppressWarnings("unchecked")
	public ProjectTextManager(Dao dao, StorageManager storageManager, BatchWorkManager batchWorkManager,
			TransactionManager transactionManager, ProjectManager projectManager, 
			ClusterManager clusterManager) {
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(ProjectTextManager.class, getClass());
		if (typeArguments.size() == 1 && AbstractEntity.class.isAssignableFrom(typeArguments.get(0))) {
			entityClass = (Class<T>) typeArguments.get(0);
		} else {
			throw new RuntimeException("Super class of entity index manager must "
					+ "be EntityIndexManager and must realize the type argument <T>");
		}
		this.dao = dao;
		this.storageManager = storageManager;
		this.batchWorkManager = batchWorkManager;
		this.transactionManager = transactionManager;
		this.projectManager = projectManager;
		this.clusterManager = clusterManager;
	}

	protected TermQuery getTermQuery(String name, String value) {
		return new TermQuery(getTerm(name, value));
	}

	protected Term getTerm(String name, String value) {
		return new Term(name, value);
	}

	private String getIndexName() {
		return WordUtils.uncamel(entityClass.getSimpleName()).replace(" ", "_").toLowerCase();
	}

	private File getIndexDir() {
		return new File(storageManager.getIndexDir(), getIndexName());
	}

	@Sessional
	@Listen
	public void on(SystemStarted event) {
		File indexDir = getIndexDir();
		FileUtils.createDir(indexDir);
		try {
			Directory directory = FSDirectory.open(indexDir.toPath());
			int indexVersion = -1;
			if (DirectoryReader.indexExists(directory)) {
				try (IndexReader reader = DirectoryReader.open(directory)) {
					IndexSearcher searcher = new IndexSearcher(reader);
					indexVersion = getIndexVersion(searcher);
				} catch (IndexFormatTooOldException e) {
				}
			}
			if (indexVersion != getIndexVersion()) {
				FileUtils.cleanDir(indexDir);
				doWithWriter(new WriterRunnable() {

					@Override
					public void run(IndexWriter writer) throws IOException {
						Document document = new Document();
						document.add(new StringField(FIELD_TYPE, FIELD_INDEX_VERSION, Store.NO));
						document.add(new StoredField(FIELD_INDEX_VERSION, String.valueOf(getIndexVersion())));
						writer.updateDocument(getTerm(FIELD_TYPE, FIELD_INDEX_VERSION), document);
					}

				});
			}
			searcherManager = new SearcherManager(directory, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		batchWorkManager.submit(getBatchWorker(), new IndexWork(INDEXING_PRIORITY, null));
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

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (entityClass.isAssignableFrom(event.getEntity().getClass())) {
			ProjectBelonging projectBelonging = ((ProjectBelonging)event.getEntity());
			Long entityId;
			if (!event.isNew())
				entityId = event.getEntity().getId();
			else
				entityId = null;
			Long projectId = projectBelonging.getProject().getId();
			UUID projectServerUUID = projectManager.getStorageServerUUID(projectId, true);
			
			UUID oldProjectServerUUID;
			if (projectBelonging.getOldVersion() != null) {
				oldProjectServerUUID = projectManager.getStorageServerUUID(
						projectBelonging.getOldVersion().getProjectId(), true);
			} else {
				oldProjectServerUUID = null;
			}
			transactionManager.runAfterCommit(new ClusterRunnable() {

				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					if (oldProjectServerUUID != null && !oldProjectServerUUID.equals(projectServerUUID)) {
						clusterManager.submitToServer(oldProjectServerUUID, new ClusterTask<Void>() {

							private static final long serialVersionUID = 1L;

							@Override
							public Void call() throws Exception {
								doWithWriter(new WriterRunnable() {

									@Override
									public void run(IndexWriter writer) throws IOException {
										writer.deleteDocuments(getTerm(FIELD_ENTITY_ID, String.valueOf(entityId)));
									}

								});
								return null;
							}
							
						});
					}
					clusterManager.submitToServer(projectServerUUID, new ClusterTask<Void>() {

						private static final long serialVersionUID = 1L;

						@Override
						public Void call() throws Exception {
							batchWorkManager.submit(getBatchWorker(), new IndexWork(INDEXING_PRIORITY, entityId));
							return null;
						}
						
					});
				}

			});
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (entityClass.isAssignableFrom(event.getEntity().getClass())) {
			Long entityId = event.getEntity().getId();
			Long projectId = ((ProjectBelonging)event.getEntity()).getProject().getId();
			transactionManager.runAfterCommit(new ClusterRunnable() {

				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					projectManager.submitToProjectServer(projectId, new ClusterTask<Void>() {

						private static final long serialVersionUID = 1L;

						@Override
						public Void call() throws Exception {
							doWithWriter(new WriterRunnable() {

								@Override
								public void run(IndexWriter writer) throws IOException {
									writer.deleteDocuments(getTerm(FIELD_ENTITY_ID, String.valueOf(entityId)));
								}

							});
							return null;
						}
						
					});
				}

			});
		} else if (event.getEntity() instanceof Project) {
			Long projectId = event.getEntity().getId();
			UUID storageServerUUID = projectManager.getStorageServerUUID(projectId, false);
			transactionManager.runAfterCommit(new ClusterRunnable() {

				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					if (storageServerUUID != null) {
						clusterManager.submitToServer(storageServerUUID, new ClusterTask<Void>() {
	
							private static final long serialVersionUID = 1L;
	
							@Override
							public Void call() throws Exception {
								doWithWriter(new WriterRunnable() {
	
									@Override
									public void run(IndexWriter writer) throws IOException {
										writer.deleteDocuments(LongPoint.newExactQuery(FIELD_PROJECT_ID, projectId));
									}
									
								});
								return null;
							}
							
						});
					}
				}
				
			});
		}

	}
	
	protected void doWithWriter(WriterRunnable runnable) {
		File indexDir = getIndexDir();
		try (Directory directory = FSDirectory.open(indexDir.toPath()); Analyzer analyzer = newAnalyzer()) {
			IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
			writerConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
			try (IndexWriter writer = new IndexWriter(directory, writerConfig)) {
				try {
					runnable.run(writer);
					writer.commit();
				} catch (Exception e) {
					writer.rollback();
					throw ExceptionUtils.unchecked(e);
				} finally {
					if (searcherManager != null)
						searcherManager.maybeRefresh();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected Analyzer newAnalyzer() {
		return new SmartChineseAnalyzer(STOP_WORDS);
	}

	private int getIndexVersion(IndexSearcher searcher) throws IOException {
		TopDocs topDocs = searcher.search(getTermQuery(FIELD_TYPE, FIELD_INDEX_VERSION), 1);
		if (topDocs.scoreDocs.length != 0) {
			Document doc = searcher.doc(topDocs.scoreDocs[0].doc);
			return Integer.parseInt(doc.get(FIELD_INDEX_VERSION));
		} else {
			return -1;
		}
	}

	private Long getLastEntityId(IndexSearcher searcher) throws IOException {
		TopDocs topDocs = searcher.search(getTermQuery(FIELD_TYPE, FIELD_LAST_ENTITY_ID), 1);
		if (topDocs.scoreDocs.length != 0) {
			Document doc = searcher.doc(topDocs.scoreDocs[0].doc);
			return Long.valueOf(doc.get(FIELD_LAST_ENTITY_ID));
		} else {
			return 0L;
		}
	}
	
	private BatchWorker getBatchWorker() {
		return new BatchWorker("index" + entityClass.getSimpleName()) {

			@Override
			public void doWorks(Collection<Prioritized> works) {
				String entityName = WordUtils.uncamel(entityClass.getSimpleName()).toLowerCase();
				logger.debug("Indexing {}s...", entityName);

				boolean checkNewEntities = false;
				Collection<Long> entityIds = new HashSet<>();
				for (Prioritized work : works) {
					Long entityId = ((IndexWork) work).getEntityId();
					if (entityId != null)
						entityIds.add(entityId);
					else
						checkNewEntities = true;
				}

				index(entityIds);

				if (checkNewEntities) {
					// do the work batch by batch to avoid consuming too much memory
					while (index());
				}

				logger.debug("Indexed {}s", entityName);
			}

		};
	}

	@Sessional
	protected void index(Collection<Long> entityIds) {
		doWithWriter(new WriterRunnable() {

			@Override
			public void run(IndexWriter writer) throws IOException {
				for (Long entityId : entityIds) {
					T entity = dao.load(entityClass, entityId);
					index(writer, entity);
				}
			}

		});
	}

	@Sessional
	protected boolean index() {
		File indexDir = getIndexDir();
		try (Directory directory = FSDirectory.open(indexDir.toPath())) {
			Long lastEntityId = 0L;
			if (DirectoryReader.indexExists(directory)) {
				try (IndexReader reader = DirectoryReader.open(directory)) {
					lastEntityId = getLastEntityId(new IndexSearcher(reader));
				}
			}
			List<T> unprocessedEntities = dao.queryAfter(entityClass, lastEntityId, BATCH_SIZE);

			doWithWriter(new WriterRunnable() {

				@Override
				public void run(IndexWriter writer) throws IOException {
					T lastEntity = null;
					for (T entity: unprocessedEntities) {
						if (clusterManager.getLocalServerUUID().equals(projectManager.getStorageServerUUID(entity.getProject().getId(), false))) 
							index(writer, entity);
						lastEntity = entity;
					}

					if (lastEntity != null) {
						Document document = new Document();
						document.add(new StringField(FIELD_TYPE, FIELD_LAST_ENTITY_ID, Store.NO));
						document.add(new StoredField(FIELD_LAST_ENTITY_ID, String.valueOf(lastEntity.getId())));
						writer.updateDocument(getTerm(FIELD_TYPE, FIELD_LAST_ENTITY_ID), document);
					}
				}

			});

			return unprocessedEntities.size() == BATCH_SIZE;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Query parse(String queryString) {
		try {
			QueryParser parser = new QueryParser("", newAnalyzer()) {

				@Override
				protected Query getRangeQuery(String field, String part1, String part2, boolean startInclusive,
						boolean endInclusive) throws ParseException {
					return LongPoint.newRangeQuery(field, Long.parseLong(part1), Long.parseLong(part2));
				}
				
			};
			parser.setAllowLeadingWildcard(true);
			return parser.parse(queryString);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected long count(Query query) {
		String queryString = query.toString();
		return clusterManager.runOnAllServers(new ClusterTask<Long>() {

			private static final long serialVersionUID = 1L;
			@Override
			public Long call() throws Exception {
				if (searcherManager != null) {
					try {
						IndexSearcher indexSearcher = searcherManager.acquire();
						try {
							TotalHitCountCollector collector = new TotalHitCountCollector();
							indexSearcher.search(parse(queryString), collector);
							return (long) collector.getTotalHits();
						} finally {
							searcherManager.release(indexSearcher);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				} else {
					return 0L;
				}
			}
			
		}).values().stream().reduce(0L, Long::sum);
	}

	protected List<T> search(Query query, int firstResult, int maxResults) {
		String queryString = query.toString();
		Map<Long, Float> entityScores = new HashMap<>();
		for (var entry: clusterManager.runOnAllServers(new ClusterTask<Map<Long, Float>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Map<Long, Float> call() throws Exception {
				if (searcherManager != null) {
					try {
						IndexSearcher indexSearcher = searcherManager.acquire();
						try {
							Map<Long, Float> entityScores = new HashMap<>();
							TopDocs topDocs = indexSearcher.search(parse(queryString), firstResult + maxResults);
							for (var scoreDoc: topDocs.scoreDocs) {
								Document doc = indexSearcher.doc(scoreDoc.doc);
								entityScores.put(Long.valueOf(doc.get(FIELD_ENTITY_ID)), scoreDoc.score);
							}
							return entityScores;
						} finally {
							searcherManager.release(indexSearcher);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				} else {
					return new HashMap<>();
				}
			}
			
		}).entrySet()) {
			entityScores.putAll(entry.getValue());
		}
		
		List<Long> entityIds = new ArrayList<>(entityScores.keySet());
		Collections.sort(entityIds, new Comparator<>() {

			@Override
			public int compare(Object o1, Object o2) {
				if (entityScores.get(o1) < entityScores.get(o2))
					return 1;
				else
					return -1;
			}
			
		});
		
		if (firstResult < entityIds.size()) {
			EntityCriteria<T> criteria = EntityCriteria.of(entityClass);
			criteria.add(Restrictions.in(
					AbstractEntity.PROP_ID, 
					entityIds.subList(firstResult, Math.min(firstResult + maxResults, entityIds.size()))));
			
			List<T> entities = dao.query(criteria);
			Collections.sort(entities, new Comparator<T>() {

				@Override
				public int compare(T o1, T o2) {
					return entityIds.indexOf(o1.getId()) - entityIds.indexOf(o2.getId());
				}

			});
			return entities;
		} else {
			return new ArrayList<>();
		}
	}

	private void index(IndexWriter writer, T entity) throws IOException {
		Document document = new Document();
		document.add(new StringField(FIELD_ENTITY_ID, String.valueOf(entity.getId()), Store.YES));
		document.add(new LongPoint(FIELD_PROJECT_ID, entity.getProject().getId()));
		addFields(document, entity);
		writer.updateDocument(getTerm(FIELD_ENTITY_ID, String.valueOf(entity.getId())), document);
	}

	protected abstract int getIndexVersion();

	protected abstract void addFields(Document document, T entity);

	private static class IndexWork extends Prioritized {

		private final Long entityId;

		public IndexWork(int priority, @Nullable Long entityId) {
			super(priority);
			this.entityId = entityId;
		}

		@Nullable
		public Long getEntityId() {
			return entityId;
		}

	}

	protected static interface WriterRunnable {

		abstract void run(IndexWriter writer) throws IOException;

	}
	
}
