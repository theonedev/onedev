package io.onedev.server.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;

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
import io.onedev.server.service.PackQueryPersonalizationService;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.model.support.pack.NamedPackQuery;
import io.onedev.server.util.watch.QuerySubscriptionSupport;
import io.onedev.server.util.watch.QueryWatchSupport;

@Entity
@Table(
		indexes={@Index(columnList="project_id"), @Index(columnList="user_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"project_id", "user_id"})}
)
public class PackQueryPersonalization extends AbstractEntity implements QueryPersonalization<NamedPackQuery> {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedPackQuery> queries = new ArrayList<>();

	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashSet<String> querySubscriptions = new LinkedHashSet<>();
	
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
	public ArrayList<NamedPackQuery> getQueries() {
		return queries;
	}

	@Override
	public void setQueries(ArrayList<NamedPackQuery> queries) {
		this.queries = queries;
	}

	@Nullable
	public NamedPackQuery getQuery(String name) {
		for (NamedPackQuery query: getQueries()) {
			if (query.getName().equals(name))
				return query;
		}
		return null;
	}

	public LinkedHashSet<String> getQuerySubscriptions() {
		return querySubscriptions;
	}

	public void setQuerySubscriptions(LinkedHashSet<String> querySubscriptions) {
		this.querySubscriptions = querySubscriptions;
	}

	@Override
	public QueryWatchSupport<NamedPackQuery> getQueryWatchSupport() {
		return null;
	}

	@Override
	public QuerySubscriptionSupport<NamedPackQuery> getQuerySubscriptionSupport() {
		return new QuerySubscriptionSupport<NamedPackQuery>() {

			@Override
			public LinkedHashSet<String> getQuerySubscriptions() {
				return querySubscriptions;
			}
			
		};
	}

	@Override
	public void onUpdated() {
		OneDev.getInstance(PackQueryPersonalizationService.class).createOrUpdate(this);
	}

}
