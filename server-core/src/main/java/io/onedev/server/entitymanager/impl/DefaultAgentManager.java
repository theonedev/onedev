package io.onedev.server.entitymanager.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import com.google.common.base.Splitter;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.agent.AgentData;
import io.onedev.agent.Message;
import io.onedev.agent.MessageType;
import io.onedev.agent.WebsocketUtils;
import io.onedev.agent.job.LogRequest;
import io.onedev.commons.loader.Listen;
import io.onedev.commons.loader.ListenerRegistry;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.entitymanager.AgentAttributeManager;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.entitymanager.AgentTokenManager;
import io.onedev.server.event.agent.AgentConnected;
import io.onedev.server.event.agent.AgentDisconnected;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentAttribute;
import io.onedev.server.model.AgentToken;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.util.validation.AttributeNameValidator;

@Singleton
public class DefaultAgentManager extends BaseEntityManager<Agent> implements AgentManager {

	private final AgentAttributeManager attributeManager;
	
	private final AgentTokenManager tokenManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final String agentVersion;
	
	private final Collection<String> agentLibs;
	
	private final Map<Long, Session> agentSessions = new ConcurrentHashMap<>();
	
	private final Set<String> osArchs = new HashSet<>();
	
	@Inject
	public DefaultAgentManager(Dao dao, AgentAttributeManager attributeManager, AgentTokenManager tokenManager, 
			ListenerRegistry listenerRegistry) {
		super(dao);
		
		this.attributeManager = attributeManager;
		this.tokenManager = tokenManager;
		this.listenerRegistry = listenerRegistry;
	
		Properties props = new Properties();
		agentLibs = new HashSet<>();
		try (InputStream is = Agent.class.getClassLoader().getResourceAsStream("META-INF/onedev-agent.properties")) {
			props = new Properties();
			props.load(is);
			for (String dependency: Splitter.on(';').omitEmptyStrings().split(props.getProperty("dependencies")))  
				agentLibs.add(StringUtils.replace(dependency, ":", "-") + ".jar");
			agentVersion = props.getProperty("version");
			agentLibs.add(props.getProperty("id") + "-" + agentVersion + ".jar");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Sessional
	@Listen
	public void on(SystemStarted event) {
		Query<String> query = dao.getSession().createQuery(String.format("select %s from Agent", Agent.PROP_OS_ARCH));
		for (String osArch: query.list()) 
			osArchs.add(osArch);
	}
	
	@Override
	public String getAgentVersion() {
		return agentVersion;
	}

	@Override
	public Collection<String> getAgentLibs() {
		return agentLibs;
	}

	@Transactional
	@Override
	public Long agentConnected(AgentData data, Session session) {
		for (String attributeName: data.getAttributes().keySet()) {
			if (!AttributeNameValidator.PATTERN.matcher(attributeName).matches()) {
				throw new ExplicitException("Attribute '" + attributeName + "' should start and end with "
						+ "alphanumeric or underscore. Only alphanumeric, underscore, dash, space and "
						+ "dot are allowed in the middle.");
			} else if (Agent.ALL_FIELDS.contains(attributeName)) { 
				throw new ExplicitException("Attribute '" + attributeName + "' is reserved");
			}
		}
		
		AgentToken token = tokenManager.find(data.getToken());
		if (token == null) { 
			throw new ExplicitException("Invalid agent token");
		} else {
			Agent agent = token.getAgent();
			if (agent == null) {
				if (findByName(data.getName()) != null)
					throw new ExplicitException("Name '" + data.getName() + "' already used by another agent");
				agent = new Agent();
				agent.setToken(token);
				agent.setOs(data.getOs());
				agent.setOsVersion(data.getOsVersion());
				agent.setOsArch(data.getOsArch());
				agent.setName(data.getName());
				agent.setCpu(data.getCpu());
				agent.setMemory(data.getMemory());
				agent.setIpAddress(session.getRemoteAddress().getAddress().getHostAddress());
				save(agent);
				
				for (Map.Entry<String, String> entry: data.getAttributes().entrySet()) {
					AgentAttribute attribute = new AgentAttribute();
					attribute.setAgent(agent);
					attribute.setName(entry.getKey());
					attribute.setValue(entry.getValue());
					attributeManager.save(attribute);
					agent.getAttributes().add(attribute);
				}
			} else if (!agentSessions.containsKey(agent.getId())) {
				Agent agentWithSameName = findByName(data.getName());
				if (agentWithSameName != null && !agentWithSameName.equals(agent))
					throw new ExplicitException("Name '" + data.getName() + "' already used by another agent");
				agent.setName(data.getName());
				agent.setOs(data.getOs());
				agent.setOsVersion(data.getOsVersion());
				agent.setOsArch(data.getOsArch());
				agent.setIpAddress(session.getRemoteAddress().getAddress().getHostAddress());
				agent.setCpu(data.getCpu());
				agent.setMemory(data.getMemory());
				save(agent);
				attributeManager.syncAttributes(agent, data.getAttributes());
			} else {
				throw new ExplicitException("Token already used by another agent");
			}

			Session prevSession = agentSessions.put(agent.getId(), session);
			if (prevSession != null) {
				try {
					prevSession.disconnect();
				} catch (IOException e) {
				}
			}
			
			listenerRegistry.post(new AgentConnected(agent));
			
			return agent.getId();
		}
	}

	@Transactional
	@Override
	public void agentDisconnected(Long agentId) {
		agentSessions.remove(agentId);
		listenerRegistry.post(new AgentDisconnected(load(agentId)));
	}

	@Override
	public void save(Agent agent) {
		super.save(agent);
		osArchs.add(agent.getOsArch());
	}

	@Transactional
	@Override
	public void delete(Agent agent) {
    	Query<?> query = getSession().createQuery("update Build set agent=null where agent=:agent");
    	query.setParameter("agent", agent);
    	query.executeUpdate();
		
		super.delete(agent);
		dao.remove(agent.getToken());

		Session prevSession = agentSessions.remove(agent.getId());
		if (prevSession != null) {
			try {
				prevSession.disconnect();
			} catch (IOException e) {
			}
		}
	}

	@Sessional
	@Override
	public Agent findByName(String name) {
		EntityCriteria<Agent> criteria = newCriteria();
		criteria.add(Restrictions.eq(Agent.PROP_NAME, name));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Override
	public Collection<Long> getOnlineAgentIds() {
		return agentSessions.keySet();
	}

	@Sessional
	@Override
	public List<String> getOsArchs() {
		List<String> osArchs = new ArrayList<>(this.osArchs);
		Collections.sort(osArchs);
		return osArchs;
	}
	
	private CriteriaQuery<Agent> buildCriteriaQuery(org.hibernate.Session session, EntityQuery<Agent> agentQuery) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Agent> query = builder.createQuery(Agent.class);
		Root<Agent> root = query.from(Agent.class);
		query.select(root);

		if (agentQuery.getCriteria() != null)
			query.where(agentQuery.getCriteria().getPredicate(root, builder));

		List<javax.persistence.criteria.Order> orders = new ArrayList<>();
		for (EntitySort sort: agentQuery.getSorts()) {
			if (sort.getDirection() == Direction.ASCENDING)
				orders.add(builder.asc(AgentQuery.getPath(root, Agent.ORDER_FIELDS.get(sort.getField()))));
			else
				orders.add(builder.desc(AgentQuery.getPath(root, Agent.ORDER_FIELDS.get(sort.getField()))));
		}

		if (orders.isEmpty())
			orders.add(builder.asc(AgentQuery.getPath(root, Agent.PROP_NAME)));
		query.orderBy(orders);
		
		return query;
	}
	
	@Sessional
	@Override
	public List<Agent> query(EntityQuery<Agent> agentQuery, int firstResult, int maxResults) {
		CriteriaQuery<Agent> criteriaQuery = buildCriteriaQuery(getSession(), agentQuery);
		Query<Agent> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		return query.getResultList();
	}

	@Sessional
	@Override
	public int count(io.onedev.server.search.entity.EntityCriteria<Agent> agentCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Agent> root = criteriaQuery.from(Agent.class);
		
		if (agentCriteria != null)
			criteriaQuery.where(agentCriteria.getPredicate(root, builder));

		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Sessional
	@Override
	public void restart(Agent agent) {
		Session session = agentSessions.get(agent.getId());
		if (session != null)
			new Message(MessageType.RESTART, new byte[0]).sendBy(session);
	}

	@Transactional
	@Override
	public void delete(Collection<Agent> agents) {
		for (Agent agent: agents)
			delete(agent);
	}

	@Transactional
	@Override
	public void restart(Collection<Agent> agents) {
		for (Agent agent: agents) 
			restart(agent);
	}

	@Transactional
	@Override
	public void pause(Collection<Agent> agents) {
		for (Agent agent: agents) { 
			agent.setPaused(true);
			save(agent);
		}
	}

	@Transactional
	@Override
	public void resume(Collection<Agent> agents) {
		for (Agent agent: agents) { 
			agent.setPaused(false);
			save(agent);
		}
	}

	@Override
	public void attributesUpdated(Agent agent) {
		Session session = agentSessions.get(agent.getId());
		if (session != null) {
			byte[] attributeBytes = SerializationUtils.serialize((Serializable) agent.getAttributeMap());
			new Message(MessageType.UPDATE_ATTRIBUTES, attributeBytes).sendBy(session);
		}
	}

	@Override
	public List<String> getAgentLog(Agent agent) {
		Session session = agentSessions.get(agent.getId());
		if (session != null) {
			try {
				return WebsocketUtils.call(session, new LogRequest(), 60000);
			} catch (InterruptedException | TimeoutException e) {
				throw new RuntimeException(e);
			}
		} else { 
			return new ArrayList<>();
		}
	}

	@Override
	public Session getAgentSession(Long agentId) {
		return agentSessions.get(agentId);
	}

}
