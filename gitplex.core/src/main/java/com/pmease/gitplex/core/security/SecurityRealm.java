package com.pmease.gitplex.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authz.Permission;

import com.pmease.commons.shiro.AbstractRealm;
import com.pmease.commons.shiro.AbstractUser;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.TeamAuthorization;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.OrganizationMembership;
import com.pmease.gitplex.core.entity.Team;
import com.pmease.gitplex.core.entity.TeamMembership;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.OrganizationMembershipManager;
import com.pmease.gitplex.core.manager.TeamMembershipManager;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.core.permission.privilege.AccountPrivilege;
import com.pmease.gitplex.core.permission.privilege.DepotPrivilege;

@Singleton
public class SecurityRealm extends AbstractRealm {

    private final AccountManager accountManager;
    
    private final OrganizationMembershipManager organizationMembershipManager;
    
    private final TeamMembershipManager teamMembershipManager;
    
    @Inject
    public SecurityRealm(AccountManager userManager, 
    		OrganizationMembershipManager organizationMembershipManager, 
    		TeamMembershipManager teamMembershipManager) {
    	this.accountManager = userManager;
    	this.organizationMembershipManager = organizationMembershipManager;
    	this.teamMembershipManager = teamMembershipManager;
    }

    @Override
    protected AbstractUser getUserByName(String userName) {
    	return accountManager.findByName(userName);
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
            		Depot checkDepot = getDepot(objectPermission);
            		if (checkDepot != null 
            				&& checkDepot.isPublicRead() 
            				&& DepotPrivilege.READ.can(objectPermission.getOperation())) {
            			return true;
            		}
	                if (userId != 0L) {
	                    Account user = accountManager.get(userId);
	                    if (user != null) {
		                    // administrator can do anything
		                    if (user.isAdministrator())
		                    	return true;

		                    Account checkAccount = getAccount(objectPermission);
		                    
		                    // if permission is to check privilege of account belongings		                    
		                    if (checkAccount != null) {  
		                    	// I can do anything against my own account
		                    	if (checkAccount.equals(user)) 
		                    		return true;

		                    	OrganizationMembership organizationMembership = 
		                    			organizationMembershipManager.find(checkAccount, user);
		                    	if (organizationMembership != null) {
		                    		AccountPrivilege accountPrivilege;
		                    		if (organizationMembership.isAdmin())
		                    			accountPrivilege = AccountPrivilege.ADMIN;
		                    		else
		                    			accountPrivilege = AccountPrivilege.MEMBER;
		                    		if (accountPrivilege.can(objectPermission.getOperation()))
		                    			return true;
		                    	}
		                    }
		                    if (checkDepot != null) {
		                    	if (checkAccount.getDefaultPrivilege().can(objectPermission.getOperation())) {
		                    		return true;
		                    	}
                				Set<Team> teams = new HashSet<>();
                				for (TeamMembership teamMembership: 
                						teamMembershipManager.query(checkDepot.getAccount(), user)) {
                					teams.add(teamMembership.getTeam());
                				}
	                			for (TeamAuthorization authorization: checkDepot.getAuthorizedTeams()) {
	                				if (authorization.getPrivilege().can(objectPermission.getOperation())
	                						&& teams.contains(authorization.getTeam())) {
	                					return true;
	                				}
	                			}
		                    }
	                    }
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
        	return depot.getAccount();
        } else if (permission.getObject() instanceof Account) {
        	return (Account) permission.getObject();
        } else {
        	return null;
        }
    }

    private Depot getDepot(ObjectPermission permission) {
        if (permission.getObject() instanceof Depot) {
        	Depot depot = (Depot) permission.getObject();
        	return depot;
        } else {
        	return null;
        }
    }
    
}
