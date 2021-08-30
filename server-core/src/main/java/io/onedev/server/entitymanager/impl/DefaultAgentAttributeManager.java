package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.loader.Listen;
import io.onedev.server.entitymanager.AgentAttributeManager;
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
	
	private final Collection<String> attributeNames = new HashSet<>();
	
	private final ReadWriteLock attributeNamesLock = new ReentrantReadWriteLock();
	
	@Inject
	public DefaultAgentAttributeManager(Dao dao, TransactionManager transactionManager) {
		super(dao);
		this.transactionManager = transactionManager;
	}

	@Sessional
	@Listen
	public void on(SystemStarted event) {
		logger.info("Caching agent attribute info...");

		Query<?> query = dao.getSession().createQuery("select name from AgentAttribute");
		for (Object name: query.list()) 
			attributeNames.add((String) name);
	}

	@Transactional
	@Override
	public void save(AgentAttribute attribute) {
		super.save(attribute);
		
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				attributeNamesLock.writeLock().lock();
				try {
					attributeNames.add(attribute.getName());
				} finally {
					attributeNamesLock.writeLock().unlock();
				}
			}
			
		});
	}

	@Override
	public List<String> getAttributeNames() {
		attributeNamesLock.readLock().lock();
		try {
			List<String> copy = new ArrayList<>(attributeNames);
			Collections.sort(copy);
			return copy;
		} finally {
			attributeNamesLock.readLock().unlock();
		}
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
