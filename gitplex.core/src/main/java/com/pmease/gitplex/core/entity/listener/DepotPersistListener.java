package com.pmease.gitplex.core.entity.listener;

import java.io.Serializable;

import javax.inject.Singleton;

import org.hibernate.CallbackException;
import org.hibernate.type.Type;

import com.pmease.commons.hibernate.PersistListener;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.User;
import com.pmease.gitplex.core.entity.component.IntegrationPolicy;
import com.pmease.gitplex.core.manager.DepotManager;

@Singleton
public class DepotPersistListener implements PersistListener {

	private final DepotManager depotManager;
	
	private final UnitOfWork unitOfWork;
	
	public DepotPersistListener(DepotManager depotManager, UnitOfWork unitOfWork) {
		this.depotManager = depotManager;
		this.unitOfWork = unitOfWork;
	}
	
	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
		return false;
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
			String[] propertyNames, Type[] types) throws CallbackException {
		int nameIndex = getPropertyIndex(propertyNames, "name");
		final String newName = (String) currentState[nameIndex];
		final String oldName = (String) previousState[nameIndex];
		if (!newName.equals(oldName)) {
			final Long depotId = (Long) id;
			depotManager.afterCommit(new Runnable() {

				@Override
				public void run() {
					unitOfWork.asyncCall(new Runnable() {

						@Override
						public void run() {
							User depotOwner = depotManager.load(Depot.class, depotId).getOwner();
							for (Depot depot: depotManager.allOf(Depot.class)) {
								boolean updated = false;
								for (IntegrationPolicy integrationPolicy: depot.getIntegrationPolicies()) {
									if (integrationPolicy.onDepotRename(depotOwner, oldName, newName))
										updated = true;
								}
								if (updated) {
									depotManager.save(depot);
								}
							}
						}
						
					});
				}
				
			});
		}
		return false;
	}

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
		return false;
	}

	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
	}

	private int getPropertyIndex(String[] propertyNames, String propertyName) {
		for (int i=0; i<propertyNames.length; i++) {
			if (propertyNames[i].equals(propertyName))
				return i;
		}
		throw new RuntimeException("Unable to find index of property '" + propertyName + "'");
	}
}
