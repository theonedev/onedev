package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.ShowCondition;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class Group extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@Column(unique=true, nullable=false)
	private String name;
	
	private String description;
	
	private boolean administrator;
	
	private boolean createProjects = true;

	@OneToMany(mappedBy="group", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<GroupAuthorization> authorizations = new ArrayList<>();
	
	@OneToMany(mappedBy="group", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<Membership> memberships = new ArrayList<>();
	
	private transient Collection<User> members;
	
	@Editable(order=100)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, description="Optionally describe the group")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Editable(order=300, name="Site Administrator")
	public boolean isAdministrator() {
		return administrator;
	}

	public void setAdministrator(boolean administrator) {
		this.administrator = administrator;
	}

	@SuppressWarnings("unused")
	private static boolean isAdministratorDisabled() {
		return !(boolean) EditContext.get().getInputValue("administrator");
	}
	
	@Editable(order=400, description="This permission is required to fork projects")
	@ShowCondition("isAdministratorDisabled")
	public boolean isCreateProjects() {
		return createProjects;
	}

	public void setCreateProjects(boolean createProjects) {
		this.createProjects = createProjects;
	}

	public Collection<GroupAuthorization> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(Collection<GroupAuthorization> authorizations) {
		this.authorizations = authorizations;
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
		Group group = (Group) entity;
		return getName().compareTo(group.getName());
	}
	
}
