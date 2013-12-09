package com.pmease.gitop.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultAutoPushManager;
import com.pmease.gitop.model.AutoPush;

@ImplementedBy(DefaultAutoPushManager.class)
public interface AutoPushManager extends GenericDao<AutoPush> {
	
}
