package io.onedev.server.persistence;

import com.hazelcast.map.IMap;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.Dao;
import org.hibernate.query.Query;

import java.util.Map;

public class SequenceGenerator {

	private final Class<? extends AbstractEntity> sequenceClass;
	
	private final ClusterService clusterService;
	
	private final Dao dao;
	
	private IMap<Long, Long> nextSequences;
	
	public SequenceGenerator(Class<? extends AbstractEntity> sequenceClass, ClusterService clusterService, Dao dao) {
		this.sequenceClass = sequenceClass;
		this.dao = dao;
		this.clusterService = clusterService;
	}

	private synchronized Map<Long, Long> getNextSequences() {
		if (nextSequences == null) 
			nextSequences = clusterService.getHazelcastInstance().getMap("nextSequences:" + sequenceClass.getName());
		return nextSequences;
	}
	
	public void removeNextSequence(Project sequenceScope) {
		getNextSequences().remove(sequenceScope.getId());
	}

	public Long getNextSequence(Project sequenceScope) {
		Long key = sequenceScope.getId();
		while (true) {
			Long nextSequence = getNextSequences().get(key);
			if (nextSequence == null) {
				Query<?> query = dao.getSession().createQuery(String.format("select max(%s) from %s where %s=:numberScope", 
						AbstractEntity.PROP_NUMBER, sequenceClass.getSimpleName(), AbstractEntity.PROP_NUMBER_SCOPE));
				query.setParameter(AbstractEntity.PROP_NUMBER_SCOPE, sequenceScope);
				
				Object result = query.uniqueResult();
				if (result != null) 
					nextSequence = (Long)result + 1;
				else 
					nextSequence = 1L;
				if (getNextSequences().putIfAbsent(key, nextSequence) == null)
					return nextSequence;
			} else if (getNextSequences().replace(key, nextSequence, nextSequence + 1)) {
				return nextSequence + 1;
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}

}
