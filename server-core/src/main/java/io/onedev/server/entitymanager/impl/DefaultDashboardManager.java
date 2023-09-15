package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.entitymanager.DashboardGroupShareManager;
import io.onedev.server.entitymanager.DashboardManager;
import io.onedev.server.entitymanager.DashboardUserShareManager;
import io.onedev.server.model.*;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
public class DefaultDashboardManager extends BaseEntityManager<Dashboard> implements DashboardManager {

	private final SubscriptionManager subscriptionManager;
	
	private final DashboardGroupShareManager groupShareManager;
	
	private final DashboardUserShareManager userShareManager;
	
	@Inject
	public DefaultDashboardManager(Dao dao, DashboardGroupShareManager groupShareManager, 
			DashboardUserShareManager userShareManager, SubscriptionManager subscriptionManager) {
		super(dao);
		this.groupShareManager = groupShareManager;
		this.userShareManager = userShareManager;
		this.subscriptionManager = subscriptionManager;
	}

	@Override
	public List<Dashboard> query() {
		return query(true);
	}
	
	@Override
	public int count() {
		return count(true);
	}

	@Sessional
	@Override
	public List<Dashboard> queryAccessible(User user) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Dashboard> criteriaQuery = builder.createQuery(Dashboard.class);
		Root<Dashboard> root = criteriaQuery.from(Dashboard.class);
		
		if (user != null) {
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(builder.equal(root.get(Dashboard.PROP_OWNER), user));
			
			Subquery<DashboardUserShare> userShareQuery = criteriaQuery.subquery(DashboardUserShare.class);
			Root<DashboardUserShare> userShareRoot = userShareQuery.from(DashboardUserShare.class);
			userShareQuery.select(userShareRoot);

			predicates.add(builder.exists(userShareQuery.where(
					builder.equal(userShareRoot.get(DashboardUserShare.PROP_DASHBOARD), root),
					builder.equal(userShareRoot.get(DashboardUserShare.PROP_USER), user))));
			
			for (Group group: user.getGroups()) {
				Subquery<DashboardGroupShare> groupShareQuery = criteriaQuery.subquery(DashboardGroupShare.class);
				Root<DashboardGroupShare> groupShareRoot = groupShareQuery.from(DashboardGroupShare.class);
				groupShareQuery.select(groupShareRoot);

				predicates.add(builder.exists(groupShareQuery.where(
						builder.equal(groupShareRoot.get(DashboardGroupShare.PROP_DASHBOARD), root),
						builder.equal(groupShareRoot.get(DashboardGroupShare.PROP_GROUP), group))));
			}
			predicates.add(builder.equal(root.get(Dashboard.PROP_FOR_EVERYONE), true));
			criteriaQuery.where(builder.or(predicates.toArray(new Predicate[0])));
		} else {
			criteriaQuery.where(builder.equal(root.get(Dashboard.PROP_FOR_EVERYONE), true));
		}
		
		Query<Dashboard> query = getSession().createQuery(criteriaQuery);
		return query.getResultList();
	}

	@Override
	public void create(Dashboard dashboard) {
		Preconditions.checkState(dashboard.isNew() && subscriptionManager.isSubscriptionActive());
		dao.persist(dashboard);
	}

	@Override
	public void update(Dashboard dashboard) {
		Preconditions.checkState(!dashboard.isNew() && subscriptionManager.isSubscriptionActive());
	}

	@Override
	public Dashboard find(User owner, String name) {
		EntityCriteria<Dashboard> criteria = EntityCriteria.of(Dashboard.class);
		criteria.add(Restrictions.eq(Dashboard.PROP_OWNER, owner));
		criteria.add(Restrictions.eq(Dashboard.PROP_NAME, name));
		return find(criteria);
	}

	@Transactional
	@Override
	public void syncShares(Dashboard dashboard, boolean forEveryone, 
			Collection<String> groupNames, Collection<String> userNames) {
		dashboard.setForEveryone(forEveryone);
		groupShareManager.syncShares(dashboard, groupNames);
		userShareManager.syncShares(dashboard, userNames);
	}
	
}
