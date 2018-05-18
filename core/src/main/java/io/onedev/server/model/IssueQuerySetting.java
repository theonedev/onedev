package io.onedev.server.model;

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

@Entity
@Table(
		indexes={@Index(columnList="g_project_id"), @Index(columnList="g_user_id")}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"g_project_id", "g_user_id"})}
)
public class IssueQuerySetting extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashMap<String, String> userQueries = new LinkedHashMap<>();

	@Lob
	@Column(nullable=false, length=65535)
	private LinkedHashMap<String, Boolean> userQueryWatches = new LinkedHashMap<>();
	
	@Column(nullable=false, length=65535)
	private LinkedHashMap<String, Boolean> projectQueryWatches = new LinkedHashMap<>();
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LinkedHashMap<String, String> getUserQueries() {
		return userQueries;
	}

	public void setUserQueries(LinkedHashMap<String, String> userQueries) {
		this.userQueries = userQueries;
	}

	public LinkedHashMap<String, Boolean> getUserQueryWatches() {
		return userQueryWatches;
	}

	public void setUserQueryWatches(LinkedHashMap<String, Boolean> userQueryWatches) {
		this.userQueryWatches = userQueryWatches;
	}

	public LinkedHashMap<String, Boolean> getProjectQueryWatches() {
		return projectQueryWatches;
	}

	public void setProjectQueryWatches(LinkedHashMap<String, Boolean> projectQueryWatches) {
		this.projectQueryWatches = projectQueryWatches;
	}

}
