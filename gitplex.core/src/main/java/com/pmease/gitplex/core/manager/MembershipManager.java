package com.pmease.gitplex.core.manager;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Membership;

public interface MembershipManager extends EntityDao<Membership> {
	
	void save(Membership membership);
	
	void delete(Set<Membership> memberships);
	
	void save(Collection<Membership> memberships);
	
	@Nullable
	Membership find(Account organization, Account user);
	
}
