package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.hazelcast.core.HazelcastInstance;
import io.onedev.agent.AgentData;
import io.onedev.agent.Message;
import io.onedev.agent.MessageTypes;
import io.onedev.agent.WebsocketUtils;
import io.onedev.agent.job.LogRequest;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.agent.AgentConnected;
import io.onedev.server.event.agent.AgentDisconnected;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.entitymanager.AgentAttributeManager;
import io.onedev.server.entitymanager.AgentLastUsedDateManager;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.entitymanager.AgentTokenManager;
import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentAttribute;
import io.onedev.server.model.AgentLastUsedDate;
import io.onedev.server.model.AgentToken;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.validation.validator.AttributeNameValidator;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@Singleton
public class DefaultAgentManager extends BaseEntityManager<Agent> implements AgentManager, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultAgentManager.class);
	
	private final AgentAttributeManager attributeManager;
	
	private final AgentTokenManager tokenManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final TransactionManager transactionManager;
	
	private final ClusterManager clusterManager;
	
	private final AgentLastUsedDateManager lastUsedDateManager;
	
	private final String agentVersion;
	
	private final Collection<String> agentLibs;
	
	private final Map<Long, Session> agentSessions = new ConcurrentHashMap<>();
	
	private volatile Map<Long, String> agentServers;
	
	private volatile Map<String, String> osNames;
	
	private volatile Map<String, String> osArchs;
	
	@Inject
	public DefaultAgentManager(Dao dao, AgentAttributeManager attributeManager, 
							   AgentTokenManager tokenManager, ListenerRegistry listenerRegistry, 
							   TransactionManager transactionManager, ClusterManager clusterManager, 
							   AgentLastUsedDateManager lastUsedDateManager) {
		super(dao);
		
		this.attributeManager = attributeManager;
		this.tokenManager = tokenManager;
		this.listenerRegistry = listenerRegistry;
		this.transactionManager = transactionManager;
		this.clusterManager = clusterManager;
		this.lastUsedDateManager = lastUsedDateManager;
	
		agentLibs = new HashSet<>();
		try (InputStream is = Agent.class.getClassLoader().getResourceAsStream("META-INF/onedev-agent.properties")) {
			Properties props = new Properties();
			props.load(is);
			for (String dependency: Splitter.on(';').omitEmptyStrings().split(props.getProperty("dependencies")))  
				agentLibs.add(StringUtils.replace(dependency, ":", "-") + ".jar");
			agentVersion = props.getProperty("version");
			agentLibs.add(props.getProperty("id") + "-" + agentVersion + ".jar");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(AgentManager.class);
	}
	
	@Sessional
	@Listen
	public void on(SystemStarting event) {
		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance(); 
		
		// Use replicated map for resource allocation performance
		agentServers = hazelcastInstance.getReplicatedMap("agentServers");
		osNames = hazelcastInstance.getMap("agentOsNames");
		osArchs = hazelcastInstance.getMap("agentOsArchs");
		
		var cacheInited = hazelcastInstance.getCPSubsystem().getAtomicLong("agentCacheInited");
		clusterManager.init(cacheInited, () -> {
			Query<Object[]> query = dao.getSession().createQuery(String.format("select %s, %s from Agent",
					Agent.PROP_OS_NAME, Agent.PROP_OS_ARCH));
			for (Object[] row: query.list()) {
				osNames.put((String) row[0], (String) row[0]);
				osArchs.put((String) row[1], (String) row[1]);
			}
			return 1L;
		});
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Agent) {
			var agent = (Agent) event.getEntity();
			String osName = agent.getOsName();
			String osArch = agent.getOsArch();
			transactionManager.runAfterCommit(() -> {
				osNames.put(osName, osName);
				osArchs.put(osArch, osArch);
			});
		}
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
		
		AgentToken token = Preconditions.checkNotNull(tokenManager.find(data.getToken()));
		Agent agent = findByToken(token);
		if (agent == null) {
			agent = new Agent();
			agent.setToken(token);
			agent.setOsName(data.getOsInfo().getOsName());
			agent.setOsVersion(data.getOsInfo().getOsVersion());
			agent.setOsArch(data.getOsInfo().getOsArch());
			agent.setName(data.getName());
			agent.setCpus(data.getCpus());
			agent.setIpAddress(data.getIpAddress());

			AgentLastUsedDate lastUsedDate = new AgentLastUsedDate();
			agent.setLastUsedDate(lastUsedDate);
			lastUsedDateManager.create(lastUsedDate);

			dao.persist(agent);
			
			for (Map.Entry<String, String> entry: data.getAttributes().entrySet()) {
				AgentAttribute attribute = new AgentAttribute();
				attribute.setAgent(agent);
				attribute.setName(entry.getKey());
				attribute.setValue(entry.getValue());
				attributeManager.create(attribute);
				agent.getAttributes().add(attribute);
			}
		} else if (agentSessions.containsKey(agent.getId())) {
			throw new ExplicitException("Token already used by another agent");
		} else {
			agent.setName(data.getName());
			agent.setOsName(data.getOsInfo().getOsName());
			agent.setOsVersion(data.getOsInfo().getOsVersion());
			agent.setOsArch(data.getOsInfo().getOsArch());
			agent.setIpAddress(data.getIpAddress());
			agent.setCpus(data.getCpus());
			dao.persist(agent);
			attributeManager.syncAttributes(agent, data.getAttributes());
		}

		agentServers.put(agent.getId(), clusterManager.getLocalServerAddress());
		Session prevSession = agentSessions.put(agent.getId(), session);
		if (prevSession != null) {
			try {
				prevSession.disconnect();
			} catch (IOException ignored) {
			}
		}
		
		listenerRegistry.post(new AgentConnected(agent));
		
		return agent.getId();
	}

	@Transactional
	@Override
	public void agentDisconnected(Long agentId) {
		agentServers.remove(agentId);
		agentSessions.remove(agentId);
		Agent agent = get(agentId);
		if (agent != null) 
			listenerRegistry.post(new AgentDisconnected(agent));
	}

	private void removeReferences(Agent agent) {
    	Query<?> query = getSession().createQuery("update Build set agent=null where agent=:agent");
    	query.setParameter("agent", agent);
    	query.executeUpdate();
	}
	
	@Sessional
	@Override
	public Agent findByName(String name) {
		EntityCriteria<Agent> criteria = newCriteria();
		criteria.add(Restrictions.eq(Agent.PROP_NAME, name));
		criteria.setCacheable(true);
		return find(criteria);
	}
	
	@Nullable
	@Sessional
	@Override
	public Agent findByToken(AgentToken token) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(Agent.PROP_TOKEN, token));
		return find(criteria);
	}

	@Override
	public String getAgentServer(Long agentId) {
		return agentServers.get(agentId);
	}
	
	@Override
	public Collection<Long> getOnlineAgents() {
		var onlineAgents = new HashSet<Long>();
		for (var entry: agentServers.entrySet()) {
			if (clusterManager.getServer(entry.getValue(), false) != null)
				onlineAgents.add(entry.getKey());
		}
		return onlineAgents;
	}

	@Sessional
	@Override
	public Collection<String> getOsNames() {
		return osNames.keySet();
	}
	
	@Sessional
	@Override
	public Collection<String> getOsArchs() {
		return osArchs.keySet();
	}
	
	private CriteriaQuery<Agent> buildCriteriaQuery(org.hibernate.Session session, EntityQuery<Agent> agentQuery) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Agent> query = builder.createQuery(Agent.class);
		Root<Agent> root = query.from(Agent.class);
		query.select(root);

		if (agentQuery.getCriteria() != null)
			query.where(agentQuery.getCriteria().getPredicate(query, root, builder));

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
		query.setCacheable(true);
		return query.getResultList();
	}

	@Sessional
	@Override
	public int count(Criteria<Agent> agentCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Agent> root = criteriaQuery.from(Agent.class);
		
		if (agentCriteria != null)
			criteriaQuery.where(agentCriteria.getPredicate(criteriaQuery, root, builder));

		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Sessional
	@Override
	public void restart(Agent agent) {
		Long agentId = agent.getId();
		String agentName = agent.getName();
		String server = agentServers.get(agentId);
		if (server != null) {
			clusterManager.submitToServer(server, () -> {
				try {
					Session session = agentSessions.get(agentId);
					if (session != null)
						new Message(MessageTypes.RESTART, new byte[0]).sendBy(session);
				} catch (Exception e) {
					logger.error("Error restarting agent '" + agentName + "'", e);
				}
				return null;
			});
		}
	}

	@Override
	public void disconnect(Long agentId) {
		String server = agentServers.get(agentId);
		if (server != null) {
			clusterManager.submitToServer(server, () -> {
				try {
					Session session = agentSessions.get(agentId);
					if (session != null)
						session.disconnect();
				} catch (Exception e) {
					logger.error("Error disconnecting agent with id '" + agentId + "'", e);
				}
				return null;
			});
		}
	}

	@Transactional
	@Override
	public void delete(Agent agent) {
		Long agentId = agent.getId();
		String agentName = agent.getName();
		
		var token = agent.getToken();
		removeReferences(agent);
		dao.remove(agent);
		lastUsedDateManager.delete(agent.getLastUsedDate());
		
		transactionManager.runAfterCommit(() -> {
			String server = agentServers.remove(agentId);
			if (server != null) {
				clusterManager.submitToServer(server, () -> {
					try {
						Session prevSession = agentSessions.remove(agentId);
						if (prevSession != null) {
							new Message(MessageTypes.STOP, new byte[0]).sendBy(prevSession);
							prevSession.disconnect();
						}
					} catch (Exception e) {
						logger.error("Error disconnecting agent '" + agentName + "'", e);						
					}
					return null;
				});
			}
		});
		dao.remove(token);
	}
	
	@Transactional
	@Override
	public void pause(Agent agent) {
		agent.setPaused(true);
		dao.persist(agent);
	}

	@Transactional
	@Override
	public void resume(Agent agent) {
		agent.setPaused(false);
		dao.persist(agent);
	}

	@Override
	public void attributesUpdated(Agent agent) {
		Long agentId = agent.getId();
		String agentName = agent.getName();
		var attributes = agent.getAttributeMap();
		String server = agentServers.get(agentId);
		if (server != null) {
			clusterManager.submitToServer(server, () -> {
				try {
					Session session = agentSessions.get(agent.getId());
					if (session != null) {
						byte[] attributeBytes = SerializationUtils.serialize((Serializable) attributes);
						new Message(MessageTypes.UPDATE_ATTRIBUTES, attributeBytes).sendBy(session);
					}
				} catch (Exception e) {
					logger.error("Error updating attributes of agent '" + agentName + "'", e);
				}
				return null;
			});
		}
	}

	@Override
	public List<String> getAgentLog(Agent agent) {
		Long agentId = agent.getId();
		String server = agentServers.get(agentId);
		if (server != null) {
			return clusterManager.runOnServer(server, () -> {
				Session session = agentSessions.get(agentId);
				if (session != null) {
					try {
						return WebsocketUtils.call(session, new LogRequest(), 60000);
					} catch (InterruptedException | TimeoutException e) {
						throw new RuntimeException(e);
					}
				} else { 
					return new ArrayList<>();
				}
			});
		} else { 
			return new ArrayList<>();
		}
	}

	@Override
	public Session getAgentSession(Long agentId) {
		return agentSessions.get(agentId);
	}

}
