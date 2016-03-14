package com.pmease.gitplex.core.manager;

import java.util.Collection;
import java.util.Set;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.Membership;

public interface MembershipManager extends EntityDao<Membership> {
	
	void save(Membership membership);
	
	void delete(Set<Membership> memberships);
	
	void save(Collection<Membership> memberships);
}
