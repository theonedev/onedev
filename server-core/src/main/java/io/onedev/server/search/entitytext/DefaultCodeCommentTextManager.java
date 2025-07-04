package io.onedev.server.search.entitytext;

import static org.apache.lucene.document.Field.Store.NO;
import static org.apache.lucene.search.BooleanClause.Occur.MUST;
import static org.apache.lucene.search.BooleanClause.Occur.SHOULD;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.entitymanager.CodeCommentTouchManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.codecomment.CodeCommentTouched;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.EntityTouch;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.util.concurrent.BatchWorkManager;
import io.onedev.server.util.lucene.BooleanQueryBuilder;
import io.onedev.server.util.lucene.LuceneUtils;

@Singleton
public class DefaultCodeCommentTextManager extends EntityTextManager<CodeComment> implements CodeCommentTextManager {
	
	private static final String FIELD_PATH = "path";
	
	private static final String FIELD_COMMENT = "comment";
	
	private static final String FIELD_REPLY = "reply";
		
	private final UserManager userManager;
	
	private final CodeCommentTouchManager touchManager;
	
	private final CodeCommentManager codeCommentManager;
	
	@Inject
	public DefaultCodeCommentTextManager(Dao dao, BatchWorkManager batchWorkManager, UserManager userManager,
								   TransactionManager transactionManager, ProjectManager projectManager,
								   ClusterManager clusterManager, SessionManager sessionManager, 
								   CodeCommentTouchManager touchManager, CodeCommentManager codeCommentManager) {
		super(dao, batchWorkManager, transactionManager, projectManager, clusterManager, sessionManager);
		this.userManager = userManager;
		this.touchManager = touchManager;
		this.codeCommentManager = codeCommentManager;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(CodeCommentTextManager.class);
	}
	
	@Override
	protected int getIndexVersion() {
		return 9;
	}

	@Transactional
	@Listen
	public void on(CodeCommentTouched event) {
		requestToIndex(event.getProject().getId(), UPDATE_PRIORITY);
	}

	@Override
	protected List<? extends EntityTouch> queryTouchesAfter(Long projectId, Long afterTouchId) {
		return touchManager.queryTouchesAfter(projectId, afterTouchId, Integer.MAX_VALUE);
	}

	@Override
	protected void addFields(Document entityDoc, CodeComment entity) {
		entityDoc.add(new TextField(FIELD_PATH, entity.getMark().getPath(), NO));
		entityDoc.add(new TextField(FIELD_COMMENT, entity.getContent(), NO));
		for (CodeCommentReply reply: entity.getReplies()) {
			if (!reply.getUser().equals(userManager.getSystem()))
				entityDoc.add(new TextField(FIELD_REPLY, reply.getContent(), NO));
		}
	}

	@Nullable
	private Query buildContentQuery(String escapedQueryString) {
		BooleanQueryBuilder contentQueryBuilder = new BooleanQueryBuilder();

		try (Analyzer analyzer = newAnalyzer()) {
			Map<String, Float> boosts = new HashMap<>();
			boosts.put(FIELD_PATH, 1.0f);
			boosts.put(FIELD_COMMENT, 0.5f);
			boosts.put(FIELD_REPLY, 0.25f);
			MultiFieldQueryParser parser = new MultiFieldQueryParser(
					new String[]{FIELD_PATH, FIELD_COMMENT, FIELD_REPLY}, analyzer, boosts) {
				@Override
				protected Query newTermQuery(Term term, float boost) {
					return new BoostQuery(new PrefixQuery(term), boost);
				}
			};
			contentQueryBuilder.add(parser.parse(escapedQueryString), SHOULD);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		return contentQueryBuilder.build();
	}

	@Override
	public List<Long> query(@Nullable Project project, String queryString, int count) {
		var escaped = LuceneUtils.escape(queryString);
		if (escaped != null) {
			var applicableProjectIds = new HashSet<Long>();
			if (project != null) {
				applicableProjectIds.add(project.getId());
			} else {
				applicableProjectIds.addAll(SecurityUtils.getAuthorizedProjects(new ReadCode())
						.stream().map(AbstractEntity::getId).collect(Collectors.toList()));
			}
			var query = buildContentQuery(escaped);
			if (query != null)
				return search(new EntityTextQuery(query, applicableProjectIds), count);
		}
		return new ArrayList<>();
	}

	@Override
	public boolean matches(CodeComment comment, String queryString) {
		var escaped = LuceneUtils.escape(queryString);
		if (escaped != null) {
			BooleanQueryBuilder queryBuilder = new BooleanQueryBuilder();
			queryBuilder.add(LongPoint.newExactQuery(FIELD_PROJECT_ID, comment.getProject().getId()), MUST);
			queryBuilder.add(getTermQuery(FIELD_ENTITY_ID, String.valueOf(comment.getId())), MUST);											
			queryBuilder.add(buildContentQuery(escaped), MUST);			
			var query = queryBuilder.build();
			if (query != null)
				return !search(new EntityTextQuery(query, Collections.singleton(comment.getProject().getId())), 1).isEmpty();
		} 
		return false;
	}
	
	@Listen
	public void on(SystemStarted event) {
		var activeIds = projectManager.getActiveIds();
		for (var projectId: codeCommentManager.getProjectIds()) {
			if (activeIds.contains(projectId)) 
				requestToIndex(projectId, CHECK_PRIORITY);			
		}
	}	
	
}
