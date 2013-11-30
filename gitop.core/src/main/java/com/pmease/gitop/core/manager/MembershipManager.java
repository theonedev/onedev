package com.pmease.gitop.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultMembershipManager;
import com.pmease.gitop.model.Membership;

@ImplementedBy(DefaultMembershipManager.class)
public interface MembershipManager extends GenericDao<Membership> {
	
}
