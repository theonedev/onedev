package io.onedev.server.search.entitytext;

import java.io.ObjectStreamException;
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
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.concurrent.BatchWorkManager;
import io.onedev.server.util.lucene.BooleanQueryBuilder;
import io.onedev.server.util.lucene.LuceneUtils;

@Singleton
public class DefaultCodeCommentTextManager extends ProjectTextManager<CodeComment> 
		implements CodeCommentTextManager {

	private static final String FIELD_PATH = "path";
	
	private static final String FIELD_COMMENT = "comment";
	
	@Inject
	public DefaultCodeCommentTextManager(Dao dao, StorageManager storageManager, 
			BatchWorkManager batchWorkManager, TransactionManager transactionManager, 
			ProjectManager projectManager, ClusterManager clusterManager) {
		super(dao, storageManager, batchWorkManager, transactionManager, projectManager, 
				clusterManager);
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(CodeCommentTextManager.class);
	}
	
	@Override
	protected int getIndexVersion() {
		return 2;
	}

	@Override
	protected void addFields(Document document, CodeComment entity) {
		document.add(new StringField(FIELD_PATH, entity.getMark().getPath(), Store.NO));
		document.add(new TextField(FIELD_COMMENT, entity.getContent(), Store.NO));
	}

	private Query buildQuery(Project project, String queryString) {
		BooleanQueryBuilder queryBuilder = new BooleanQueryBuilder();
		queryBuilder.add(LongPoint.newExactQuery(FIELD_PROJECT_ID, project.getId()), Occur.MUST);
		BooleanQueryBuilder contentQueryBuilder = new BooleanQueryBuilder();
		
		queryString = LuceneUtils.escape(queryString);
		Query pathQuery = new BoostQuery(new WildcardQuery(new Term(FIELD_PATH, "*" + queryString + "*")), 0.75f);
		try (Analyzer analyzer = newAnalyzer()) {
			contentQueryBuilder.add(pathQuery, Occur.SHOULD);
			StandardQueryParser parser = new StandardQueryParser(analyzer);
			contentQueryBuilder.add(
					new BoostQuery(parser.parse(queryString, FIELD_COMMENT), 0.5f), 
					Occur.SHOULD);
		} catch (QueryNodeException e) {
			throw new RuntimeException(e);
		}
		queryBuilder.add(contentQueryBuilder.build(), Occur.MUST);
		
		return queryBuilder.build();		
	}
	
	@Override
	public List<CodeComment> query(@Nullable Project project, String queryString, 
			int firstResult, int maxResults) {
		return search(buildQuery(project, queryString), firstResult, maxResults);
	}

	@Override
	public long count(@Nullable Project project, String queryString) {
		return count(buildQuery(project, queryString));
	}
	
}
