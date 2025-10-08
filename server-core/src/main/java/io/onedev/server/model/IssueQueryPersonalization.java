package io.onedev.server.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.jspecify.annotations.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import io.onedev.server.OneDev;
import io.onedev.server.service.IssueQueryPersonalizationService;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.util.watch.QuerySubscriptionSupport;
import io.onedev.server.util.watch.QueryWatchSupport;

@Entity
@Table(
		indexes={@Index(columnList="o_project_id"), @Index(columnList="o_user_id")}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", "o_user_id"})}
)
public class IssueQueryPersonalization extends AbstractEntity implements QueryPersonalization<NamedIssueQuery> {

	private static final long serialVersionUID = 1L;

	public static final String PROP_PROJECT = "project";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedIssueQuery> queries = new ArrayList<>();

	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashMap<String, Boolean> queryWatches = new LinkedHashMap<>();
	
	@Override
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Override
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public ArrayList<NamedIssueQuery> getQueries() {
		return queries;
	}

	@Override
	public void setQueries(ArrayList<NamedIssueQuery> queries) {
		this.queries = queries;
	}

	@Nullable
	public NamedIssueQuery getQuery(String name) {
		for (NamedIssueQuery query: getQueries()) {
			if (query.getName().equals(name))
				return query;
		}
		return null;
	}
	
	public LinkedHashMap<String, Boolean> getQueryWatches() {
		return queryWatches;
	}

	public void setQueryWatches(LinkedHashMap<String, Boolean> queryWatches) {
		this.queryWatches = queryWatches;
	}

	@Override
	public QueryWatchSupport<NamedIssueQuery> getQueryWatchSupport() {
		return new QueryWatchSupport<NamedIssueQuery>() {

			@Override
			public LinkedHashMap<String, Boolean> getQueryWatches() {
				return queryWatches;
			}
			
		};
	}

	@Override
	public QuerySubscriptionSupport<NamedIssueQuery> getQuerySubscriptionSupport() {
		return null;
	}

	@Override
	public void onUpdated() {
		OneDev.getInstance(IssueQueryPersonalizationService.class).createOrUpdate(this);
	}
	
}
