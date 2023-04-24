package io.onedev.server.search.entitytext;

import com.google.common.collect.Lists;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.codecomment.CodeCommentDeleted;
import io.onedev.server.event.project.codecomment.CodeCommentEvent;
import io.onedev.server.event.project.codecomment.CodeCommentsDeleted;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.concurrent.BatchWorkManager;
import io.onedev.server.util.lucene.BooleanQueryBuilder;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
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

@Singleton
public class DefaultCodeCommentTextManager extends ProjectTextManager<CodeComment> 
		implements CodeCommentTextManager {

	private static final String FIELD_PATH = "path";
	
	private static final String FIELD_COMMENT = "comment";
	
	private static final String FIELD_REPLIES = "replies";
	
	@Inject
	public DefaultCodeCommentTextManager(Dao dao, BatchWorkManager batchWorkManager, 
										 TransactionManager transactionManager, 
										 ProjectManager projectManager, 
										 ClusterManager clusterManager) {
		super(dao, batchWorkManager, transactionManager, projectManager, clusterManager);
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(CodeCommentTextManager.class);
	}
	
	@Override
	protected int getIndexVersion() {
		return 4;
	}

	@Override
	protected void addFields(Document document, CodeComment entity) {
		document.add(new StringField(FIELD_PATH, entity.getMark().getPath(), Store.NO));
		document.add(new TextField(FIELD_COMMENT, entity.getContent(), Store.NO));
		StringBuilder builder = new StringBuilder();
		for (CodeCommentReply reply: entity.getReplies())
			builder.append(reply.getContent()).append("\n");
		if (builder.length() != 0)
			document.add(new TextField(FIELD_REPLIES, builder.toString(), Store.NO));
	}

	@Sessional
	@Listen
	public void on(CodeCommentEvent event) {
		requestIndex(event.getComment());
	}
	@Sessional
	@Listen
	public void on(CodeCommentsDeleted event) {
		clusterManager.submitToAllServers(() -> {
			deleteEntities(event.getCommentIds());
			return null;
		});
	}

	@Sessional
	@Listen
	public void on(CodeCommentDeleted event) {
		clusterManager.submitToAllServers(() -> {
			deleteEntities(Lists.newArrayList(event.getCommentId()));
			return null;
		});
	}

	@Nullable
	private Query buildQuery(Project project, String queryString) {
		BooleanQueryBuilder queryBuilder = new BooleanQueryBuilder();
		queryBuilder.add(LongPoint.newExactQuery(FIELD_PROJECT_ID, project.getId()), Occur.MUST);
		BooleanQueryBuilder contentQueryBuilder = new BooleanQueryBuilder();
		
		var escaped = escape(queryString);
		if (escaped != null) {
			Query pathQuery = new BoostQuery(new WildcardQuery(new Term(FIELD_PATH, "*" + escaped + "*")), 0.75f);
			try (Analyzer analyzer = newAnalyzer()) {
				Map<String, Float> boosts = new HashMap<>();
				boosts.put(FIELD_COMMENT, 0.75f);
				boosts.put(FIELD_REPLIES, 0.5f);
				MultiFieldQueryParser parser = new MultiFieldQueryParser(
						new String[] {FIELD_COMMENT, FIELD_REPLIES}, analyzer, boosts) {

					protected Query newTermQuery(Term term, float boost) {
						return new BoostQuery(new PrefixQuery(term), boost);
					}

				};
				contentQueryBuilder.add(parser.parse(escaped), Occur.SHOULD);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
			queryBuilder.add(contentQueryBuilder.build(), Occur.MUST);

			return queryBuilder.build();
		} else {
			return null;
		}
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
