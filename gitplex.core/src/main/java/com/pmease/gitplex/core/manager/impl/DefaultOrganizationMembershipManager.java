package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.OrganizationMembership;
import com.pmease.gitplex.core.entity.TeamMembership;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.OrganizationMembershipManager;
import com.pmease.gitplex.core.manager.TeamMembershipManager;

@Singleton
public class DefaultOrganizationMembershipManager extends AbstractEntityManager<OrganizationMembership> 
		implements OrganizationMembershipManager {

	private final AccountManager accountManager;
	
	private final OrganizationMembershipManager organizationMembershipManager;
	
	private final TeamMembershipManager teamMembershipManager;
	
	@Inject
	public DefaultOrganizationMembershipManager(Dao dao, AccountManager accountManager,
			OrganizationMembershipManager organizationMembershipManager, 
			TeamMembershipManager teamMembershipManager) {
		super(dao);
		this.accountManager = accountManager;
		this.organizationMembershipManager = organizationMembershipManager;
		this.teamMembershipManager = teamMembershipManager;
	}

	@Transactional
	@Override
	public void save(OrganizationMembership membership) {
		if (membership.getOrganization().isNew())
			accountManager.save(membership.getOrganization(), null);
		dao.persist(membership);
	}

	@Sessional
	@Override
	public OrganizationMembership find(Account organization, Account user) {
		EntityCriteria<OrganizationMembership> criteria = newCriteria();
		criteria.add(Restrictions.eq("organization", organization)).add(Restrictions.eq("user", user));
		return find(criteria);
	}

	@Transactional
	@Override
	public void save(Collection<OrganizationMembership> organizationMemberships,
			Collection<TeamMembership> teamMemberships) {
		for (OrganizationMembership organizationMembership: organizationMemberships)
			organizationMembershipManager.save(organizationMembership);
		for (TeamMembership teamMembership: teamMemberships)
			teamMembershipManager.save(teamMembership);
	}	

	@Transactional
	@Override
	public void delete(Collection<OrganizationMembership> organizationMemberships) {
		for (OrganizationMembership organizationMembership: organizationMemberships) {
			delete(organizationMembership);
		}
	}

	@Transactional
	@Override
	public void save(OrganizationMembership organizationMembership, Collection<TeamMembership> teamMembershipsToAdd,
			Collection<TeamMembership> teamMembershipsToRemove) {
		dao.persist(organizationMembership);
		for (TeamMembership teamMembership: teamMembershipsToAdd)
			teamMembershipManager.save(teamMembership);
		for (TeamMembership teamMembership: teamMembershipsToRemove)
			teamMembershipManager.delete(teamMembership);
	}

	@Transactional
	@Override
	public void delete(OrganizationMembership organizationMembership) {
		Account organization = organizationMembership.getOrganization();
		Account user = organizationMembership.getUser();
		for (TeamMembership teamMembership: teamMembershipManager.findAll(organization, user)) {
			teamMembershipManager.delete(teamMembership);
		}
		dao.remove(organizationMembership);
	}

}
