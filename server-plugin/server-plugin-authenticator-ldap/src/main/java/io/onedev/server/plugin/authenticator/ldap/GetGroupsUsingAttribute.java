package io.onedev.server.plugin.authenticator.ldap;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=200)
public class GetGroupsUsingAttribute implements GroupRetrieval {

	private static final long serialVersionUID = 1L;
	
	private String userGroupsAttribute;

	private String groupNameAttribute = "cn";

	@Editable(order=100, description=""
			+ "Specifies name of the attribute inside the user LDAP entry whose value contains distinguished names of "
			+ "belonging groups. For instance some LDAP servers uses attribute <i>memberOf</i> to list groups")
    @NotEmpty
	public String getUserGroupsAttribute() {
		return userGroupsAttribute;
	}

	public void setUserGroupsAttribute(String userGroupsAttribute) {
		this.userGroupsAttribute = userGroupsAttribute;
	}
	
	@Editable(order=200, description=""
			+ "Specifies the attribute containing group name inside the found group LDAP entry. Value of this attribute "
			+ "will be mapped to a OneDev group. This attribute is normally set to <i>cn</i>")
	@NotEmpty
	public String getGroupNameAttribute() {
		return groupNameAttribute;
	}

	public void setGroupNameAttribute(String groupNameAttribute) {
		this.groupNameAttribute = groupNameAttribute;
	}
		
}
