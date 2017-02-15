package com.gitplex.server.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import com.gitplex.launcher.loader.AppLoader;
import com.gitplex.server.GitPlex;
import com.gitplex.server.entity.Account;
import com.gitplex.server.entity.Depot;
import com.gitplex.server.entity.OrganizationMembership;
import com.gitplex.server.entity.Team;
import com.gitplex.server.entity.TeamAuthorization;
import com.gitplex.server.entity.TeamMembership;
import com.gitplex.server.entity.UserAuthorization;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.manager.OrganizationMembershipManager;
import com.gitplex.server.manager.TeamMembershipManager;
import com.gitplex.server.manager.UserAuthorizationManager;
import com.gitplex.server.security.privilege.AccountPrivilege;
import com.gitplex.server.security.privilege.DepotPrivilege;

@Singleton
public class SecurityRealm extends AuthorizingRealm {

    private final AccountManager accountManager;
    
    private final OrganizationMembershipManager organizationMembershipManager;
    
    private final TeamMembershipManager teamMembershipManager;
    
	@Inject
    public SecurityRealm(AccountManager userManager, 
    		OrganizationMembershipManager organizationMembershipManager, 
    		TeamMembershipManager teamMembershipManager) {
	    PasswordMatcher passwordMatcher = new PasswordMatcher();
	    passwordMatcher.setPasswordService(AppLoader.getInstance(PasswordService.class));
		setCredentialsMatcher(passwordMatcher);
		
    	this.accountManager = userManager;
    	this.organizationMembershipManager = organizationMembershipManager;
    	this.teamMembershipManager = teamMembershipManager;
    }

	@SuppressWarnings("serial")
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		final Long userId = (Long) principals.getPrimaryPrincipal();
		
		return new AuthorizationInfo() {
			
			@Override
			public Collection<String> getStringPermissions() {
				return new HashSet<>();
			}
			
			@Override
			public Collection<String> getRoles() {
				return new HashSet<>();
			}
			
			@Override
			public Collection<Permission> getObjectPermissions() {
				return permissionsOf(userId);
			}
		};
	}
	
	@Override
	protected final AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    	return accountManager.findByName(((UsernamePasswordToken) token).getUsername());
	}
	
	/**
	 * Get collection of permissions for specified user identifier.
	 * 
	 * @param userId
	 *         identifier of user to get permissions for. Specifically, value <tt>0</tt> stands for 
	 *         anonymous user
	 * @return
	 *         collection of permissions associated with specified user identifier
	 */
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
            				&& DepotPrivilege.READ.can(objectPermission.getPrivilege())) {
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
		                    			accountPrivilege = AccountPrivilege.ACCESS;
		                    		if (accountPrivilege.can(objectPermission.getPrivilege()))
		                    			return true;
		                    	}
			                    if (checkDepot != null) {
			                    	if (organizationMembership != null 
			                    			&& checkAccount.getDefaultPrivilege().can(objectPermission.getPrivilege())) {
			                    		return true;
			                    	}
			                    	UserAuthorizationManager userAuthorizationManager = 
			                    			GitPlex.getInstance(UserAuthorizationManager.class);
			                    	UserAuthorization userAuthorization = userAuthorizationManager.find(user, checkDepot);
			                    	if (userAuthorization != null 
			                    			&& userAuthorization.getPrivilege().can(objectPermission.getPrivilege())) {
			                    		return true;
			                    	}
			                    		
	                				Set<Team> teams = new HashSet<>();
	                				for (TeamMembership teamMembership: 
	                						teamMembershipManager.findAll(checkAccount, user)) {
	                					teams.add(teamMembership.getTeam());
	                				}
		                			for (TeamAuthorization authorization: checkDepot.getAuthorizedTeams()) {
		                				if (authorization.getPrivilege().can(objectPermission.getPrivilege())
		                						&& teams.contains(authorization.getTeam())) {
		                					return true;
		                				}
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
