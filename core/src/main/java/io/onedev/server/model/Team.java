package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.security.permission.ProjectPrivilege;
import io.onedev.server.util.facade.TeamFacade;
import io.onedev.server.web.editable.annotation.Editable;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
@Table(
		indexes={@Index(columnList="g_project_id"), @Index(columnList="name")}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"g_project_id", "name"})})
public class Team extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String FQN_SEPARATOR = ":";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@Column(nullable=false)
	private String name;
	
	private ProjectPrivilege privilege;
	
	@OneToMany(mappedBy="team", cascade=CascadeType.REMOVE)
	private Collection<Membership> memberships = new ArrayList<>();
	
	private transient Collection<User> members;
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Editable(order=100)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=300)
	@NotNull
	public ProjectPrivilege getPrivilege() {
		return privilege;
	}

	public void setPrivilege(ProjectPrivilege privilege) {
		this.privilege = privilege;
	}

	public Collection<Membership> getMemberships() {
		return memberships;
	}

	public void setMemberships(Collection<Membership> memberships) {
		this.memberships = memberships;
	}

	public Collection<User> getMembers() {
		if (members == null) {
			members = new HashSet<>();
			for (Membership membership: getMemberships()) {
				members.add(membership.getUser());
			}
		}
		return members;
	}

	@Override
	public int compareTo(AbstractEntity entity) {
		Team team = (Team) entity;
		return getName().compareTo(team.getName());
	}

	public TeamFacade getFacade() {
		return new TeamFacade(this);
	}
	
	public String getFQN() {
		return getProject().getName() + FQN_SEPARATOR + getName();
	}
	
}
