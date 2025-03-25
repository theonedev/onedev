package io.onedev.server.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Table(
		indexes={@Index(columnList="o_token_id"), @Index(columnList="o_project_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_token_id", "o_project_id", "o_role_id"})
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class AccessTokenAuthorization extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
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
