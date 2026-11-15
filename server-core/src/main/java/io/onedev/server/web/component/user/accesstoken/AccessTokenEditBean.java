package io.onedev.server.web.component.user.accesstoken;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.AccessToken;
import io.onedev.server.model.AccessTokenAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.model.Role;
import io.onedev.server.security.SecurityUtils;

@Editable
public class AccessTokenEditBean implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private String description;
	
	private boolean hasOwnerPermissions;

	private List<AccessTokenAuthorizationBean> authorizations = new ArrayList<>();
	
	private Date expireDate;

	@Editable(order=100)
	@NotEmpty
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Editable(order=250, description = "Enable this if the access token has same permissions as the owner")
	public boolean isHasOwnerPermissions() {
		return hasOwnerPermissions;
	}

	public void setHasOwnerPermissions(boolean hasOwnerPermissions) {
		this.hasOwnerPermissions = hasOwnerPermissions;
	}
	
	@Editable(order=300, name="Authorized Projects", description = "Only projects manageable by access token owner can be authorized")
	@Size(min=1, message = "At least one project should be authorized")
	@DependsOn(property="hasOwnerPermissions", value="false")
	public List<AccessTokenAuthorizationBean> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(List<AccessTokenAuthorizationBean> authorizations) {
		this.authorizations = authorizations;
	}
	
	@Editable(order=400, placeholder = "Never expire")
	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}
	
	public static AccessTokenEditBean of(AccessToken token) {
		var bean = new AccessTokenEditBean();
		bean.setDescription(token.getDescription());
		bean.setHasOwnerPermissions(token.isHasOwnerPermissions());
		bean.setExpireDate(token.getExpireDate());

		Map<Project, List<Role>> projectRoles = new HashMap<>();
		for (AccessTokenAuthorization authorization : token.getAuthorizations()) {
			Project project = authorization.getProject();
			Role role = authorization.getRole();			
			projectRoles.computeIfAbsent(project, k -> new ArrayList<>()).add(role);
		}
		
		for (var entry: projectRoles.entrySet()) {
			Project project = entry.getKey();
			List<Role> roles = entry.getValue();
			if (SecurityUtils.canManageProject(token.getOwner().asSubject(), project)) {
				var authorizationBean = new AccessTokenAuthorizationBean();
				authorizationBean.setProjectPath(project.getPath());
				authorizationBean.setRoleNames(roles.stream().map(it->it.getName()).collect(Collectors.toList()));
				bean.getAuthorizations().add(authorizationBean);
			}
		}
		return bean;
	}
}
