package io.onedev.server.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
		indexes={@Index(columnList="g_project_id"), @Index(columnList="g_user_id"), 
				@Index(columnList="type"), @Index(columnList="source"), @Index(columnList="description")},
		uniqueConstraints={@UniqueConstraint(columnNames={"g_project_id", "g_user_id", "type", "source", "description"})
})
public class Task extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String TYPE_PULLREQUEST = "Pull Request";
	
	public static final String TYPE_ISSUE = "Issue";

	public static final String DESC_REVIEW = "There are new changes to review";
	
	public static final String DESC_UPDATE = "Someone disapproved current changes";
	
	public static final String DESC_RESOLVE_CONFLICT = "There are merge conflicts";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;
	
	@Column(nullable=false)
	private String type;

	@Column(nullable=false)
	private String source;
	
	@Column(nullable=false)
	private String description;
	
	@Column(nullable=false)
	private Date date = new Date();

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
