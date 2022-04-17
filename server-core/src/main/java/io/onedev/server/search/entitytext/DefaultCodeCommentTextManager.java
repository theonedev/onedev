package io.onedev.server.search.entitytext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;

import io.onedev.commons.loader.Listen;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.concurrent.BatchWorkManager;

@Singleton
public class DefaultCodeCommentTextManager extends EntityTextManager<CodeComment> 
		implements CodeCommentTextManager {

	private static final String FIELD_PROJECT_ID = "projectId";
	
	private static final String FIELD_PATH = "path";
	
	private static final String FIELD_COMMENT = "comment";
	
	@Inject
	public DefaultCodeCommentTextManager(Dao dao, StorageManager storageManager, 
			BatchWorkManager batchWorkManager, TransactionManager transactionManager) {
		super(dao, storageManager, batchWorkManager, transactionManager);
	}

	@Override
	protected int getIndexVersion() {
		return 1;
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		super.on(event);
		if (event.getEntity() instanceof Project) {
			Long projectId = event.getEntity().getId();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					doWithWriter(new WriterRunnable() {

						@Override
						public void run(IndexWriter writer) throws IOException {
							writer.deleteDocuments(LongPoint.newExactQuery(FIELD_PROJECT_ID, projectId));
						}
						
					});
				}
				
			});
		}
	}
	
	@Override
	protected void addFields(Document document, CodeComment entity) {
		document.add(new LongPoint(FIELD_PROJECT_ID, entity.getProject().getId()));
		document.add(new StringField(FIELD_PATH, entity.getMark().getPath(), Store.NO));
		document.add(new TextField(FIELD_COMMENT, entity.getContent(), Store.NO));
	}

	@Nullable
	private Query buildQuery(Project project, String queryString) {
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		queryBuilder.add(LongPoint.newExactQuery(FIELD_PROJECT_ID, project.getId()), Occur.MUST);
		BooleanQuery.Builder contentQueryBuilder = new BooleanQuery.Builder();
		Query pathQuery = new BoostQuery(new WildcardQuery(new Term(FIELD_PATH, "*" + queryString + "*")), 0.75f);
		try (Analyzer analyzer = newAnalyzer()) {
			contentQueryBuilder.add(pathQuery, Occur.SHOULD);
			StandardQueryParser parser = new StandardQueryParser(analyzer);
			contentQueryBuilder.add(new BoostQuery(parser.parse(queryString, FIELD_COMMENT), 0.5f), Occur.SHOULD);
		} catch (Exception e) {
			contentQueryBuilder.add(pathQuery, Occur.SHOULD);
			contentQueryBuilder.add(new BoostQuery(getTermQuery(FIELD_COMMENT, queryString), 0.5f), Occur.SHOULD);
		}
		contentQueryBuilder.setMinimumNumberShouldMatch(1);
		queryBuilder.add(contentQueryBuilder.build(), Occur.MUST);
		
		return queryBuilder.build();		
	}

	@Override
	public List<CodeComment> query(@Nullable Project project, String queryString, 
			int firstResult, int maxResults) {
		Query query = buildQuery(project, queryString);
		if (query != null) 
			return search(query, firstResult, maxResults);
		else 
			return new ArrayList<>();
	}

	@Override
	public long count(@Nullable Project project, String queryString) {
		Query query = buildQuery(project, queryString);
		if (query != null) 
			return count(query);
		else 
			return 0;
	}
	
}
