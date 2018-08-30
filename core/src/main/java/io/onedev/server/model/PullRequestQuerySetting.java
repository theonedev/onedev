package io.onedev.server.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;

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
import io.onedev.server.model.support.QuerySubscriptionSupport;
import io.onedev.server.model.support.QueryWatchSupport;
import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;

@Entity
@Table(
		indexes={@Index(columnList="g_project_id"), @Index(columnList="g_user_id")}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"g_project_id", "g_user_id"})}
)
public class PullRequestQuerySetting extends QuerySetting<NamedPullRequestQuery> {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedPullRequestQuery> userQueries = new ArrayList<>();

	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashMap<String, Boolean> userQueryWatches = new LinkedHashMap<>();
	
	@Column(nullable=false, length=65535)
	private LinkedHashMap<String, Boolean> projectQueryWatches = new LinkedHashMap<>();
	
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
	public ArrayList<NamedPullRequestQuery> getUserQueries() {
		return userQueries;
	}

	@Override
	public void setUserQueries(ArrayList<NamedPullRequestQuery> userQueries) {
		this.userQueries = userQueries;
	}
	
	public void setUserQueryWatches(LinkedHashMap<String, Boolean> userQueryWatches) {
		this.userQueryWatches = userQueryWatches;
	}

	public void setProjectQueryWatches(LinkedHashMap<String, Boolean> projectQueryWatches) {
		this.projectQueryWatches = projectQueryWatches;
	}

	@Override
	public QueryWatchSupport<NamedPullRequestQuery> getQueryWatchSupport() {
		return new QueryWatchSupport<NamedPullRequestQuery>() {

			@Override
			public LinkedHashMap<String, Boolean> getUserQueryWatches() {
				return userQueryWatches;
			}

			@Override
			public LinkedHashMap<String, Boolean> getProjectQueryWatches() {
				return projectQueryWatches;
			}
			
		};
	}

	@Override
	public QuerySubscriptionSupport<NamedPullRequestQuery> getQuerySubscriptionSupport() {
		return null;
	}

}
