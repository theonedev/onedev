package io.onedev.server.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;

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
import io.onedev.server.entitymanager.CommitQueryPersonalizationManager;
import io.onedev.server.model.support.NamedCommitQuery;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.util.watch.QuerySubscriptionSupport;
import io.onedev.server.util.watch.QueryWatchSupport;

@Entity
@Table(
		indexes={@Index(columnList="o_project_id"), @Index(columnList="o_user_id")}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", "o_user_id"})}
)
public class CommitQueryPersonalization extends AbstractEntity implements QueryPersonalization<NamedCommitQuery> {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedCommitQuery> queries = new ArrayList<>();

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
	public ArrayList<NamedCommitQuery> getQueries() {
		return queries;
	}

	@Override
	public void setQueries(ArrayList<NamedCommitQuery> queries) {
		this.queries = queries;
	}
	
	public void setQuerySubscriptions(LinkedHashSet<String> querySubscriptions) {
		this.querySubscriptions = querySubscriptions;
	}

	@Override
	public QuerySubscriptionSupport<NamedCommitQuery> getQuerySubscriptionSupport() {
		return new QuerySubscriptionSupport<NamedCommitQuery>() {

			@Override
			public LinkedHashSet<String> getQuerySubscriptions() {
				return querySubscriptions;
			}
			
		};
	}

	@Override
	public QueryWatchSupport<NamedCommitQuery> getQueryWatchSupport() {
		return null;
	}

	@Override
	public void onUpdated() {
		if (isNew())
			OneDev.getInstance(CommitQueryPersonalizationManager.class).create(this);
		else
			OneDev.getInstance(CommitQueryPersonalizationManager.class).update(this);			
	}
	
}
