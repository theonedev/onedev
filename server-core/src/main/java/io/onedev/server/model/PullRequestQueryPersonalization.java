package io.onedev.server.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import io.onedev.server.OneDev;
import io.onedev.server.service.PullRequestQueryPersonalizationService;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;
import io.onedev.server.util.watch.QuerySubscriptionSupport;
import io.onedev.server.util.watch.QueryWatchSupport;

@Entity
@Table(
		indexes={@Index(columnList="project_id"), @Index(columnList="user_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"project_id", "user_id"})}
)
public class PullRequestQueryPersonalization extends AbstractEntity implements QueryPersonalization<NamedPullRequestQuery> {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedPullRequestQuery> queries = new ArrayList<>();

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
	public ArrayList<NamedPullRequestQuery> getQueries() {
		return queries;
	}

	@Override
	public void setQueries(ArrayList<NamedPullRequestQuery> queries) {
		this.queries = queries;
	}

	@Nullable
	public NamedPullRequestQuery getQuery(String name) {
		for (NamedPullRequestQuery query: getQueries()) {
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
	public QueryWatchSupport<NamedPullRequestQuery> getQueryWatchSupport() {
		return new QueryWatchSupport<NamedPullRequestQuery>() {

			@Override
			public LinkedHashMap<String, Boolean> getQueryWatches() {
				return queryWatches;
			}
			
		};
	}

	@Override
	public QuerySubscriptionSupport<NamedPullRequestQuery> getQuerySubscriptionSupport() {
		return null;
	}

	@Override
	public void onUpdated() {
		OneDev.getInstance(PullRequestQueryPersonalizationService.class).createOrUpdate(this);
	}
	
}
