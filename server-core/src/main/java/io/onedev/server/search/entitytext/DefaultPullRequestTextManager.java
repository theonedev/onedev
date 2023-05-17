package io.onedev.server.search.entitytext;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.*;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.support.EntityTouch;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.util.concurrent.BatchWorkManager;
import io.onedev.server.util.lucene.BooleanQueryBuilder;
import io.onedev.server.util.lucene.LuceneUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
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

import static java.util.stream.Collectors.toList;
import static org.apache.lucene.document.Field.Store.NO;
import static org.apache.lucene.document.LongPoint.newExactQuery;

@Singleton
public class DefaultPullRequestTextManager extends ProjectTextManager<PullRequest> 
		implements PullRequestTextManager {

	private static final String FIELD_NUMBER = "number";
	
	private static final String FIELD_TITLE = "title";
	
	private static final String FIELD_DESCRIPTION = "description";

	private static final String FIELD_COMMENT = "comment";
	
	private final PullRequestReviewManager reviewManager;
	
	private final BuildManager buildManager;
	
	private final UserManager userManager;
	
	private final PullRequestTouchManager touchManager;
	
	@Inject
	public DefaultPullRequestTextManager(Dao dao, UserManager userManager, BatchWorkManager batchWorkManager, 
										 TransactionManager transactionManager, ProjectManager projectManager, 
										 PullRequestReviewManager reviewManager, BuildManager buildManager, 
										 ClusterManager clusterManager, SessionManager sessionManager, 
										 PullRequestTouchManager touchManager) {
		super(dao, batchWorkManager, transactionManager, projectManager, clusterManager, sessionManager);
		this.reviewManager = reviewManager;
		this.buildManager = buildManager;
		this.userManager = userManager;
		this.touchManager = touchManager;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(PullRequestTextManager.class);
	}
	
	@Override
	protected int getIndexVersion() {
		return 5;
	}

	@Override
	protected List<? extends EntityTouch> queryTouchesAfter(Long projectId, Long afterTouchId) {
		return touchManager.queryTouchesAfter(projectId, afterTouchId, Integer.MAX_VALUE);
	}

	@Override
	protected void addFields(Document entityDoc, PullRequest entity) {
		entityDoc.add(new LongPoint(FIELD_NUMBER, entity.getNumber()));
		entityDoc.add(new TextField(FIELD_TITLE, entity.getTitle(), NO));
		if (entity.getDescription() != null)
			entityDoc.add(new TextField(FIELD_DESCRIPTION, entity.getDescription(), NO));
		for (PullRequestComment comment: entity.getComments()) {
			if (!comment.getUser().equals(userManager.getSystem()))
				entityDoc.add(new TextField(FIELD_COMMENT, comment.getContent(), NO));
		}
	}
	
	@Nullable
	private EntityTextQuery buildQuery(@Nullable Project project, String queryString) {
		var escaped = LuceneUtils.escape(queryString);
		if (escaped != null) {
			var applicableProjectIds = new HashSet<Long>();
			if (project != null) {
				applicableProjectIds.add(project.getId());
			} else {
				applicableProjectIds.addAll(projectManager.getPermittedProjects(new ReadCode())
						.stream().map(AbstractEntity::getId).collect(toList()));
			}
			if (!applicableProjectIds.isEmpty()) {
				BooleanQueryBuilder contentQueryBuilder = new BooleanQueryBuilder();

				String numberString = queryString;
				if (numberString.startsWith("#"))
					numberString = numberString.substring(1);
				try {
					long number = Long.parseLong(numberString);
					contentQueryBuilder.add(new BoostQuery(newExactQuery(FIELD_NUMBER, number), 1f), Occur.SHOULD);
				} catch (NumberFormatException ignored) {
				}

				try (Analyzer analyzer = newAnalyzer()) {
					Map<String, Float> boosts = new HashMap<>();
					boosts.put(FIELD_TITLE, 0.75f);
					boosts.put(FIELD_DESCRIPTION, 0.5f);
					boosts.put(FIELD_COMMENT, 0.25f);
					MultiFieldQueryParser parser = new MultiFieldQueryParser(
							new String[]{FIELD_TITLE, FIELD_DESCRIPTION, FIELD_COMMENT}, analyzer, boosts) {

						protected Query newTermQuery(Term term, float boost) {
							return new BoostQuery(new PrefixQuery(term), boost);
						}

					};
					contentQueryBuilder.add(parser.parse(escaped), Occur.SHOULD);
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
				var contentQuery = contentQueryBuilder.build();
				if (contentQuery != null)
					return new EntityTextQuery(contentQuery, applicableProjectIds);
			}
		}
		return null;
	}

	@Override
	public List<PullRequest> query(@Nullable Project project, String queryString, 
		boolean loadReviewsAndBuilds, int firstResult, int maxResults) {
		List<PullRequest> requests = search(buildQuery(project, queryString), firstResult, maxResults);
		if (!requests.isEmpty() && loadReviewsAndBuilds) {
			reviewManager.populateReviews(requests);
			buildManager.populateBuilds(requests);
		}
		return requests;
	}

	@Override
	public long count(@Nullable Project project, String queryString) {
		return count(buildQuery(project, queryString));
	}
	
}
