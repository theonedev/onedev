package io.onedev.server.persistence;

import com.hazelcast.cp.IAtomicLong;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.model.AbstractEntity;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static io.onedev.server.persistence.PersistenceUtils.callWithTransaction;

@Singleton
public class DefaultIdManager implements IdManager {

	private final PersistenceManager persistenceManager;
	
	private final ClusterManager clusterManager;
	
	private final SessionFactoryManager sessionFactoryManager;
	
	private final Map<Class<?>, IAtomicLong> nextIds = new HashMap<>();
	
	@Inject
	public DefaultIdManager(PersistenceManager persistenceManager, ClusterManager clusterManager,
                            SessionFactoryManager sessionFactoryManager) {
		this.persistenceManager = persistenceManager;
		this.sessionFactoryManager = sessionFactoryManager;
		this.clusterManager = clusterManager;
	}

	@SuppressWarnings("unchecked")
	private long getMaxId(Connection conn, Class<?> entityClass) {
		try (Statement stmt = conn.createStatement()) {
			String query = String.format("select max(%s) from %s", 
					persistenceManager.getColumnName(AbstractEntity.PROP_ID), 
					persistenceManager.getTableName((Class<? extends AbstractEntity>) entityClass));
			try (ResultSet resultset = stmt.executeQuery(query)) {
				if (resultset.next()) 
					return Math.max(resultset.getLong(1), 0);
				else 
					return 0;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void init() {
		try (var conn = persistenceManager.openConnection()) {
			callWithTransaction(conn, () -> {
				for (var persistenceClass: sessionFactoryManager.getMetadata().getEntityBindings()) {
					Class<?> entityClass = persistenceClass.getMappedClass();
					var nextId = clusterManager.getHazelcastInstance().getCPSubsystem().getAtomicLong(entityClass.getName());
					clusterManager.init(nextId, () -> getMaxId(conn, entityClass) + 1);
					nextIds.put(entityClass, nextId);
				}
				return null;
			});
		} catch (SQLException e) {
			throw new RuntimeException(e);
		};
	}

	@Override
	public long nextId(Class<?> entityClass) {
		return nextIds.get(entityClass).getAndIncrement();
	}

	@Override
	public void useId(Class<?> entityClass, long id) {
		var nextAtomicId = nextIds.get(entityClass);
		while (true) {
			long nextId = nextAtomicId.getAndIncrement();
			if (id+1 > nextId) {
				if (nextAtomicId.compareAndSet(nextId, id+1))
					break;
			} else {
				break;
			}
		}
	}

}
