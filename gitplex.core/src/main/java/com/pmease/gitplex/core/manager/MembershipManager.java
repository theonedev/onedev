package com.pmease.gitplex.core.manager;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.Membership;

public interface MembershipManager extends EntityDao<Membership> {
	
	void save(Membership membership);
	
}
