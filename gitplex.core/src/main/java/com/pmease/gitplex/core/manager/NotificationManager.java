package com.pmease.gitplex.core.manager;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.Notification;
import com.pmease.gitplex.core.listener.PullRequestListener;

public interface NotificationManager extends EntityDao<Notification>, PullRequestListener {

}
