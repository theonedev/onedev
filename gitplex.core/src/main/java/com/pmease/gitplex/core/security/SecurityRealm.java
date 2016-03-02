package com.pmease.gitplex.core.security;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authz.Permission;

import com.google.common.base.Preconditions;
import com.pmease.commons.shiro.AbstractRealm;
import com.pmease.commons.shiro.AbstractUser;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.Membership;
import com.pmease.gitplex.core.entity.component.Team;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.core.permission.privilege.DepotPrivilege;

@Singleton
public class SecurityRealm extends AbstractRealm {

    private final AccountManager accountManager;
    
    @Inject
    public SecurityRealm(AccountManager userManager) {
    	this.accountManager = userManager;
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
	                if (userId != 0L) {
	                    Account user = accountManager.get(userId);
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
		                    	
			                	for (Membership membership: user.getOrganizationMemberships()) {
			                		if (membership.getOrganization().equals(checkAccount)) {
			                			if (membership.isAdmin() 
			                					|| checkAccount.getDefaultPrivilege().can(objectPermission.getOperation())) {
			                				return true;
			                			}
			                			Depot checkDepot = getDepot(objectPermission);
			                			if (checkDepot != null) {
				                			for (String teamName: membership.getJoinedTeams()) {
				                				Team team = Preconditions.checkNotNull(checkAccount.getTeams().get(teamName));
				                				DepotPrivilege privilege = team.getAuthorizations().get(checkDepot.getName());
				                				if (privilege != null && privilege.can(objectPermission.getOperation())) {				                					
				                					return true;
				                				}
				                			}
			                			}
			                			break;
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
        	return depot.getOwner();
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
