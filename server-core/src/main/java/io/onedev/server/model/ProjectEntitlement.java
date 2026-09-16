package io.onedev.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(
		indexes={@Index(columnList="ai_id"), @Index(columnList="project_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"ai_id", "project_id"})
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class ProjectEntitlement extends AbstractEntity {

	private static final long serialVersionUID = 1L;
		
	public static String PROP_AI = "ai";
	
	public static String PROP_PROJECT = "project";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User ai;
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public User getAi() {
		return ai;
	}

	public void setAi(User ai) {
		this.ai = ai;
	}
	
}
