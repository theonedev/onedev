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

import io.onedev.server.model.support.NamedCommitQuery;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.util.watch.QuerySubscriptionSupport;
import io.onedev.server.util.watch.QueryWatchSupport;

@Entity
@Table(
		indexes={@Index(columnList="o_project_id"), @Index(columnList="o_user_id")}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", "o_user_id"})}
)
public class CommitQuerySetting extends AbstractEntity implements QuerySetting<NamedCommitQuery> {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<NamedCommitQuery> userQueries = new ArrayList<>();

	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashSet<String> userQuerySubscriptions = new LinkedHashSet<>();
	
	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashSet<String> projectQuerySubscriptions = new LinkedHashSet<>();
	
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
	public ArrayList<NamedCommitQuery> getUserQueries() {
		return userQueries;
	}

	@Override
	public void setUserQueries(ArrayList<NamedCommitQuery> userQueries) {
		this.userQueries = userQueries;
	}
	
	public void setUserQuerySubscriptions(LinkedHashSet<String> userQuerySubscriptions) {
		this.userQuerySubscriptions = userQuerySubscriptions;
	}

	public void setProjectQuerySubscriptions(LinkedHashSet<String> projectQuerySubscriptions) {
		this.projectQuerySubscriptions = projectQuerySubscriptions;
	}

	@Override
	public QuerySubscriptionSupport<NamedCommitQuery> getQuerySubscriptionSupport() {
		return new QuerySubscriptionSupport<NamedCommitQuery>() {

			@Override
			public LinkedHashSet<String> getUserQuerySubscriptions() {
				return userQuerySubscriptions;
			}

			@Override
			public LinkedHashSet<String> getQuerySubscriptions() {
				return projectQuerySubscriptions;
			}
			
		};
	}

	@Override
	public QueryWatchSupport<NamedCommitQuery> getQueryWatchSupport() {
		return null;
	}

}
