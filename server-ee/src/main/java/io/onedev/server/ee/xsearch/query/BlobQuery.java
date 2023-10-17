package io.onedev.server.ee.xsearch.query;

import io.onedev.server.OneDev;
import io.onedev.server.ee.xsearch.match.BlobMatch;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.search.code.query.TooGeneralQueryException;
import io.onedev.server.security.permission.ReadCode;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;

import static io.onedev.server.search.code.FieldConstants.PROJECT_ID;
import static io.onedev.server.util.criteria.Criteria.forManyValues;
import static io.onedev.server.util.patternset.PatternSet.parse;
import static java.util.stream.Collectors.toSet;

public abstract class BlobQuery implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String projects;
	
	private final int count;

	public BlobQuery(@Nullable String projects, int count) {
		this.projects = projects;
		this.count = count;
	}

	@Nullable
	public String getProjects() {
		return projects;
	}

	public int getCount() {
		return count;
	}
	
	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	/**
	 * Get lucene query representation of this query
	 * 
	 * @return 
	 * 			lucene query
	 * @throws 
	 *            TooGeneralQueryException if supplied query term is too general to possibly cause query slow
	 */
	public Query asLuceneQuery(Collection<Long> projectIds) throws TooGeneralQueryException {
		BooleanQuery.Builder luceneQueryBuilder = new BooleanQuery.Builder();
		var allIds = getProjectManager().getIds();
		luceneQueryBuilder.add(forManyValues(PROJECT_ID.name(), projectIds, allIds), Occur.MUST);
		
		applyConstraints(luceneQueryBuilder);
		
		return luceneQueryBuilder.build();
	}
	
	public Collection<Long> getApplicableProjectIds() {
		var projectManager = OneDev.getInstance(ProjectManager.class);
		Collection<Long> projectIds = projectManager.getPermittedProjects(new ReadCode())
				.stream().map(AbstractEntity::getId).collect(toSet());
		if (projects != null) 
			projectIds.retainAll(projectManager.getPathMatchingIds(parse(projects)));
		
		return projectIds;
	}

	protected abstract void applyConstraints(BooleanQuery.Builder query);

	@Nullable
	public abstract BlobMatch matches(Document document);
	
}
