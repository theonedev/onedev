package io.onedev.server.search.entitytext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;

import io.onedev.commons.loader.Listen;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.IssueLinkManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.concurrent.BatchWorkManager;

@Singleton
public class DefaultIssueTextManager extends EntityTextManager<Issue> implements IssueTextManager {

	private static final String FIELD_PROJECT_ID = "projectId";
	
	private static final String FIELD_NUMBER = "number";
	
	private static final String FIELD_TITLE = "title";
	
	private static final String FIELD_DESCRIPTION = "description";
	
	private final ProjectManager projectManager;
	
	private final IssueFieldManager fieldManager;
	
	private final IssueLinkManager linkManager;
	
	@Inject
	public DefaultIssueTextManager(Dao dao, StorageManager storageManager, 
			BatchWorkManager batchWorkManager, TransactionManager transactionManager, 
			ProjectManager projectManager, IssueFieldManager fieldManager, 
			IssueLinkManager linkManager) {
		super(dao, storageManager, batchWorkManager, transactionManager);
		this.projectManager = projectManager;
		this.fieldManager = fieldManager;
		this.linkManager = linkManager;
	}

	@Override
	protected int getIndexVersion() {
		return 1;
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		super.on(event);
		if (event.getEntity() instanceof Project) {
			Long projectId = event.getEntity().getId();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					doWithWriter(new WriterRunnable() {

						@Override
						public void run(IndexWriter writer) throws IOException {
							writer.deleteDocuments(LongPoint.newExactQuery(FIELD_PROJECT_ID, projectId));
						}
						
					});
				}
				
			});
		}
	}
	
	@Override
	protected void addFields(Document document, Issue entity) {
		document.add(new LongPoint(FIELD_PROJECT_ID, entity.getProject().getId()));
		document.add(new LongPoint(FIELD_NUMBER, entity.getNumber()));
		document.add(new TextField(FIELD_TITLE, entity.getTitle(), Store.NO));
		if (entity.getDescription() != null)
			document.add(new TextField(FIELD_DESCRIPTION, entity.getDescription(), Store.NO));
	}

	@Nullable
	private Query buildQuery(@Nullable ProjectScope projectScope, String queryString) {
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		if (projectScope != null) {
			if (projectScope.isRecursive())
				queryBuilder.add(projectManager.getTreeQuery(FIELD_PROJECT_ID, projectScope.getProject()), Occur.MUST);
			else
				queryBuilder.add(LongPoint.newExactQuery(FIELD_PROJECT_ID, projectScope.getProject().getId()), Occur.MUST);
		} else if (!SecurityUtils.isAdministrator()) {
			Collection<Project> projects = projectManager.getPermittedProjects(new AccessProject());
			if (!projects.isEmpty()) 
				queryBuilder.add(projectManager.getProjectsQuery(FIELD_PROJECT_ID, projects), Occur.MUST);
			else
				return null;
		}
		
		BooleanQuery.Builder contentQueryBuilder = new BooleanQuery.Builder();
		
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
			contentQueryBuilder.add(parser.parse(queryString), Occur.SHOULD);
		} catch (Exception e) {
			contentQueryBuilder.add(getTermQuery(FIELD_TITLE, queryString), Occur.SHOULD);
			contentQueryBuilder.add(getTermQuery(FIELD_DESCRIPTION, queryString), Occur.SHOULD);
		}
		contentQueryBuilder.setMinimumNumberShouldMatch(1);
		queryBuilder.add(contentQueryBuilder.build(), Occur.MUST);
		
		return queryBuilder.build();		
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
