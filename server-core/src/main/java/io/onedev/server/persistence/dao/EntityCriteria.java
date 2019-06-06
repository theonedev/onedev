package io.onedev.server.persistence.dao;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.ResultTransformer;

import io.onedev.server.model.AbstractEntity;

@SuppressWarnings("serial")
public class EntityCriteria<T extends AbstractEntity> implements CriteriaSpecification, Serializable {
	
	private final CriteriaImpl impl;
	
	private final Criteria criteria;
	
	protected EntityCriteria(String entityName) {
		impl = new CriteriaImpl(entityName, null);
		criteria = impl;
	}
	
	protected EntityCriteria(String entityName, String alias) {
		impl = new CriteriaImpl(entityName, alias, null);
		criteria = impl;
	}
	
	protected EntityCriteria(CriteriaImpl impl, Criteria criteria) {
		this.impl = impl;
		this.criteria = criteria;
	}
	
	/**
	 * Get an executable instance of <literal>Criteria</literal>,
	 * to actually run the query.
	 */
	public Criteria getExecutableCriteria(Session session) {
		CriteriaImpl clone = (CriteriaImpl) SerializationUtils.clone(impl);
		clone.setSession( ( SessionImplementor ) session );
		return clone;
	}
	
	public static <T extends AbstractEntity> EntityCriteria<T> of(Class<T> clazz) {
		return new EntityCriteria<T>(clazz.getName());
	}
	
	public static <T extends AbstractEntity> EntityCriteria<T> of(Class<T> clazz, String alias) {
		return new EntityCriteria<T>(clazz.getName(), alias );
	}
	
	public EntityCriteria<T> add(Criterion criterion) {
		criteria.add(criterion);
		return this;
	}

	public EntityCriteria<T> addOrder(Order order) {
		criteria.addOrder(order);
		return this;
	}
	
	public void setCacheable(boolean cacheable) {
		criteria.setCacheable(cacheable);
	}
	
	public void setCacheMode(CacheMode cacheMode) {
		criteria.setCacheMode(cacheMode);
	}
	
	public void setCacheRegion(String cacheRegion) {
		criteria.setCacheRegion(cacheRegion);
	}

	public EntityCriteria<T> createAlias(String associationPath, String alias)
	throws HibernateException {
		criteria.createAlias(associationPath, alias);
		return this;
	}

	public Criteria createCriteria(String associationPath, String alias)
	throws HibernateException {
		return criteria.createCriteria(associationPath, alias);
	}

	public Criteria createCriteria(String associationPath)
	throws HibernateException {
		return criteria.createCriteria(associationPath);
	}

	public String getAlias() {
		return criteria.getAlias();
	}

	public EntityCriteria<T> setFetchMode(String associationPath, FetchMode mode)
	throws HibernateException {
		criteria.setFetchMode(associationPath, mode);
		return this;
	}

	public EntityCriteria<T> setProjection(Projection projection) {
		criteria.setProjection(projection);
		return this;
	}

	public EntityCriteria<T> setResultTransformer(ResultTransformer resultTransformer) {
		criteria.setResultTransformer(resultTransformer);
		return this;
	}
	
	public String toString() {
		return "DetachableCriteria(" + criteria.toString() + ')';
	}
	
	CriteriaImpl getCriteriaImpl() {
		return impl;
	}

    public EntityCriteria<T> createAlias(String associationPath, String alias, JoinType joinType) throws HibernateException {
        criteria.createAlias(associationPath, alias, joinType);
        return this;
    }
	
	public EntityCriteria<T> createAlias(String associationPath, String alias, JoinType joinType, Criterion withClause) throws HibernateException {
		criteria.createAlias(associationPath, alias, joinType, withClause);
		return this;
	}
	
	public EntityCriteria<T> createCriteria(String associationPath, JoinType joinType) throws HibernateException {
        return new EntityCriteria<T>(impl, criteria.createCriteria(associationPath, joinType));
    }

    public EntityCriteria<T> createCriteria(String associationPath, String alias, JoinType joinType) throws HibernateException {
        return new EntityCriteria<T>(impl, criteria.createCriteria(associationPath, alias, joinType));
    }
	
	public EntityCriteria<T> createCriteria(String associationPath, String alias, JoinType joinType, Criterion withClause) throws HibernateException {
		return new EntityCriteria<T>(impl, criteria.createCriteria(associationPath, alias, joinType, withClause));
	}

	public EntityCriteria<T> setComment(String comment) {
        criteria.setComment(comment);
        return this;
    }

    public EntityCriteria<T> setLockMode(LockMode lockMode) {
        criteria.setLockMode(lockMode);
        return this;
    }

    public EntityCriteria<T> setLockMode(String alias, LockMode lockMode) {
        criteria.setLockMode(alias, lockMode);
        return this;
    }
}
