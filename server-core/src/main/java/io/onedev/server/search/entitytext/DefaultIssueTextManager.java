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
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.IssueLinkManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.concurrent.BatchWorkManager;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.lucene.BooleanQueryBuilder;
import io.onedev.server.util.lucene.LuceneUtils;

@Singleton
public class DefaultIssueTextManager extends ProjectTextManager<Issue> implements IssueTextManager {

	private static final String FIELD_NUMBER = "number";
	
	private static final String FIELD_TITLE = "title";
	
	private static final String FIELD_CONFIDENTIAL = "confidential";
	
	private static final String FIELD_DESCRIPTION = "description";
	
	private final IssueFieldManager fieldManager;
	
	private final IssueLinkManager linkManager;
	
	@Inject
	public DefaultIssueTextManager(Dao dao, StorageManager storageManager, 
			BatchWorkManager batchWorkManager, TransactionManager transactionManager, 
			ProjectManager projectManager, IssueFieldManager fieldManager, 
			IssueLinkManager linkManager, ClusterManager clusterManager) {
		super(dao, storageManager, batchWorkManager, transactionManager, 
				projectManager, clusterManager);
		this.fieldManager = fieldManager;
		this.linkManager = linkManager;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(IssueTextManager.class);
	}
	
	@Override
	protected int getIndexVersion() {
		return 3;
	}

	@Override
	protected void addFields(Document document, Issue entity) {
		document.add(new LongPoint(FIELD_NUMBER, entity.getNumber()));
		document.add(new LongPoint(FIELD_CONFIDENTIAL, entity.isConfidential()?1:0));
		document.add(new TextField(FIELD_TITLE, entity.getTitle(), Store.NO));
		if (entity.getDescription() != null)
			document.add(new TextField(FIELD_DESCRIPTION, entity.getDescription(), Store.NO));
	}

	@Nullable
	private Query buildQuery(@Nullable ProjectScope projectScope, String queryString) {
		BooleanQueryBuilder queryBuilder = new BooleanQueryBuilder();
		if (projectScope != null) {
			BooleanQueryBuilder projectQueryBuilder = new BooleanQueryBuilder();
			Project project = projectScope.getProject();
			if (projectScope.isRecursive()) 
				projectQueryBuilder.add(buildQuery(projectManager.getSubtreeIds(project.getId())), Occur.SHOULD);
			else if (SecurityUtils.canAccessConfidentialIssues(projectScope.getProject()))
				projectQueryBuilder.add(LongPoint.newExactQuery(FIELD_PROJECT_ID, project.getId()), Occur.SHOULD);
			else 
				projectQueryBuilder.add(getNonConfidentialQuery(project), Occur.SHOULD);
			
			if (projectScope.isInherited()) {
				for (Project ancestor: projectScope.getProject().getAncestors()) {
					if (SecurityUtils.canAccessConfidentialIssues(ancestor))
						projectQueryBuilder.add(LongPoint.newExactQuery(FIELD_PROJECT_ID, ancestor.getId()), Occur.SHOULD);
					else if (SecurityUtils.canAccess(ancestor)) 
						projectQueryBuilder.add(getNonConfidentialQuery(ancestor), Occur.SHOULD);
				}
			}
			queryBuilder.add(projectQueryBuilder.build(), Occur.MUST);
		} else if (!SecurityUtils.isAdministrator()) {
			Collection<Long> projectIds = projectManager.getPermittedProjects(new AccessProject()).stream()
					.map(it->it.getId())
					.collect(Collectors.toSet());
			
			if (!projectIds.isEmpty()) 
				queryBuilder.add(buildQuery(projectIds), Occur.MUST);
			else 
				return null;
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
	
	private Query buildQuery(Collection<Long> projectIds) {
		Collection<Long> allIds = projectManager.getIds();
		if (SecurityUtils.isAdministrator()) {
			return Criteria.forManyValues(FIELD_PROJECT_ID, projectIds, allIds);
		} else {
			Collection<Long> projectIdsWithConfidentialIssuePermission = new ArrayList<>();
			Collection<Long> projectIdsWithoutConfidentialIssuePermission = new ArrayList<>();
			for (Long projectId: projectIds) {
				Project project = projectManager.load(projectId);
				if (SecurityUtils.canAccessConfidentialIssues(project)) 
					projectIdsWithConfidentialIssuePermission.add(projectId);
				else
					projectIdsWithoutConfidentialIssuePermission.add(projectId);
			}
			BooleanQueryBuilder queryBuilder = new BooleanQueryBuilder();
			queryBuilder.add(Criteria.forManyValues(
					FIELD_PROJECT_ID, projectIdsWithConfidentialIssuePermission, allIds), Occur.SHOULD);
			BooleanQueryBuilder nonConfidentialQueryBuilder = new BooleanQueryBuilder();
			nonConfidentialQueryBuilder.add(Criteria.forManyValues(
					FIELD_PROJECT_ID, projectIdsWithoutConfidentialIssuePermission, allIds), Occur.MUST);
			nonConfidentialQueryBuilder.add(LongPoint.newExactQuery(FIELD_CONFIDENTIAL, 0L), Occur.MUST);
			queryBuilder.add(nonConfidentialQueryBuilder.build(), Occur.SHOULD);
			return queryBuilder.build();
		}
	}
	
	private BooleanQuery getNonConfidentialQuery(Project project) {
		BooleanQueryBuilder builder = new BooleanQueryBuilder();
		builder.add(LongPoint.newExactQuery(FIELD_PROJECT_ID, project.getId()), Occur.MUST);
		builder.add(LongPoint.newExactQuery(FIELD_CONFIDENTIAL, 0L), Occur.MUST);
		return builder.build();
	}
	
	@Override
	public List<Issue> query(@Nullable ProjectScope projectScope, String queryString, 
			boolean loadFieldsAndLinks, int firstResult, int maxResults) {
		Query query = buildQuery(projectScope, queryString);
		if (query != null) {
			List<Issue> issues = search(query, firstResult, maxResults);
			
			if (loadFieldsAndLinks && !issues.isEmpty()) {
				fieldManager.populateFields(issues);
				linkManager.populateLinks(issues);
			}
	
			return issues;
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public long count(@Nullable ProjectScope projectScope, String queryString) {
		Query query = buildQuery(projectScope, queryString);
		if (query != null) 
			return count(query);
		else 
			return 0;
	}
	
}
