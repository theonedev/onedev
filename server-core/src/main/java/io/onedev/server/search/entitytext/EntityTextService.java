package io.onedev.server.search.entitytext;

import static com.google.common.collect.Lists.partition;
import static io.onedev.server.util.criteria.Criteria.forManyValues;
import static java.lang.Long.valueOf;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;
import static org.apache.lucene.document.Field.Store.YES;
import static org.apache.lucene.document.LongPoint.newExactQuery;
import static org.apache.lucene.search.BooleanClause.Occur.MUST;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.WordlistLoader;
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
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.WordUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.service.ProjectService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.ActiveServerChanged;
import io.onedev.server.event.project.ProjectDeleted;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.event.system.SystemStopped;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.support.EntityTouch;
import io.onedev.server.model.support.ProjectBelonging;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.util.concurrent.BatchWorkExecutionService;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;
import io.onedev.server.util.lucene.BooleanQueryBuilder;
import io.onedev.server.util.lucene.LuceneUtils;

public abstract class EntityTextService<T extends ProjectBelonging> implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(EntityTextService.class);

	private static final String FIELD_TYPE = "type";
	
	private static final String TYPE_META = "meta";
	
	private static final String FIELD_VERSION = "version";

	private static final String FIELD_TOUCH_ID = "touchId";
	
	protected static final String FIELD_ENTITY_ID = "entityId";

	protected static final String FIELD_PROJECT_ID = "projectId";
	
	protected static final int UPDATE_PRIORITY = 100;

	protected static final int CHECK_PRIORITY = 200;
	
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

		try {
		    STOP_WORDS.addAll(WordlistLoader.getWordSet(IOUtils.getDecodingReader(
		    		EntityTextService.class, "chinese_stop.txt", StandardCharsets.UTF_8)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private final Class<T> entityClass;

	@Inject
	protected Dao dao;

	@Inject
	private BatchWorkExecutionService batchWorkExecutionService;

	@Inject
	protected TransactionService transactionService;
	
	@Inject
	protected ProjectService projectService;
	
	@Inject
	protected ClusterService clusterService;
	
	@Inject
	private SessionService sessionService;

	private volatile SearcherManager searcherManager;
	
	@SuppressWarnings("unchecked")
	public EntityTextService() {
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(EntityTextService.class, getClass());
		if (typeArguments.size() == 1 && AbstractEntity.class.isAssignableFrom(typeArguments.get(0))) {
			entityClass = (Class<T>) typeArguments.get(0);
		} else {
			throw new RuntimeException("Super class of entity index manager must "
					+ "be EntityIndexManager and must realize the type argument <T>");
		}
	}

	@Listen
	public void on(SystemStarting event) {
		File indexDir = getIndexDir();
		FileUtils.createDir(indexDir);
		try {
			Directory directory = FSDirectory.open(indexDir.toPath());
			int indexVersion = -1;
			if (DirectoryReader.indexExists(directory)) {
				try (IndexReader reader = DirectoryReader.open(directory)) {
					IndexSearcher searcher = new IndexSearcher(reader);
					Document doc = readMetaDoc(searcher, 0L);
					if (doc != null)
						indexVersion = Integer.parseInt(doc.get(FIELD_VERSION));
				} catch (IndexFormatTooOldException ignored) {
				}
			}
			if (indexVersion != getIndexVersion()) {
				FileUtils.cleanDir(indexDir);
				callWithWriter(writer -> {
					Document document = new Document();
					document.add(new StoredField(FIELD_VERSION, String.valueOf(getIndexVersion())));
					updateMetaDoc(writer, 0L, document);
					return null;
				});
			}
			searcherManager = new SearcherManager(directory, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
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

	@Sessional
	@Listen
	public void on(ProjectDeleted event) {
		Long projectId = event.getProjectId();
		clusterService.submitToAllServers(() -> {
			callWithWriter(writer -> {
				try {
					return writer.deleteDocuments(newExactQuery(FIELD_PROJECT_ID, projectId));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			return null;
		});
	}
	
	@Listen
	public void on(ActiveServerChanged event) {
		for(var projectId: event.getProjectIds()) 	
			requestToIndex(projectId, CHECK_PRIORITY);
	}
	
	protected <R> R callWithSearcher(Function<IndexSearcher, R> func) {
		return LuceneUtils.callWithSearcher(getIndexDir(), func);
	}
	
	protected synchronized <R> R callWithWriter(Function<IndexWriter, R> func) {
		try (Analyzer analyzer = newAnalyzer()) {
			return LuceneUtils.callWithWriter(getIndexDir(), analyzer, func);
		} finally {
			if (searcherManager != null) {
				try {
					searcherManager.maybeRefresh();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	protected void requestToIndex(Long projectId, int priority) {
		var batchWorker = new BatchWorker("project-" + projectId + "-index-entity-" + entityClass.getSimpleName()) {

			@Override
			public void doWorks(List<Prioritized> works) {
				String entityName = WordUtils.uncamel(entityClass.getSimpleName()).toLowerCase();
				String projectPath = projectService.findFacadeById(projectId).getPath();
				logger.debug("Indexing {} (project: {})...", entityName, projectPath);

				var touchInfo = callWithSearcher(searcher -> {
					var touchId = 0L;
					var metaDoc = readMetaDoc(searcher, projectId);
					if (metaDoc != null)
						touchId = metaDoc.getField(FIELD_TOUCH_ID).numericValue().longValue();
					
					var touches = queryTouchesAfter(projectId, touchId);
					if (!touches.isEmpty()) {
						var entityIds = new HashSet<Long>();
						var maxTouchId = 0L;
						for (var touch: touches) {
							entityIds.add(touch.getEntityId());
							if (touch.getId() > maxTouchId)
								maxTouchId = touch.getId();
						}
						return new EntityTouchInfo(maxTouchId, entityIds);
					} else {
						return null;
					}
				});
				
				callWithWriter(writer -> {
					if (touchInfo != null) {
						for (var partition: partition(new ArrayList<>(touchInfo.getEntityIds()), BATCH_SIZE)) {
							sessionService.run(() -> {
								try {
									for (var entityId: partition) {
										var queryBuilder = new BooleanQueryBuilder();
										queryBuilder.add(newExactQuery(FIELD_PROJECT_ID, projectId), MUST);
										queryBuilder.add(getTermQuery(FIELD_ENTITY_ID, String.valueOf(entityId)), MUST);											
										writer.deleteDocuments(queryBuilder.build());
										var entity = dao.get(entityClass, entityId);
										if (entity != null && entity.getProject().getId().equals(projectId)) {
											Document entityDoc = new Document();
											entityDoc.add(new LongPoint(FIELD_PROJECT_ID, entity.getProject().getId()));
											entityDoc.add(new StringField(FIELD_ENTITY_ID, String.valueOf(entityId), YES));
											addFields(entityDoc, entity);
											writer.addDocument(entityDoc);
										}
									}
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							});
						}
						var metaDoc = new Document();
						metaDoc.add(new StoredField(FIELD_TOUCH_ID, touchInfo.getTouchId()));
						updateMetaDoc(writer, projectId, metaDoc);
					}
					return null;
				});
				logger.debug("Indexed {} (project: {})", entityName, projectPath);
			}

		};		
		batchWorkExecutionService.submit(batchWorker, new IndexWork(priority));
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
	
	private Query buildQuery(Map<String, Collection<Long>> projectIdsByServer, String contentQueryString) {
		var queryBuilder = new BooleanQueryBuilder();
		var allIds = projectService.getIds();
		var projectIds = projectIdsByServer.get(clusterService.getLocalServerAddress());
		queryBuilder.add(forManyValues(FIELD_PROJECT_ID, projectIds, allIds), MUST);
		queryBuilder.add(parse(contentQueryString), MUST);
		return queryBuilder.build();
	}
	
	protected List<Long> search(EntityTextQuery query, int count) {
		String contentQueryString = query.getContentQuery().toString();
		var projectIdsByServer = projectService.groupByActiveServers(query.getApplicableProjectIds());
		Map<Long, Float> entityScores = new HashMap<>();
		for (var entry : clusterService.runOnServers(projectIdsByServer.keySet(), (ClusterTask<Map<Long, Float>>) () -> {
			if (searcherManager != null) {
				try {
					IndexSearcher searcher = searcherManager.acquire();
					try {
						Map<Long, Float> innerEntityScores = new HashMap<>();
						TopDocs topDocs = searcher.search(buildQuery(projectIdsByServer, contentQueryString), count);
						for (var scoreDoc : topDocs.scoreDocs) {
							Document doc = searcher.doc(scoreDoc.doc);
							innerEntityScores.put(valueOf(doc.get(FIELD_ENTITY_ID)), scoreDoc.score);
						}
						return innerEntityScores;
					} finally {
						searcherManager.release(searcher);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				return new HashMap<>();
			}
		}).entrySet()) {
			entityScores.putAll(entry.getValue());
		}

		List<Map.Entry<Long, Float>> entries = new ArrayList<>(entityScores.entrySet());
		entries.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
		
		if (!entries.isEmpty()) {
			entries = entries.subList(0, min(count, entries.size()));
			return entries.stream().map(Map.Entry::getKey).collect(toList());
		} else {
			return new ArrayList<>();
		}
	}
	
	private String getIndexName() {
		return WordUtils.uncamel(entityClass.getSimpleName()).replace(" ", "_").toLowerCase();
	}

	private File getIndexDir() {
		return new File(OneDev.getIndexDir(), getIndexName());
	}
	
	protected Analyzer newAnalyzer() {
		return new StandardAnalyzer(STOP_WORDS);
	}
	
	protected void updateMetaDoc(IndexWriter writer, Long projectId, Document document) {
		document.add(new StringField(FIELD_TYPE, TYPE_META, Store.NO));
		document.add(new LongPoint(FIELD_PROJECT_ID, projectId));
		try {
			writer.deleteDocuments(newMetaQuery(projectId));
			writer.addDocument(document);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Query newMetaQuery(Long projectId) {
		var queryBuilder = new BooleanQueryBuilder();
		queryBuilder.add(getTermQuery(FIELD_TYPE, TYPE_META), MUST);
		queryBuilder.add(newExactQuery(FIELD_PROJECT_ID, projectId), MUST);
		return queryBuilder.build();
	}
	
	@Nullable
	protected Document readMetaDoc(IndexSearcher searcher, Long projectId) {
		try {
			TopDocs topDocs = searcher.search(newMetaQuery(projectId), 1);
			if (topDocs.scoreDocs.length != 0) 
				return searcher.doc(topDocs.scoreDocs[0].doc);
			else 
				return null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected TermQuery getTermQuery(String name, String value) {
		return new TermQuery(getTerm(name, value));
	}

	protected Term getTerm(String name, String value) {
		return new Term(name, value);
	}

	protected abstract int getIndexVersion();
	
	protected abstract List<? extends EntityTouch> queryTouchesAfter(Long projectId, Long touchId);
	
	protected abstract void addFields(Document entityDoc, T entity);

	private static class IndexWork extends Prioritized {

		public IndexWork(int priority) {
			super(priority);
		}

	}

	private static class EntityTouchInfo {

		private final Long touchId;

		private final Collection<Long> entityIds;

		public EntityTouchInfo(Long touchId, Collection<Long> entityIds) {
			this.touchId = touchId;
			this.entityIds = entityIds;
		}

		public Long getTouchId() {
			return touchId;
		}

		public Collection<Long> getEntityIds() {
			return entityIds;
		}
	}
	
}
