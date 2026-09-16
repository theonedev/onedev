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

import io.onedev.server.rest.annotation.Immutable;

@Entity
@Table(
		indexes={@Index(columnList="project_id"), @Index(columnList="role_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"project_id", "role_id"})
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class BaseAuthorization extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_PROJECT = "project";
	
	public static final String PROP_ROLE = "role";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Immutable
	private Project project;
		
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Role role;
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
	
}
