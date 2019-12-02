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
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.util.watch.QuerySubscriptionSupport;
import io.onedev.server.util.watch.QueryWatchSupport;

@Entity
@Table(
		indexes={@Index(columnList="o_project_id"), @Index(columnList="o_user_id")}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", "o_user_id"})}
)
public class IssueQuerySetting extends AbstractEntity implements QuerySetting<NamedIssueQuery> {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedIssueQuery> userQueries = new ArrayList<>();

	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashMap<String, Boolean> userQueryWatches = new LinkedHashMap<>();
	
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
	public ArrayList<NamedIssueQuery> getUserQueries() {
		return userQueries;
	}

	@Override
	public void setUserQueries(ArrayList<NamedIssueQuery> userQueries) {
		this.userQueries = userQueries;
	}
	
	public void setUserQueryWatches(LinkedHashMap<String, Boolean> userQueryWatches) {
		this.userQueryWatches = userQueryWatches;
	}

	public void setQueryWatches(LinkedHashMap<String, Boolean> projectQueryWatches) {
		this.queryWatches = projectQueryWatches;
	}

	@Override
	public QueryWatchSupport<NamedIssueQuery> getQueryWatchSupport() {
		return new QueryWatchSupport<NamedIssueQuery>() {

			@Override
			public LinkedHashMap<String, Boolean> getUserQueryWatches() {
				return userQueryWatches;
			}

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

}
