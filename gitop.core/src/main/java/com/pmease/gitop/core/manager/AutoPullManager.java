package com.pmease.gitop.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultAutoPullManager;
import com.pmease.gitop.model.AutoPull;

@ImplementedBy(DefaultAutoPullManager.class)
public interface AutoPullManager extends GenericDao<AutoPull> {
	
}
