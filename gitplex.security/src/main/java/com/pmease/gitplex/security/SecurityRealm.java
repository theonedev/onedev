package com.pmease.gitplex.security;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.gitplex.core.GitPlex;

import org.apache.shiro.authz.Permission;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.shiro.AbstractRealm;
import com.pmease.commons.shiro.AbstractUser;
import com.pmease.gitplex.core.manager.TeamManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Authorization;
import com.pmease.gitplex.core.model.Membership;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.Team;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.core.permission.operation.PrivilegedOperation;

@Singleton
public class SecurityRealm extends AbstractRealm {

    private final Dao dao;
    
    private final UserManager userManager;
    
    private final TeamManager teamManager;
    
    @Inject
    public SecurityRealm(Dao dao, UserManager userManager, TeamManager teamManager) {
    	this.dao = dao;
    	this.userManager = userManager;
    	this.teamManager = teamManager;
    }

    @Override
    protected AbstractUser getUserByName(String userName) {
        return userManager.findByName(userName);
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
	                    User user = dao.get(User.class, userId);
	                    if (user != null) {
		                    // Administrator can do anything
		                    if (user.equals(GitPlex.getInstance(UserManager.class).getRoot()) || user.isAdmin()) return true;
		
		                    for (Membership membership: user.getMemberships())
		                    	teams.add(membership.getTeam());
		                    
		                    if (getUser(objectPermission) != null)
		                    	teams.add(teamManager.getLoggedIn(getUser(objectPermission)));
	                    }
	                }

                    if (getUser(objectPermission) != null)
                    	teams.add(teamManager.getAnonymous(getUser(objectPermission)));

                    for (Team team: teams) {
                    	PrivilegedOperation operation = null;
                    	for (Authorization authorization: team.getAuthorizations()) {
                    		if (authorization.getRepository().has(objectPermission.getObject())) {
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
        if (permission.getObject() instanceof Repository) {
        	Repository repository = (Repository) permission.getObject();
        	return repository.getOwner();
        } else if (permission.getObject() instanceof User) {
        	return (User) permission.getObject();
        } else {
        	return null;
        }
    }

}
