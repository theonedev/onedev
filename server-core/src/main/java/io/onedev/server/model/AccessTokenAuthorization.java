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
		indexes={@Index(columnList="token_id"), @Index(columnList="project_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"token_id", "project_id", "role_id"})
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class AccessTokenAuthorization extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Immutable
	private AccessToken token;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Role role;
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public AccessToken getToken() {
		return token;
	}

	public void setToken(AccessToken token) {
		this.token = token;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
		
}
