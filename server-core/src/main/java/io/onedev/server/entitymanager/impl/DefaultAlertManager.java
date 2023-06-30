package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.AlertManager;
import io.onedev.server.model.Alert;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import org.apache.wicket.request.cycle.RequestCycle;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Date;

import static io.onedev.server.model.Alert.PROP_MESSAGE;

@Singleton
public class DefaultAlertManager extends BaseEntityManager<Alert> implements AlertManager {
	
	@Inject
	public DefaultAlertManager(Dao dao) {
		super(dao);
	}
	
	@Transactional
	@Override
	public void alert(String message) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_MESSAGE, message));
		var alert = find(criteria);
		if (alert == null) {
			alert = new Alert();
			alert.setMessage(message);
		}
		alert.setDate(new Date());
		dao.persist(alert);
	}

	@Override
	public int count() {
		return count(true);
	}
}
