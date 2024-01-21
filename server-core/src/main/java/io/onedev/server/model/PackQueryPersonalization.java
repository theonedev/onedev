package io.onedev.server.model;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PackQueryPersonalizationManager;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.model.support.pack.NamedPackQuery;
import io.onedev.server.util.watch.QuerySubscriptionSupport;
import io.onedev.server.util.watch.QueryWatchSupport;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;

@Entity
@Table(
		indexes={@Index(columnList="o_project_id"), @Index(columnList="o_user_id")}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", "o_user_id"})}
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
		if (isNew())
			OneDev.getInstance(PackQueryPersonalizationManager.class).create(this);
		else
			OneDev.getInstance(PackQueryPersonalizationManager.class).update(this);			
	}

}
