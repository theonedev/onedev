package com.pmease.gitplex.core.security;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authz.Permission;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.shiro.AbstractRealm;
import com.pmease.commons.shiro.AbstractUser;
import com.pmease.gitplex.core.entity.Authorization;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.OrganizationMembership;
import com.pmease.gitplex.core.entity.Team;
import com.pmease.gitplex.core.entity.TeamMembership;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.TeamManager;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.core.permission.privilege.Privilege;

@Singleton
public class SecurityRealm extends AbstractRealm {

    private final AccountManager userManager;
    
    private final TeamManager teamManager;
    
    @Inject
    public SecurityRealm(AccountManager userManager, TeamManager teamManager) {
    	this.userManager = userManager;
    	this.teamManager = teamManager;
    }

    @Override
    protected AbstractUser getUserByName(String userName) {
    	return userManager.findByName(userName);
    }

    @Override
    protected Collection<Permission> permissionsOf(Long userId) {
        Collection<Permission> permissions = new ArrayList<>();

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
	                    Account user = userManager.get(userId);
	                    if (user != null) {
		                    // administrator can do anything
		                    if (user.isRoot() || user.isAdmin())
		                    	return true;

		                    Account checkAccount = getAccount(objectPermission);
		                    
		                    // if permission is to check privilege of account belongings		                    
		                    if (checkAccount != null) {  
		                    	// I can do anything against my own account
		                    	if (checkAccount.equals(user)) 
		                    		return true;
		                    	
			                	for (OrganizationMembership membership: user.getOrganizationMemberships()) {
			                		
			                	}
		                    }
	                    }
	                }

                    for (Team team: teams) {
                    	Privilege operation = null;
                    	for (Authorization authorization: team.getAuthorizations()) {
                    		if (authorization.getDepot().has(objectPermission.getObject())) {
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
    
    private Account getAccount(ObjectPermission permission) {
        if (permission.getObject() instanceof Depot) {
        	Depot depot = (Depot) permission.getObject();
        	return depot.getOwner();
        } else if (permission.getObject() instanceof Account) {
        	return (Account) permission.getObject();
        } else {
        	return null;
        }
    }

}
