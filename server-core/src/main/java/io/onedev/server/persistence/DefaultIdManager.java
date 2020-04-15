package io.onedev.server.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultIdManager implements IdManager {

	private final Dao dao;
	
	private final PersistManager persistManager;
	
	private final Map<Class<?>, AtomicLong> nextIds = new HashMap<>();
	
	@Inject
	public DefaultIdManager(Dao dao, PersistManager persistManager) {
		this.dao = dao;
		this.persistManager = persistManager;
	}

	private long getMaxId(Class<?> entityClass) {
		CriteriaBuilder builder = persistManager.getSessionFactory().getCriteriaBuilder();
		CriteriaQuery<Number> query = builder.createQuery(Number.class);
		Root<?> root = query.from(entityClass);
		query.select(builder.max(root.get("id")));
		Number result = dao.getSession().createQuery(query).getSingleResult();
		return result!=null?result.longValue():0;
	}
	
	@Sessional
	@Override
	public void init() {
		for (EntityType<?> entityType: ((EntityManagerFactory)persistManager.getSessionFactory()).getMetamodel().getEntities()) {
			Class<?> entityClass = entityType.getJavaType();
			nextIds.put(entityClass, new AtomicLong(getMaxId(entityClass)+1));
		}
	}

	@Override
	public long nextId(Class<?> entityClass) {
		return nextIds.get(entityClass).getAndIncrement();
	}

	@Override
	public void useId(Class<?> entityClass, long id) {
		AtomicLong nextIdAtom = nextIds.get(entityClass);
		while (true) {
			long nextId = nextIdAtom.get();
			if (id+1 > nextId) {
				if (nextIdAtom.compareAndSet(nextId, id+1))
					break;
			} else {
				break;
			}
		}
	}

}
