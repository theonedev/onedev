package io.onedev.server.entitymanager.impl;

import com.google.common.base.Splitter;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.core.HazelcastInstance;
import io.onedev.agent.AgentData;
import io.onedev.agent.Message;
import io.onedev.agent.MessageTypes;
import io.onedev.agent.WebsocketUtils;
import io.onedev.agent.job.LogRequest;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.AgentAttributeManager;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.entitymanager.AgentTokenManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.agent.AgentConnected;
import io.onedev.server.event.agent.AgentDisconnected;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentAttribute;
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
import io.onedev.server.util.validation.AttributeNameValidator;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

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
import java.util.stream.Collectors;

@Singleton
public class DefaultAgentManager extends BaseEntityManager<Agent> implements AgentManager, Serializable {

	private final AgentAttributeManager attributeManager;
	
	private final AgentTokenManager tokenManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final TransactionManager transactionManager;
	
	private final ClusterManager clusterManager;
	
	private final String agentVersion;
	
	private final Collection<String> agentLibs;
	
	private final Map<Long, Session> agentSessions = new ConcurrentHashMap<>();
	
	private volatile Map<Long, UUID> agentServers;
	
	private volatile Map<String, String> osNames;
	
	private volatile Map<String, String> osArchs;
	
	@Inject
	public DefaultAgentManager(Dao dao, AgentAttributeManager attributeManager, AgentTokenManager tokenManager, 
			ListenerRegistry listenerRegistry, TransactionManager transactionManager, 
			ClusterManager clusterManager) {
		super(dao);
		
		this.attributeManager = attributeManager;
		this.tokenManager = tokenManager;
		this.listenerRegistry = listenerRegistry;
		this.transactionManager = transactionManager;
		this.clusterManager = clusterManager;
	
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

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(AgentManager.class);
	}
	
	@SuppressWarnings("unchecked")
	@Sessional
	@Listen
	public void on(SystemStarted event) {
		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance(); 
		
		agentServers = hazelcastInstance.getReplicatedMap("agentServers");
		osNames = hazelcastInstance.getReplicatedMap("agentOsNames");
		osArchs = hazelcastInstance.getReplicatedMap("agentOsArchs");
		
		hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {

			@Override
			public void memberAdded(MembershipEvent membershipEvent) {
			}

			@Override
			public void memberRemoved(MembershipEvent membershipEvent) {
				if (clusterManager.isLeaderServer()) {
					for (var id: agentServers.entrySet().stream()
							.filter(it->it.getValue().equals(membershipEvent.getMember().getUuid()))
							.map(it->it.getKey())
							.collect(Collectors.toSet())) {
						agentServers.remove(id);
					}
				}
			}
			
		});
		
		Query<Object[]> query = dao.getSession().createQuery(String.format("select %s, %s from Agent", 
				Agent.PROP_OS_NAME, Agent.PROP_OS_ARCH));
		for (Object[] row: query.list()) { 
			osNames.put((String) row[0], (String) row[0]);
			osArchs.put((String) row[1], (String) row[1]);
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
		if (!OneDev.getInstance().isReady()) 
			throw new ExplicitException("Server not ready");
		
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
			Agent agent = findByName(data.getName());
			if (agent == null) {
				agent = new Agent();
				agent.setToken(token);
				agent.setOsName(data.getOsInfo().getOsName());
				agent.setOsVersion(data.getOsInfo().getOsVersion());
				agent.setOsArch(data.getOsInfo().getOsArch());
				agent.setName(data.getName());
				agent.setCpus(data.getCpus());
				agent.setTemporal(data.isTemporal());
				agent.setIpAddress(data.getIpAddress());
				save(agent);
				
				for (Map.Entry<String, String> entry: data.getAttributes().entrySet()) {
					AgentAttribute attribute = new AgentAttribute();
					attribute.setAgent(agent);
					attribute.setName(entry.getKey());
					attribute.setValue(entry.getValue());
					attributeManager.save(attribute);
					agent.getAttributes().add(attribute);
				}
			} else if (agentSessions.containsKey(agent.getId())) {
				throw new ExplicitException("Name '" + data.getName() + "' already used by another agent");
			} else {
				agent.setOsName(data.getOsInfo().getOsName());
				agent.setOsVersion(data.getOsInfo().getOsVersion());
				agent.setOsArch(data.getOsInfo().getOsArch());
				agent.setIpAddress(data.getIpAddress());
				agent.setCpus(data.getCpus());
				agent.setTemporal(data.isTemporal());
				save(agent);
				attributeManager.syncAttributes(agent, data.getAttributes());
			}

			agentServers.put(agent.getId(), clusterManager.getLocalServerUUID());
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
		agentServers.remove(agentId);
		agentSessions.remove(agentId);
		Agent agent = get(agentId);
		if (agent != null) {
			if (agent.isTemporal()) {
				removeReferences(agent);
				dao.remove(agent);
			}
			listenerRegistry.post(new AgentDisconnected(agent));
		}
	}

	@Override
	public void save(Agent agent) {
		super.save(agent);
		
    	transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				osNames.put(agent.getOsName(), agent.getOsName());
				osArchs.put(agent.getOsArch(), agent.getOsArch());
			}
    		
    	});
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

	@Override
	public Map<Long, UUID> getAgentServers() {
		return agentServers;
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
		UUID serverUUID = agentServers.get(agentId);
		if (serverUUID != null) {
			clusterManager.submitToServer(serverUUID, new ClusterTask<Void>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Void call() throws Exception {
					Session session = agentSessions.get(agentId);
					if (session != null)
						new Message(MessageTypes.RESTART, new byte[0]).sendBy(session);
					return null;
				}
				
			});
		}
	}

	@Transactional
	@Override
	public void delete(Agent agent) {
		for (Agent eachAgent: agent.getToken().getAgents()) {
			removeReferences(eachAgent);
			dao.remove(eachAgent);
			Long eachAgentId = eachAgent.getId();
			
			transactionManager.runAfterCommit(new ClusterRunnable() {

				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					UUID serverUUID = agentServers.remove(eachAgentId);
					if (serverUUID != null) {
						clusterManager.submitToServer(serverUUID, new ClusterTask<Void>() {

							private static final long serialVersionUID = 1L;

							@Override
							public Void call() throws Exception {
								Session prevSession = agentSessions.remove(eachAgentId);
								if (prevSession != null) {
									new Message(MessageTypes.STOP, new byte[0]).sendBy(prevSession);
									prevSession.disconnect();
								}
								return null;
							}
							
						});
					}
				}
				
			});
		}
		dao.remove(agent.getToken());
	}

	@Transactional
	@Override
	public void pause(Agent agent) {
		agent.setPaused(true);
		save(agent);
	}

	@Transactional
	@Override
	public void resume(Agent agent) {
		agent.setPaused(false);
		save(agent);
	}

	@Override
	public void attributesUpdated(Agent agent) {
		Long agentId = agent.getId();
		var attributes = agent.getAttributeMap();
		UUID serverUUID = agentServers.get(agentId);
		if (serverUUID != null) {
			clusterManager.submitToServer(serverUUID, new ClusterTask<Void>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Void call() throws Exception {
					Session session = agentSessions.get(agent.getId());
					if (session != null) {
						byte[] attributeBytes = SerializationUtils.serialize((Serializable) attributes);
						new Message(MessageTypes.UPDATE_ATTRIBUTES, attributeBytes).sendBy(session);
					}
					return null;
				}
				
			});
		}
	}

	@Override
	public List<String> getAgentLog(Agent agent) {
		Long agentId = agent.getId();
		UUID serverUUID = agentServers.get(agentId);
		if (serverUUID != null) {
			return clusterManager.runOnServer(serverUUID, new ClusterTask<List<String>>() {

				private static final long serialVersionUID = 1L;

				@Override
				public List<String> call() throws Exception {
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
