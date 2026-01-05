package io.onedev.server.web.component.user.aisetting;

import static io.onedev.server.model.User.Type.ORDINARY;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.GroupChoice;
import io.onedev.server.annotation.ProjectChoice;
import io.onedev.server.annotation.UserChoice;
import io.onedev.server.model.User;
import io.onedev.server.service.UserService;

@Editable
public class EntitlementEditBean implements Serializable{
	
    private static final long serialVersionUID = 1L;

    private boolean entitleToAll;

    private List<String> entitledUsers;

    private List<String> entitledGroups;

    private List<String> entitledProjects;

    @Editable(order=100, name="Entitle to All Users and Projects")
    public boolean isEntitleToAll() {
        return entitleToAll;
    }

    public void setEntitleToAll(boolean entitleToAll) {
        this.entitleToAll = entitleToAll;
    }

    @Editable(order=200, description="Entitled users will be able to access this AI service")
    @UserChoice("getUsers")
    @DependsOn(property="entitleToAll", value="false")
    public List<String> getEntitledUsers() {
        return entitledUsers;
    }

    public void setEntitledUsers(List<String> entitledUsers) {
        this.entitledUsers = entitledUsers;
    }
    
    @SuppressWarnings("unused")
    private static List<User> getUsers() {
        var cache = OneDev.getInstance(UserService.class).cloneCache();
        return cache.getUsers(it->!it.isDisabled() && it.getType() == ORDINARY)
                .stream()
                .sorted(cache.comparingDisplayName())
                .collect(Collectors.toList());
    }

    @Editable(order=300, description="All members of entitled groups will be able to access this AI service")
    @GroupChoice
    @DependsOn(property="entitleToAll", value="false")
    public List<String> getEntitledGroups() {
        return entitledGroups;
    }

    public void setEntitledGroups(List<String> entitledGroups) {
        this.entitledGroups = entitledGroups;
    }

    @Editable(order=400, description="Entitled projects and all the sub-projects will be able to access this AI service")
    @ProjectChoice
    @DependsOn(property="entitleToAll", value="false")
    public List<String> getEntitledProjects() {
        return entitledProjects;
    }

    public void setEntitledProjects(List<String> entitledProjects) {
        this.entitledProjects = entitledProjects;
    }

}