package io.onedev.server.search.entitytext;

import static io.onedev.server.util.criteria.Criteria.forManyValues;
import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toSet;
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

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.issue.IssuesTouched;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.EntityTouch;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessConfidentialIssues;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.IssueTouchService;
import io.onedev.server.service.UserService;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.lucene.BooleanQueryBuilder;
import io.onedev.server.util.lucene.LuceneUtils;

@Singleton
public class DefaultIssueTextService extends EntityTextService<Issue> implements IssueTextService {
	
	private static final String FIELD_NUMBER = "number";
	
	private static final String FIELD_TITLE = "title";
	
	private static final String FIELD_CONFIDENTIAL = "confidential";
	
	private static final String FIELD_DESCRIPTION = "description";
	
	private static final String FIELD_COMMENT = "comments";

	@Inject
	private UserService userService;

	@Inject
	private IssueService issueService;

	@Inject
	private IssueTouchService touchService;

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(IssueTextService.class);
	}
	
	@Override
	protected int getIndexVersion() {
		return 9;
	}

	@Override
	protected List<? extends EntityTouch> queryTouchesAfter(Long projectId, Long afterTouchId) {
		return touchService.queryTouchesAfter(projectId, afterTouchId, Integer.MAX_VALUE);
	}

	@Override
	protected void addFields(Document entityDoc, Issue entity) {
		entityDoc.add(new StringField(FIELD_NUMBER, valueOf(entity.getNumber()), NO));
		entityDoc.add(new StringField(FIELD_CONFIDENTIAL, valueOf(entity.isConfidential()), NO));
		entityDoc.add(new TextField(FIELD_TITLE, entity.getTitle(), NO));
		if (entity.getDescription() != null)
			entityDoc.add(new TextField(FIELD_DESCRIPTION, entity.getDescription(), NO));
		for (var comment: entity.getComments()) {
			if (!comment.getUser().equals(userService.getSystem()))
				entityDoc.add(new TextField(FIELD_COMMENT, comment.getContent(), NO));
		}
	}

	@Transactional
	@Listen
	public void on(IssuesTouched event) {
		requestToIndex(event.getProject().getId(), UPDATE_PRIORITY);
	}

	@Listen
	public void on(SystemStarted event) {
		var activeIds = projectService.getActiveIds();
		for (var projectId: issueService.getProjectIds()) {
			if (activeIds.contains(projectId))
				requestToIndex(projectId, CHECK_PRIORITY);			
		}
	}	
	
	@Nullable
	private Query buildContentQuery(String escapedQueryString) {
		BooleanQueryBuilder contentQueryBuilder = new BooleanQueryBuilder();

		String numberString = escapedQueryString;
		if (numberString.startsWith("#"))
			numberString = numberString.substring(1);
		try {
			long number = Long.parseLong(numberString);
			contentQueryBuilder.add(new BoostQuery(getTermQuery(FIELD_NUMBER, valueOf(number)), 1f), SHOULD);
		} catch (NumberFormatException ignored) {
		}

		try (Analyzer analyzer = newAnalyzer()) {
			Map<String, Float> boosts = new HashMap<>();
			boosts.put(FIELD_TITLE, 1.0f);
			boosts.put(FIELD_DESCRIPTION, 0.5f);
			boosts.put(FIELD_COMMENT, 0.25f);
			MultiFieldQueryParser parser = new MultiFieldQueryParser(
					new String[]{FIELD_TITLE, FIELD_DESCRIPTION, FIELD_COMMENT}, analyzer, boosts) {
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
	public List<Long> query(@Nullable ProjectScope projectScope, String queryString, int count) {
		var escaped = LuceneUtils.escape(queryString);
		if (escaped != null) {
			var applicableProjectIds = new HashSet<Long>();
			if (projectScope != null) {
				var project = projectScope.getProject();
				applicableProjectIds.add(project.getId());
				if (projectScope.isRecursive()) 
					applicableProjectIds.addAll(projectService.getSubtreeIds(project.getId()));
				if (projectScope.isInherited()) {
					for (var ancestor: project.getAncestors()) {
						if (SecurityUtils.canAccessProject(ancestor))
							applicableProjectIds.add(ancestor.getId());
					}
				}
			} else {
				applicableProjectIds.addAll(SecurityUtils.getAuthorizedProjects(new AccessProject())
						.stream()
						.map(AbstractEntity::getId)
						.collect(toSet()));
			}
			
			var queryBuilder = new BooleanQueryBuilder();
			if (!SecurityUtils.isAdministrator()) {
				var permittedProjectIds = SecurityUtils.getAuthorizedProjects(new AccessConfidentialIssues())
						.stream()
						.map(AbstractEntity::getId)
						.collect(toSet());
				permittedProjectIds.retainAll(applicableProjectIds);
				var allIds = projectService.getIds();
				BooleanQueryBuilder confidentialQueryBuilder = new BooleanQueryBuilder();
				confidentialQueryBuilder.add(forManyValues(
						FIELD_PROJECT_ID, permittedProjectIds, allIds), SHOULD);
				confidentialQueryBuilder.add(getTermQuery(FIELD_CONFIDENTIAL, valueOf(false)), SHOULD);
				queryBuilder.add(confidentialQueryBuilder.build(), MUST);
			}
			queryBuilder.add(buildContentQuery(escaped), MUST);			
			var query = queryBuilder.build();
			if (query != null)
				return search(new EntityTextQuery(query, applicableProjectIds), count);
		}
		return new ArrayList<>();
	}

	@Override
	public boolean matches(Issue issue, String queryString) {
		var escaped = LuceneUtils.escape(queryString);
		if (escaped != null) {
			BooleanQueryBuilder queryBuilder = new BooleanQueryBuilder();
			queryBuilder.add(LongPoint.newExactQuery(FIELD_PROJECT_ID, issue.getProject().getId()), MUST);
			queryBuilder.add(getTermQuery(FIELD_ENTITY_ID, String.valueOf(issue.getId())), MUST);											
			queryBuilder.add(buildContentQuery(escaped), MUST);			
			var query = queryBuilder.build();
			if (query != null)
				return !search(new EntityTextQuery(query, Collections.singleton(issue.getProject().getId())), 1).isEmpty();
		} 
		return false;
	}
	
}
