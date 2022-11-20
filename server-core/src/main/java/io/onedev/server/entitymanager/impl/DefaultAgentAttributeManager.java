package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.AgentAttributeManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentAttribute;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

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
	public void on(SystemStarted event) {
		logger.info("Caching agent attribute info...");

		attributeNames = clusterManager.getHazelcastInstance().getReplicatedMap("agentAttributeNames");
		
		if (clusterManager.isLeaderServer()) {
			Query<?> query = dao.getSession().createQuery("select name from AgentAttribute");
			for (Object name: query.list()) 
				attributeNames.put((String) name, (String) name);
		}
	}

	@Transactional
	@Override
	public void save(AgentAttribute attribute) {
		super.save(attribute);
		
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				attributeNames.put(attribute.getName(), attribute.getName());
			}
			
		});
	}

	@Override
	public List<String> getAttributeNames() {
		List<String> copy = new ArrayList<>(attributeNames.keySet());
		Collections.sort(copy);
		return copy;
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
				save(attribute);
				agent.getAttributes().add(attribute);
			}
		}
	}
	
}
