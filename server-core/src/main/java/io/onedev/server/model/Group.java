package io.onedev.server.model;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ShowCondition;
import io.onedev.server.security.permission.BasePermission;
import io.onedev.server.security.permission.CreateRootProjects;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.SystemAdministration;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.facade.GroupFacade;
import org.apache.shiro.authz.Permission;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jetbrains.annotations.Nullable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class Group extends AbstractEntity implements BasePermission {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_ADMINISTRATOR = "administrator";
	
	public static final String PROP_NAME = "name";

	@Column(unique=true, nullable=false)
	private String name;
	
	private String description;
	
	private boolean administrator;
	
	private boolean createRootProjects;
	
	private boolean enforce2FA;
	
	@OneToMany(mappedBy="group", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<GroupAuthorization> authorizations = new ArrayList<>();
	
	@OneToMany(mappedBy="group", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<DashboardGroupShare> dashboardShares = new ArrayList<>();
	
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

	@Editable(order=300, name="Is Site Admin")
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

	@Editable(order=300, name="Can Create Root Projects", description="Whether or not to allow creating root projects (project without parent)")
	@ShowCondition("isAdministratorDisabled")
	public boolean isCreateRootProjects() {
		return createRootProjects;
	}

	public void setCreateRootProjects(boolean createRootProjects) {
		this.createRootProjects = createRootProjects;
	}

	@Editable(order=400, name="Enforce Two-factor Authentication", description="Check this to enforce "
			+ "all users in this group to set up two-factor authentication upon next login. Users will "
			+ "not be able to disable two-factor authentication themselves if this option is set")
	public boolean isEnforce2FA() {
		return enforce2FA;
	}

	public void setEnforce2FA(boolean enforce2FA) {
		this.enforce2FA = enforce2FA;
	}
	
	public Collection<GroupAuthorization> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(Collection<GroupAuthorization> authorizations) {
		this.authorizations = authorizations;
	}

	public Collection<DashboardGroupShare> getDashboardShares() {
		return dashboardShares;
	}

	public void setDashboardShares(Collection<DashboardGroupShare> dashboardShares) {
		this.dashboardShares = dashboardShares;
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
	public GroupFacade getFacade() {
		return new GroupFacade(getId(), getName());
	}
	
	@Override
	public int compareTo(AbstractEntity entity) {
		Group group = (Group) entity;
		return getName().compareTo(group.getName());
	}

	@Override
	public boolean implies(Permission permission) {
		return getPermissions().stream().anyMatch(it -> it.implies(permission));
	}
	
	private Collection<BasePermission> getPermissions() {
		Collection<BasePermission> permissions = new ArrayList<>(); 
		if (isAdministrator())
			permissions.add(new SystemAdministration());
		if (isCreateRootProjects())
			permissions.add(new CreateRootProjects());
		for (GroupAuthorization authorization: getAuthorizations()) 
			permissions.add(new ProjectPermission(authorization.getProject(), authorization.getRole()));
		return permissions;
	}

	@Override
	public boolean isApplicable(@Nullable User user) {
		return getPermissions().stream().allMatch(it -> it.isApplicable(user));
	}
	
}
