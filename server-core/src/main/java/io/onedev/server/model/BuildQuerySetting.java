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

import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.model.support.build.NamedBuildQuery;
import io.onedev.server.util.watch.QuerySubscriptionSupport;
import io.onedev.server.util.watch.QueryWatchSupport;

@Entity
@Table(
		indexes={@Index(columnList="o_project_id"), @Index(columnList="o_user_id")}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", "o_user_id"})}
)
public class BuildQuerySetting extends AbstractEntity implements QuerySetting<NamedBuildQuery> {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedBuildQuery> userQueries = new ArrayList<>();

	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashSet<String> userQuerySubscriptions = new LinkedHashSet<>();
	
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
	public ArrayList<NamedBuildQuery> getUserQueries() {
		return userQueries;
	}

	@Override
	public void setUserQueries(ArrayList<NamedBuildQuery> userQueries) {
		this.userQueries = userQueries;
	}

	@Override
	public QueryWatchSupport<NamedBuildQuery> getQueryWatchSupport() {
		return null;
	}

	@Override
	public QuerySubscriptionSupport<NamedBuildQuery> getQuerySubscriptionSupport() {
		return new QuerySubscriptionSupport<NamedBuildQuery>() {

			@Override
			public LinkedHashSet<String> getUserQuerySubscriptions() {
				return userQuerySubscriptions;
			}

			@Override
			public LinkedHashSet<String> getQuerySubscriptions() {
				return querySubscriptions;
			}
			
		};
	}

}
