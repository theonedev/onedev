package io.onedev.server.search.entitytext;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.*;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.issue.IssuesTouched;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.EntityTouch;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessConfidentialIssues;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.concurrent.BatchWorkManager;
import io.onedev.server.util.lucene.BooleanQueryBuilder;
import io.onedev.server.util.lucene.LuceneUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static io.onedev.server.util.criteria.Criteria.forManyValues;
import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toSet;
import static org.apache.lucene.document.Field.Store.NO;
import static org.apache.lucene.search.BooleanClause.Occur.MUST;
import static org.apache.lucene.search.BooleanClause.Occur.SHOULD;

@Singleton
public class DefaultIssueTextManager extends ProjectTextManager<Issue> implements IssueTextManager {
	
	private static final String FIELD_NUMBER = "number";
	
	private static final String FIELD_TITLE = "title";
	
	private static final String FIELD_CONFIDENTIAL = "confidential";
	
	private static final String FIELD_DESCRIPTION = "description";
	
	private static final String FIELD_COMMENT = "comments";
	
	private final IssueFieldManager fieldManager;
	
	private final IssueLinkManager linkManager;
	
	private final UserManager userManager;
	
	private final IssueTouchManager touchManager;
	
	@Inject
	public DefaultIssueTextManager(Dao dao, BatchWorkManager batchWorkManager, UserManager userManager,
								   TransactionManager transactionManager, ProjectManager projectManager,
								   IssueFieldManager fieldManager, IssueLinkManager linkManager,
								   ClusterManager clusterManager, SessionManager sessionManager, 
								   IssueTouchManager touchManager) {
		super(dao, batchWorkManager, transactionManager, projectManager, clusterManager, sessionManager);
		this.userManager = userManager;
		this.fieldManager = fieldManager;
		this.linkManager = linkManager;
		this.touchManager = touchManager;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(IssueTextManager.class);
	}
	
	@Override
	protected int getIndexVersion() {
		return 8;
	}

	@Override
	protected List<? extends EntityTouch> queryTouchesAfter(Long projectId, Long afterTouchId) {
		return touchManager.queryTouchesAfter(projectId, afterTouchId, Integer.MAX_VALUE);
	}

	@Override
	protected void addFields(Document entityDoc, Issue entity) {
		entityDoc.add(new StringField(FIELD_NUMBER, valueOf(entity.getNumber()), NO));
		entityDoc.add(new StringField(FIELD_CONFIDENTIAL, valueOf(entity.isConfidential()), NO));
		entityDoc.add(new TextField(FIELD_TITLE, entity.getTitle(), NO));
		if (entity.getDescription() != null)
			entityDoc.add(new TextField(FIELD_DESCRIPTION, entity.getDescription(), NO));
		for (var comment: entity.getComments()) {
			if (!comment.getUser().equals(userManager.getSystem()))
				entityDoc.add(new TextField(FIELD_COMMENT, comment.getContent(), NO));
		}
	}

	@Transactional
	@Listen
	public void on(IssuesTouched event) {
		requestToIndex(event.getProject().getId());
	}
	
	@Nullable
	private EntityTextQuery buildQuery(@Nullable ProjectScope projectScope, String queryString) {
		var escaped = LuceneUtils.escape(queryString);
		if (escaped != null) {
			var applicableProjectIds = new HashSet<Long>();
			BooleanQueryBuilder queryBuilder = new BooleanQueryBuilder();
			if (projectScope != null) {
				var project = projectScope.getProject();
				applicableProjectIds.add(project.getId());
				if (projectScope.isRecursive()) 
					applicableProjectIds.addAll(projectManager.getSubtreeIds(project.getId()));
				if (projectScope.isInherited()) {
					for (var ancestor: project.getAncestors()) {
						if (SecurityUtils.canAccess(ancestor))
							applicableProjectIds.add(ancestor.getId());
					}
				}
			} else {
				applicableProjectIds.addAll(projectManager.getPermittedProjects(new AccessProject())
						.stream()
						.map(AbstractEntity::getId)
						.collect(toSet()));
			}
			
			if (!SecurityUtils.isAdministrator()) {
				var permittedProjectIds = projectManager.getPermittedProjects(new AccessConfidentialIssues())
						.stream()
						.map(AbstractEntity::getId)
						.collect(toSet());
				permittedProjectIds.retainAll(applicableProjectIds);
				var allIds = projectManager.getIds();
				BooleanQueryBuilder confidentialQueryBuilder = new BooleanQueryBuilder();
				confidentialQueryBuilder.add(forManyValues(
						FIELD_PROJECT_ID, permittedProjectIds, allIds), SHOULD);
				confidentialQueryBuilder.add(getTermQuery(FIELD_CONFIDENTIAL, valueOf(false)), SHOULD);
				queryBuilder.add(confidentialQueryBuilder.build(), MUST);
			}

			BooleanQueryBuilder contentQueryBuilder = new BooleanQueryBuilder();

			String numberString = queryString;
			if (numberString.startsWith("#"))
				numberString = numberString.substring(1);
			try {
				long number = Long.parseLong(numberString);
				contentQueryBuilder.add(new BoostQuery(getTermQuery(FIELD_NUMBER, valueOf(number)), 1f), SHOULD);
			} catch (NumberFormatException ignored) {
			}

			try (Analyzer analyzer = newAnalyzer()) {
				Map<String, Float> boosts = new HashMap<>();
				boosts.put(FIELD_TITLE, 0.75f);
				boosts.put(FIELD_DESCRIPTION, 0.5f);
				boosts.put(FIELD_COMMENT, 0.25f);
				MultiFieldQueryParser parser = new MultiFieldQueryParser(
						new String[]{FIELD_TITLE, FIELD_DESCRIPTION, FIELD_COMMENT}, analyzer, boosts) {
					@Override
					protected Query newTermQuery(Term term, float boost) {
						return new BoostQuery(new PrefixQuery(term), boost);
					}
				};
				contentQueryBuilder.add(parser.parse(escaped), SHOULD);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
			queryBuilder.add(contentQueryBuilder.build(), MUST);
			
			var query = queryBuilder.build();
			if (query != null)
				return new EntityTextQuery(query, applicableProjectIds);
		}
		return null;
	}
	
	@Override
	public List<Issue> query(@Nullable ProjectScope projectScope, String queryString, 
			boolean loadFieldsAndLinks, int firstResult, int maxResults) {
		var issues = search(buildQuery(projectScope, queryString), firstResult, maxResults);
		if (projectScope != null) {
			for (var it = issues.iterator(); it.hasNext(); ) {
				var issue = it.next();
				if (!issue.getProject().equals(projectScope.getProject())
						&& (!projectScope.isInherited() || !issue.getProject().isSelfOrAncestorOf(projectScope.getProject())					
						&& (!projectScope.isRecursive() || !projectScope.getProject().isSelfOrAncestorOf(issue.getProject())))) {
					it.remove();
				}
			}
		}
		if (loadFieldsAndLinks && !issues.isEmpty()) {
			fieldManager.populateFields(issues);
			linkManager.populateLinks(issues);
		}

		return issues;
	}

	@Override
	public long count(@Nullable ProjectScope projectScope, String queryString) {
		return count(buildQuery(projectScope, queryString));
	}
	
}
