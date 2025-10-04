package io.onedev.server.service.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.server.cluster.ClusterService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentAttribute;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.AgentAttributeService;

@Singleton
public class DefaultAgentAttributeService extends BaseEntityService<AgentAttribute> implements AgentAttributeService {

	private static final Logger logger = LoggerFactory.getLogger(DefaultAgentAttributeService.class);

	@Inject
	private TransactionService transactionService;

	@Inject
	private ClusterService clusterService;
	
	private volatile Map<String, String> attributeNames;

	@Sessional
	@Listen
	public void on(SystemStarting event) {
		logger.info("Caching agent attribute info...");

		var hazelcastInstance = clusterService.getHazelcastInstance();
		attributeNames = hazelcastInstance.getMap("agentAttributeNames");
		var cacheInited = hazelcastInstance.getCPSubsystem().getAtomicLong("agentAttributeCacheInited");
		clusterService.initWithLead(cacheInited, () -> {
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
		transactionService.runAfterCommit(() -> attributeNames.put(attribute.getName(), attribute.getName()));
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
