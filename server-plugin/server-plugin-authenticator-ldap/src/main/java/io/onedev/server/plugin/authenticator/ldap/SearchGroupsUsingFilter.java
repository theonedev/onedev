package io.onedev.server.plugin.authenticator.ldap;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=300)
public class SearchGroupsUsingFilter implements GroupRetrieval {
	
	private static final long serialVersionUID = 1L;

	private String groupSearchBase;
	
	private String groupSearchFilter;

	private String groupNameAttribute = "cn";
	
	@Editable(order=100, description=
		"In case user group membership maintained at group side, this property specifies " +
		"base node for group search. For example: <i>ou=groups, dc=example, dc=com</i>")
	@NotEmpty
	public String getGroupSearchBase() {
		return groupSearchBase;
	}

	public void setGroupSearchBase(String groupSearchBase) {
		this.groupSearchBase = groupSearchBase;
	}

	@Editable(order=200, description=""
			+ "In case user group relationship maintained at group side, this filter is used to determine belonging "
			+ "groups of current user. For example: <i>(&(uniqueMember={0})(objectclass=groupOfUniqueNames))</i>. In "
			+ "this example, <i>{0}</i> represents DN of current user")
	@NotEmpty
	public String getGroupSearchFilter() {
		return groupSearchFilter;
	}

	public void setGroupSearchFilter(String groupSearchFilter) {
		this.groupSearchFilter = groupSearchFilter;
	}

	@Editable(order=300, description=""
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

