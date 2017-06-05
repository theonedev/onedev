package com.gitplex.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.gitplex.server.security.ProjectPrivilege;
import com.gitplex.server.util.facade.GroupAuthorizationFacade;

@Entity
@Table(
		indexes={@Index(columnList="g_group_id"), @Index(columnList="g_project_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"g_group_id", "g_project_id"})
})
public class GroupAuthorization extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Group group;
	
	@Column(nullable=false)
	private ProjectPrivilege privilege = ProjectPrivilege.READ;
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public ProjectPrivilege getPrivilege() {
		return privilege;
	}

	public void setPrivilege(ProjectPrivilege privilege) {
		this.privilege = privilege;
	}

	public GroupAuthorizationFacade getFacade() {
		return new GroupAuthorizationFacade(this);
	}
	
}
