package io.onedev.server.search.entitytext;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.concurrent.BatchWorkManager;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.lucene.BooleanQueryBuilder;
import io.onedev.server.util.lucene.LuceneUtils;

@Singleton
public class DefaultPullRequestTextManager extends ProjectTextManager<PullRequest> 
		implements PullRequestTextManager {

	private static final String FIELD_NUMBER = "number";
	
	private static final String FIELD_TITLE = "title";
	
	private static final String FIELD_DESCRIPTION = "description";
	
	private final PullRequestReviewManager reviewManager;
	
	private final BuildManager buildManager;
	
	@Inject
	public DefaultPullRequestTextManager(Dao dao, StorageManager storageManager, 
			BatchWorkManager batchWorkManager, TransactionManager transactionManager, 
			ProjectManager projectManager, PullRequestReviewManager reviewManager, 
			BuildManager buildManager, ClusterManager clusterManager) {
		super(dao, storageManager, batchWorkManager, transactionManager, projectManager, 
				clusterManager);
		this.reviewManager = reviewManager;
		this.buildManager = buildManager;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(PullRequestTextManager.class);
	}
	
	@Override
	protected int getIndexVersion() {
		return 2;
	}

	@Override
	protected void addFields(Document document, PullRequest entity) {
		document.add(new LongPoint(FIELD_NUMBER, entity.getNumber()));
		document.add(new TextField(FIELD_TITLE, entity.getTitle(), Store.NO));
		if (entity.getDescription() != null)
			document.add(new TextField(FIELD_DESCRIPTION, entity.getDescription(), Store.NO));
	}

	@Nullable
	private Query buildQuery(@Nullable Project project, String queryString) {
		BooleanQueryBuilder queryBuilder = new BooleanQueryBuilder();
		if (project != null) {
			queryBuilder.add(LongPoint.newExactQuery(FIELD_PROJECT_ID, project.getId()), Occur.MUST);
		} else if (!SecurityUtils.isAdministrator()) {
			Collection<Project> projects = projectManager.getPermittedProjects(new ReadCode());
			if (!projects.isEmpty()) {
				Query projectsQuery = Criteria.forManyValues(
						FIELD_PROJECT_ID, 
						projects.stream().map(it->it.getId()).collect(Collectors.toSet()), 
						projectManager.getIds());
				queryBuilder.add(projectsQuery, Occur.MUST);
			} else {
				return null;
			}
		}
		BooleanQueryBuilder contentQueryBuilder = new BooleanQueryBuilder();
		
		String numberString = queryString;
		if (numberString.startsWith("#")) 
			numberString = numberString.substring(1);
		try {
			Long number = Long.valueOf(numberString);
			contentQueryBuilder.add(new BoostQuery(LongPoint.newExactQuery(FIELD_NUMBER, number), 1f), Occur.SHOULD);
		} catch (NumberFormatException e) {
		}
		
		try (Analyzer analyzer = newAnalyzer()) {
			Map<String, Float> boosts = new HashMap<>();
			boosts.put(FIELD_TITLE, 0.75f);
			boosts.put(FIELD_DESCRIPTION, 0.5f);
			MultiFieldQueryParser parser = new MultiFieldQueryParser(
					new String[] {FIELD_TITLE, FIELD_DESCRIPTION}, analyzer, boosts);
			contentQueryBuilder.add(parser.parse(LuceneUtils.escape(queryString)), Occur.SHOULD);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		queryBuilder.add(contentQueryBuilder.build(), Occur.MUST);
		
		return queryBuilder.build();		
	}

	@Override
	public List<PullRequest> query(@Nullable Project project, String queryString, 
			boolean loadReviewsAndBuilds, int firstResult, int maxResults) {
		Query query = buildQuery(project, queryString);
		if (query != null) {
			List<PullRequest> requests = search(query, firstResult, maxResults);
			if (!requests.isEmpty() && loadReviewsAndBuilds) {
				reviewManager.populateReviews(requests);
				buildManager.populateBuilds(requests);
			}
			return requests;
		} else { 
			return new ArrayList<>();
		}
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
