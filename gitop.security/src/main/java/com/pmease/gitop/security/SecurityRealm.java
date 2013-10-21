package com.pmease.gitop.security;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authz.Permission;

import com.pmease.commons.shiro.AbstractRealm;
import com.pmease.commons.shiro.AbstractUser;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.Authorization;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.ObjectPermission;
import com.pmease.gitop.core.permission.operation.GeneralOperation;

@Singleton
public class SecurityRealm extends AbstractRealm {

    private final UserManager userManager;
    
    private final ProjectManager projectManager;

    @Inject
    public SecurityRealm(UserManager userManager, ProjectManager projectManager) {
        this.userManager = userManager;
        this.projectManager = projectManager;
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
                if (userId != 0L) {
                    User user = userManager.load(userId);
                    // Administrator can do anything
                    if (user.isRoot() || user.isAdmin()) return true;

                    // One can do anything against its belongings
                    if (ObjectPermission.ofUserAdmin(user).implies(permission)) return true;

                    for (Team team : user.getTeams()) {
                        Permission userPermission = new ObjectPermission(team.getOwner(), team.getAuthorizedOperation());
                        if (userPermission.implies(permission))
                            return true;
                        
                        for (Authorization authorization: team.getAuthorizations()) {
                            Permission projectPermission = new ObjectPermission(
                                    authorization.getProject(), authorization.getAuthorizedOperation());
                            if (projectPermission.implies(permission))
                                return true;
                        }
                    }

                    for (Project each : projectManager.query()) {
                        ObjectPermission projectPermission =
                                new ObjectPermission(each, each.getDefaultAuthorizedOperation());
                        if (projectPermission.implies(permission)) return true;
                    }

                    for (User each : userManager.query()) {
                        ObjectPermission userPermission =
                                new ObjectPermission(each, each.getDefaultAuthorizedOperation());
                        if (userPermission.implies(permission)) return true;
                    }
                }

                // check if is public access to projects
                for (Project each : projectManager.findPublic()) {
                    ObjectPermission projectPermission =
                            new ObjectPermission(each, GeneralOperation.READ);
                    if (projectPermission.implies(permission)) return true;
                }

                // check if is public access to accounts
                for (User each : userManager.findPublic()) {
                    ObjectPermission userPermission =
                            new ObjectPermission(each, GeneralOperation.READ);
                    if (userPermission.implies(permission)) return true;
                }

                return false;
            }

        });

        return permissions;        
    }

}
