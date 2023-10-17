package io.onedev.server.ee.dashboard;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ShowCondition;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.MembershipManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.facade.UserFacade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Editable
public class DashboardShareBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String PROP_FOR_EVERYONE = "forEveryone";
	
	private boolean forEveryone;
	
	private List<String> shareGroups = new ArrayList<>();
	
	private List<String> shareUsers = new ArrayList<>();

	@Editable(order=100, name="To Everyone")
	public boolean isForEveryone() {
		return forEveryone;
	}

	public void setForEveryone(boolean forEveryone) {
		this.forEveryone = forEveryone;
	}

	@SuppressWarnings("unused")
	private static boolean isNotForEveryone() {
		return !(boolean) EditContext.get().getInputValue("forEveryone");
	}
	
	@Editable(order=200, name="Share with Groups", descriptionProvider="getShareGroupsDescription")
	@ShowCondition("isNotForEveryone")
	@ChoiceProvider("getGroupChoices")
	public List<String> getShareGroups() {
		return shareGroups;
	}

	public void setShareGroups(List<String> shareGroups) {
		this.shareGroups = shareGroups;
	}
	
	@SuppressWarnings("unused")
	private static String getShareGroupsDescription() {
		if (SecurityUtils.isAdministrator()) {
			return "Share dashboard with specified groups";
		} else {
			return "Share this dashboard with all members of specified groups. Note that as a non-"
					+ "admin user you can only share with groups you are currently a member of";
		}
	}
	
	@SuppressWarnings("unused")
	private static List<String> getGroupChoices() {
		List<String> groups = new ArrayList<>();
		if (SecurityUtils.isAdministrator()) {
			for (Group group: OneDev.getInstance(GroupManager.class).query())
				groups.add(group.getName());
		} else { 
			for (Group group: SecurityUtils.getUser().getGroups()) 
				groups.add(group.getName());
		}
		Collections.sort(groups);
		
		return groups;
	}

	@Editable(order=300, name="Share with Users", descriptionProvider="getShareUsersDescription")
	@ShowCondition("isNotForEveryone")
	@ChoiceProvider("getUserChoices")
	public List<String> getShareUsers() {
		return shareUsers;
	}

	public void setShareUsers(List<String> shareUsers) {
		this.shareUsers = shareUsers;
	}

	@SuppressWarnings("unused")
	private static String getShareUsersDescription() {
		if (SecurityUtils.isAdministrator()) {
			return "Share dashboard with specified users";
		} else {
			return "Share this dashboard with specified users. Note that as a non-admin user you "
					+ "can only share with members of groups you are currently a member of";
		}
	}
	
	@SuppressWarnings("unused")
	private static List<String> getUserChoices() {
		Set<String> users = new TreeSet<>();
		if (SecurityUtils.isAdministrator()) {
			for (UserFacade user: OneDev.getInstance(UserManager.class).cloneCache().values()) {
				if (user.getId() > 0)
					users.add(user.getName());
			}
		} else { 
			for (User user: OneDev.getInstance(MembershipManager.class).queryMembers(SecurityUtils.getUser()))  
				users.add(user.getName());
		}
		users.remove(SecurityUtils.getUser().getName());
		
		List<String> choices = new ArrayList<>(users);
		Collections.sort(choices);
		return choices;
	}
	
}
