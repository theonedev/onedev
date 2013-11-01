package com.pmease.gitop.security;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authz.Permission;

import com.pmease.commons.shiro.AbstractRealm;
import com.pmease.commons.shiro.AbstractUser;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.Authorization;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.ObjectPermission;
import com.pmease.gitop.core.permission.operation.PrivilegedOperation;

@Singleton
public class SecurityRealm extends AbstractRealm {

    private final UserManager userManager;
    
    private final TeamManager teamManager;

    @Inject
    public SecurityRealm(UserManager userManager, TeamManager teamManager) {
        this.userManager = userManager;
        this.teamManager = teamManager;
    }

    @Override
    protected AbstractUser getUserByName(String userName) {
        return userManager.find(userName);
    }

    @Override
    protected Collection<Permission> permissionsOf(final Long userId) {
        Collection<Permission> permissions = new ArrayList<Permission>();

        /*
         * Instead of returning all permissions of the user, we return a customized
         * permission object so that we can control the authorization procedure for 
         * optimization purpose. For instance, we may check information contained 
         * in the permission being checked and if it means authorization of certain 
         * object, we can then only load authorization information of that object.
         */
        permissions.add(new Permission() {

            @Override
            public boolean implies(Permission permission) {
            	if (permission instanceof ObjectPermission) {
            		ObjectPermission objectPermission = (ObjectPermission) permission;
            		Collection<Team> teams = new ArrayList<>();
	                if (userId != 0L) {
	                    User user = userManager.load(userId);
	                    // Administrator can do anything
	                    if (user.isRoot() || user.isAdmin()) return true;
	
	                    teams.addAll(user.getTeams());
	                    
	                    if (getUser(objectPermission) != null)
	                    	teams.add(teamManager.getLoggedIn(getUser(objectPermission)));
	                }

                    if (getUser(objectPermission) != null)
                    	teams.add(teamManager.getAnonymous(getUser(objectPermission)));

                    for (Team team: teams) {
                    	PrivilegedOperation operation = null;
                    	for (Authorization authorization: team.getAuthorizations()) {
                    		if (authorization.getProject().has(objectPermission.getObject())) {
                    			operation = authorization.getOperation();
                    			break;
                    		}
                    	}
                    	if (operation == null && team.getOwner().has(objectPermission.getObject())) {
                    		operation = team.getAuthorizedOperation();
                    	}
                    	
                    	if (operation != null && operation.can(objectPermission.getOperation()))
                    		return true;
                    }
                    
            	} 
            	return false;
            }
        });

        return permissions;        
    }
    
    private User getUser(ObjectPermission permission) {
        if (permission.getObject() instanceof Project) {
        	Project project = (Project) permission.getObject();
        	return project.getOwner();
        } else if (permission.getObject() instanceof User) {
        	return (User) permission.getObject();
        } else {
        	return null;
        }
    }

}
