package io.onedev.server.search.entitytext;

import static java.lang.String.valueOf;
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
import io.onedev.server.event.project.pullrequest.PullRequestTouched;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.EntityTouch;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.PullRequestTouchService;
import io.onedev.server.service.UserService;
import io.onedev.server.util.lucene.BooleanQueryBuilder;
import io.onedev.server.util.lucene.LuceneUtils;

@Singleton
public class DefaultPullRequestTextService extends EntityTextService<PullRequest> implements PullRequestTextService {
	
	private static final String FIELD_NUMBER = "number";
	
	private static final String FIELD_TITLE = "title";
		
	private static final String FIELD_DESCRIPTION = "description";
	
	private static final String FIELD_COMMENT = "comments";
		
	@Inject
	private UserService userService;

	@Inject
	private PullRequestTouchService touchService;

	@Inject
	private PullRequestService pullRequestService;

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(PullRequestTextService.class);
	}
	
	@Override
	protected int getIndexVersion() {
		return 9;
	}

	@Transactional
	@Listen
	public void on(PullRequestTouched event) {
		requestToIndex(event.getProject().getId(), UPDATE_PRIORITY);
	}

	@Listen
	public void on(SystemStarted event) {
		var activeIds = projectService.getActiveIds();
		for (var projectId: pullRequestService.getTargetProjectIds()) {
			if (activeIds.contains(projectId)) 
				requestToIndex(projectId, CHECK_PRIORITY);			
		}
	}	

	@Override
	protected List<? extends EntityTouch> queryTouchesAfter(Long projectId, Long afterTouchId) {
		return touchService.queryTouchesAfter(projectId, afterTouchId, Integer.MAX_VALUE);
	}

	@Override
	protected void addFields(Document entityDoc, PullRequest entity) {
		entityDoc.add(new StringField(FIELD_NUMBER, valueOf(entity.getNumber()), NO));
		entityDoc.add(new TextField(FIELD_TITLE, entity.getTitle(), NO));
		if (entity.getDescription() != null)
			entityDoc.add(new TextField(FIELD_DESCRIPTION, entity.getDescription(), NO));
		for (var comment: entity.getComments()) {
			if (!comment.getUser().equals(userService.getSystem()))
				entityDoc.add(new TextField(FIELD_COMMENT, comment.getContent(), NO));
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
	public boolean matches(PullRequest request, String queryString) {
		var escaped = LuceneUtils.escape(queryString);
		if (escaped != null) {
			BooleanQueryBuilder queryBuilder = new BooleanQueryBuilder();
			queryBuilder.add(LongPoint.newExactQuery(FIELD_PROJECT_ID, request.getProject().getId()), MUST);
			queryBuilder.add(getTermQuery(FIELD_ENTITY_ID, String.valueOf(request.getId())), MUST);											
			queryBuilder.add(buildContentQuery(escaped), MUST);			
			var query = queryBuilder.build();
			if (query != null)
				return !search(new EntityTextQuery(query, Collections.singleton(request.getProject().getId())), 1).isEmpty();
		} 
		return false;
	}
	
}
