package com.pmease.gitop.core.manager.impl;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.util.namedentity.EntityLoader;
import com.pmease.commons.util.namedentity.NamedEntity;
import com.pmease.gitop.core.manager.MembershipManager;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.Membership;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.operation.GeneralOperation;
import com.pmease.gitop.core.validation.UserNameReservation;

@Singleton
public class DefaultUserManager extends AbstractGenericDao<User> implements UserManager {

    private volatile Long rootId;

    private final Set<UserNameReservation> nameReservations;

    private final TeamManager teamManager;
    private final MembershipManager membershipManager;
    
    @Inject
    public DefaultUserManager(GeneralDao generalDao, Set<UserNameReservation> nameReservations,
    		TeamManager teamManager, MembershipManager membershipManager) {
        super(generalDao);

        this.nameReservations = nameReservations;
        this.teamManager = teamManager;
        this.membershipManager = membershipManager;
    }

    @Transactional
    @Override
	public void save(User user) {
    	boolean isNew = user.isNew();
    	super.save(user);
    	
    	if (isNew) {
        	Team team = new Team();
        	team.setOwner(user);
        	team.setAuthorizedOperation(GeneralOperation.NO_ACCESS);
        	team.setName(Team.ANONYMOUS);
        	teamManager.save(team);
        	
        	team = new Team();
        	team.setOwner(user);
        	team.setName(Team.LOGGEDIN);
        	team.setAuthorizedOperation(GeneralOperation.NO_ACCESS);
        	teamManager.save(team);
        	
        	team = new Team();
        	team.setOwner(user);
        	team.setName(Team.OWNERS);
        	team.setAuthorizedOperation(GeneralOperation.ADMIN);
        	teamManager.save(team);
        	
        	Membership membership = new Membership();
        	membership.setTeam(team);
        	membership.setUser(user);
        	membershipManager.save(membership);
    	}
    }
    
    @Sessional
    @Override
    public User getRoot() {
        User root;
        if (rootId == null) {
            // The first created user should be root user
            root = find(null, new Order[] {Order.asc("id")});
            Preconditions.checkNotNull(root);
            rootId = root.getId();
        } else {
            root = load(rootId);
        }
        return root;
    }

    @Sessional
    @Override
    public User find(String userName) {
        return find(new Criterion[] {Restrictions.eq("name", userName)});
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

}
