package io.onedev.server.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(
		indexes={@Index(columnList="o_ai_id"), @Index(columnList="o_project_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_ai_id", "o_project_id"})
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
