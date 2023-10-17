package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.entitymanager.AgentAttributeManager;
import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentAttribute;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class DefaultAgentAttributeManager extends BaseEntityManager<AgentAttribute> implements AgentAttributeManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultAgentAttributeManager.class);
	
	private final TransactionManager transactionManager;
	
	private final ClusterManager clusterManager;
	
	private volatile Map<String, String> attributeNames;
	
	@Inject
	public DefaultAgentAttributeManager(Dao dao, TransactionManager transactionManager, ClusterManager clusterManager) {
		super(dao);
		this.transactionManager = transactionManager;
		this.clusterManager = clusterManager;
	}

	@Sessional
	@Listen
	public void on(SystemStarting event) {
		logger.info("Caching agent attribute info...");

		var hazelcastInstance = clusterManager.getHazelcastInstance();
		attributeNames = hazelcastInstance.getMap("agentAttributeNames");
		var cacheInited = hazelcastInstance.getCPSubsystem().getAtomicLong("agentAttributeCacheInited");
		clusterManager.init(cacheInited, () -> {
			Query<?> query = dao.getSession().createQuery("select name from AgentAttribute");
			for (Object name: query.list())
				attributeNames.put((String) name, (String) name);
			return 1L;
		});		
	}

	@Transactional
	@Override
	public void create(AgentAttribute attribute) {
		Preconditions.checkState(attribute.isNew());
		dao.persist(attribute);
		transactionManager.runAfterCommit(() -> attributeNames.put(attribute.getName(), attribute.getName()));
	}

	@Override
	public Collection<String> getAttributeNames() {
		return attributeNames.keySet();
	}

	@Transactional
	@Override
	public void syncAttributes(Agent agent, Map<String, String> attributeMap) {
		for (Iterator<AgentAttribute> it = agent.getAttributes().iterator(); it.hasNext();) {
			AgentAttribute attribute = it.next();
			String newValue = attributeMap.get(attribute.getName());
			if (newValue == null) {
				delete(attribute);
				it.remove();
			} else { 
				attribute.setValue(newValue);
			}
		}
		
		Map<String, String> currentAttributeMap = agent.getAttributeMap();
		
		for (Map.Entry<String, String> entry: attributeMap.entrySet()) {
			if (!currentAttributeMap.containsKey(entry.getKey())) {
				AgentAttribute attribute = new AgentAttribute();
				attribute.setAgent(agent);
				attribute.setName(entry.getKey());
				attribute.setValue(entry.getValue());
				create(attribute);
				agent.getAttributes().add(attribute);
			}
		}
	}
	
}
