package com.pmease.gitop.core.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authz.Permission;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.util.namedentity.EntityLoader;
import com.pmease.commons.util.namedentity.NamedEntity;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.ObjectPermission;
import com.pmease.gitop.core.permission.operation.GeneralOperation;
import com.pmease.gitop.core.validation.UserNameReservation;

@Singleton
public class DefaultUserManager extends AbstractGenericDao<User> implements UserManager {

    private volatile Long rootUserId;

    private final Set<UserNameReservation> nameReservations;

    private final ProjectManager projectManager;

    @Inject
    public DefaultUserManager(GeneralDao generalDao, ProjectManager projectManager,
            Set<UserNameReservation> nameReservations) {
        super(generalDao);

        this.projectManager = projectManager;
        this.nameReservations = nameReservations;
    }

    @Sessional
    @Override
    public User getRootUser() {
        User rootUser;
        if (rootUserId == null) {
            // The first created user should be root user
            rootUser = find(null, new Order[] {Order.asc("id")});
            Preconditions.checkNotNull(rootUser);
            rootUserId = rootUser.getId();
        } else {
            rootUser = load(rootUserId);
        }
        return rootUser;
    }

    @Sessional
    @Override
    public User find(String userName) {
        return find(new Criterion[] {Restrictions.eq("name", userName)});
    }

    @Override
    public Collection<User> findPublic() {
        return query(new Criterion[] {Restrictions.eq("publiclyAccessible", true)});
    }

    @Override
    public EntityLoader asEntityLoader() {
        return new EntityLoader() {

            @Override
            public NamedEntity get(final Long id) {
                final User user = DefaultUserManager.this.get(id);
                if (user != null) {
                    return new NamedEntity() {

                        @Override
                        public Long getId() {
                            return id;
                        }

                        @Override
                        public String getName() {
                            return user.getName();
                        }

                    };
                } else {
                    return null;
                }
            }

            @Override
            public NamedEntity get(String name) {
                final User user = find(name);
                if (user != null) {
                    return new NamedEntity() {

                        @Override
                        public Long getId() {
                            return user.getId();
                        }

                        @Override
                        public String getName() {
                            return user.getName();
                        }

                    };
                } else {
                    return null;
                }
            }

        };
    }

    @Override
    public Set<String> getReservedNames() {
        Set<String> reservedNames = new HashSet<String>();
        for (UserNameReservation each : nameReservations)
            reservedNames.addAll(each.getReserved());

        return reservedNames;
    }

    @Override
    public Collection<Permission> permissionsOf(final User user) {
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
                if (user != null) {
                    // Administrator can do anything
                    if (user.isRoot() || user.isAdmin()) return true;

                    // One can do anything against its belongings
                    if (ObjectPermission.ofUserAdmin(user).implies(permission)) return true;

                    for (Team team : user.getTeams()) {
                        if (team.implies(permission)) return true;
                    }

                    for (Project each : projectManager.query()) {
                        ObjectPermission projectPermission =
                                new ObjectPermission(each, each.getDefaultAuthorizedOperation());
                        if (projectPermission.implies(permission)) return true;
                    }

                    for (User each : query()) {
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
                for (User each : findPublic()) {
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
