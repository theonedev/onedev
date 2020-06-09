package io.onedev.server.plugin.authenticator.ldap;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable(name="Active Directory", order=100)
public class ActiveDirectoryAuthenticator extends LdapAuthenticator {

	private static final long serialVersionUID = 1L;

	private String groupSearchBase;
	
    @Editable(order=100, name="LDAP URL", description=
    	"Specifies LDAP URL of the Active Directory server, for example: <i>ldap://ad-server</i>, or <i>ldaps://ad-server</i>")
    @NotEmpty
	@Override
	public String getLdapUrl() {
		return super.getLdapUrl();
	}

	@Override
	public void setLdapUrl(String ldapUrl) {
		super.setLdapUrl(ldapUrl);
	}

	@Editable(order=300, description=""
			+ "To authenticate user against Active Directory and retrieve associated attributes and groups, OneDev "
			+ "would have to first authenticate itself against the Active Directory server and OneDev does that by "
			+ "sending 'manager' DN and password. The manager DN should be specified in form of "
			+ "<i>&lt;account name&gt;@&lt;domain&gt;</i>, for instance: <i>onedev@example.com</i>")
	@NotEmpty
	@Override
	public String getManagerDN() {
		return super.getManagerDN();
	}

	@Override
	public void setManagerDN(String managerDN) {
		super.setManagerDN(managerDN);
	}

	@Editable(order=500, description=
		"Specifies the base node for user search. For example: <i>cn=Users, dc=example, dc=com</i>")
	@NotEmpty
	@Override
	public String getUserSearchBase() {
		return super.getUserSearchBase();
	}

	@Override
	public void setUserSearchBase(String userSearchBase) {
		super.setUserSearchBase(userSearchBase);
	}

	@Override
	public String getUserSearchFilter() {
		return "(&(sAMAccountName={0})(objectclass=user))";
	}
    
	@Override
	public void setUserSearchFilter(String userSearchFilter) {
		super.setUserSearchFilter(userSearchFilter);
	}

	@Editable(order=1000, description=""
			+ "Optionally specify group search base if you want to retrieve group membership information "
			+ "of the user. For example: <i>cn=Users, dc=example, dc=com</i>. To give appropriate "
			+ "permissions to a Active Directory group, a OneDev group with same name should be defined. "
			+ "Leave empty to manage group memberships at OneDev side")
	@NameOfEmptyValue("Do not retrieve groups")
	public String getGroupSearchBase() {
		return groupSearchBase;
	}

	public void setGroupSearchBase(String groupSearchBase) {
		this.groupSearchBase = groupSearchBase;
	}

	@Override
	public GroupRetrieval getGroupRetrieval() {
		if (getGroupSearchBase() != null) {
			SearchGroupsUsingFilter groupRetrieval = new SearchGroupsUsingFilter();
			groupRetrieval.setGroupSearchBase(getGroupSearchBase());
			groupRetrieval.setGroupSearchFilter("(&(member:1.2.840.113556.1.4.1941:={0})(objectclass=group))");
			return groupRetrieval;
		} else {
			return new DoNotRetrieveGroups();
		}
	}

}
