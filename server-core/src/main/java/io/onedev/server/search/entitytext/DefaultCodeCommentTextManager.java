package io.onedev.server.search.entitytext;

import com.google.common.collect.Sets;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.CodeCommentTouchManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.EntityTouch;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.concurrent.BatchWorkManager;
import io.onedev.server.util.lucene.BooleanQueryBuilder;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.onedev.server.util.lucene.LuceneUtils.escape;
import static org.apache.lucene.document.Field.Store.NO;
import static org.apache.lucene.search.BooleanClause.Occur.SHOULD;

@Singleton
public class DefaultCodeCommentTextManager extends ProjectTextManager<CodeComment> 
		implements CodeCommentTextManager {

	private static final String FIELD_PATH = "path";
	
	private static final String FIELD_COMMENT = "comment";
	
	private static final String FIELD_REPLY = "reply";
	
	private final CodeCommentTouchManager touchManager;
	
	@Inject
	public DefaultCodeCommentTextManager(Dao dao, BatchWorkManager batchWorkManager, TransactionManager transactionManager, 
										 ProjectManager projectManager, ClusterManager clusterManager, 
										 SessionManager sessionManager, CodeCommentTouchManager touchManager) {
		super(dao, batchWorkManager, transactionManager, projectManager, clusterManager, sessionManager);
		this.touchManager = touchManager;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(CodeCommentTextManager.class);
	}
	
	@Override
	protected int getIndexVersion() {
		return 6;
	}

	@Override
	protected List<? extends EntityTouch> queryTouchesAfter(Long projectId, Long afterTouchId) {
		return touchManager.queryTouchesAfter(projectId, afterTouchId, Integer.MAX_VALUE);
	}

	@Override
	protected void addFields(Document document, CodeComment entity) {
		document.add(new StringField(FIELD_PATH, entity.getMark().getPath(), NO));
		document.add(new TextField(FIELD_COMMENT, entity.getContent(), NO));
		for (CodeCommentReply reply: entity.getReplies())
			document.add(new TextField(FIELD_REPLY, reply.getContent(), NO));
	}
	
	@Nullable
	private EntityTextQuery buildQuery(Project project, String queryString) {
		var escaped = escape(queryString);
		if (escaped != null) {
			BooleanQueryBuilder queryBuilder = new BooleanQueryBuilder();
			queryBuilder.add(LongPoint.newExactQuery(FIELD_PROJECT_ID, project.getId()), Occur.MUST);
			BooleanQueryBuilder contentQueryBuilder = new BooleanQueryBuilder();

			Query pathQuery = new BoostQuery(new WildcardQuery(new Term(FIELD_PATH, "*" + escaped + "*")), 0.75f);
			contentQueryBuilder.add(pathQuery, SHOULD);
			try (Analyzer analyzer = newAnalyzer()) {
				Map<String, Float> boosts = new HashMap<>();
				boosts.put(FIELD_COMMENT, 0.75f);
				boosts.put(FIELD_REPLY, 0.5f);
				MultiFieldQueryParser parser = new MultiFieldQueryParser(
						new String[] {FIELD_COMMENT, FIELD_REPLY}, analyzer, boosts) {

					protected Query newTermQuery(Term term, float boost) {
						return new BoostQuery(new PrefixQuery(term), boost);
					}

				};
				contentQueryBuilder.add(parser.parse(escaped), SHOULD);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
			queryBuilder.add(contentQueryBuilder.build(), Occur.MUST);
			var query = queryBuilder.build();
			if (query != null)
				return new EntityTextQuery(query, Sets.newHashSet(project.getId()));
		} 
		return null;
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
