package io.onedev.server.web.component.user.accesstoken;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Secret;
import io.onedev.server.annotation.ShowCondition;
import io.onedev.server.model.AccessToken;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.util.UserAware;
import io.onedev.server.web.util.WicketUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Editable
public class AccessTokenEditBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String value;
	
	private boolean hasOwnerPermissions;

	private List<AuthorizationBean> authorizations = new ArrayList<>();
	
	private Date expireDate;

	@Editable(order=100)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200)
	@Secret(displayChars=4)
	@NotEmpty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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
	@ShowCondition("isHasOwnerPermissionsDisabled")
	public List<AuthorizationBean> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(List<AuthorizationBean> authorizations) {
		this.authorizations = authorizations;
	}
	
	@SuppressWarnings("unused")
	private static boolean isHasOwnerPermissionsDisabled() {
		return !(boolean) EditContext.get().getInputValue("hasOwnerPermissions");
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
		bean.setName(token.getName());
		bean.setValue(token.getValue());
		bean.setHasOwnerPermissions(token.isHasOwnerPermissions());
		bean.setExpireDate(token.getExpireDate());

		var user = WicketUtils.findInnermost(ComponentContext.get().getComponent(), UserAware.class).getUser();
		for (var authorization: token.getAuthorizations()) {
			if (SecurityUtils.canManageProject(user.asSubject(), authorization.getProject())) {
				var authorizationBean = new AuthorizationBean();
				authorizationBean.setProjectPath(authorization.getProject().getPath());
				authorizationBean.setRoleName(authorization.getRole().getName());
				bean.getAuthorizations().add(authorizationBean);
			}
		}
		return bean;
	}
}
