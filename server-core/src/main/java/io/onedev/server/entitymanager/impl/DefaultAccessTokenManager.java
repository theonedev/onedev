package io.onedev.server.entitymanager.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.DisabledAccountException;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.AccessTokenManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.exception.ServerNotReadyException;
import io.onedev.server.model.AccessToken;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.util.facade.AccessTokenCache;

@Singleton
public class DefaultAccessTokenManager extends BaseEntityManager<AccessToken> implements AccessTokenManager {
	
	private final UserManager userManager;
	
	private final ClusterManager clusterManager;
	
	private final TransactionManager transactionManager;

	private final SessionManager sessionManager;
	
	private volatile AccessTokenCache cache;

	private volatile IMap<String, Long> temporalAccessTokens;
	
	@Inject
    public DefaultAccessTokenManager(Dao dao, UserManager userManager, ClusterManager clusterManager,
									 TransactionManager transactionManager, SessionManager sessionManager) {
        super(dao);
		this.userManager = userManager;
		this.clusterManager = clusterManager;
		this.transactionManager = transactionManager;
		this.sessionManager = sessionManager;
    }

	@Transactional
    @Override
    public void createOrUpdate(AccessToken projectToken) {
		dao.persist(projectToken);
    }

	@Sessional
	@Override
	public AccessToken findByOwnerAndName(User owner, String name) {
		if (cache != null) {
			var facade = cache.findByOwnerAndName(owner.getId(), name);
			if (facade != null) 
				return load(facade.getId());
			else 
				return null;
		} else {
			throw new ServerNotReadyException();
		}
	}

	private AccessToken checkDisabled(AccessToken accessToken) {
		if (accessToken.getOwner().isDisabled())
			throw new DisabledAccountException("Account is disabled");
		return accessToken;
	}

    @Override
    public final AccessToken findByValue(String value) {
		return sessionManager.call(new Callable<AccessToken>() {

			@Override
			public AccessToken call() throws Exception {
				if (cache != null) {
					var facade = cache.findByValue(value);
					if (facade != null) {
						return checkDisabled(load(facade.getId()));
					} else {
						Long userId = temporalAccessTokens.get(value);
						if (userId != null) {
							var accessToken = new AccessToken();
							accessToken.setOwner(userManager.load(userId));
							accessToken.setHasOwnerPermissions(true);
							accessToken.setValue(value);
							return checkDisabled(accessToken);
						} else {
							return null;
						}
					}
				} else {
					throw new ServerNotReadyException();
				}						
			}
		});		
    }

    @Sessional
    @Listen
    public void on(SystemStarting event) {
		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance();

		temporalAccessTokens = hazelcastInstance.getMap("temporalAccessTokens");
		
        cache = new AccessTokenCache(hazelcastInstance.getReplicatedMap("accessTokenCache"));
		for (var token: query())
			cache.put(token.getId(), token.getFacade());
    }

	@Override
	public String createTemporal(Long userId, long secondsToExpire) {
		var value = CryptoUtils.generateSecret();
		temporalAccessTokens.put(value, userId, secondsToExpire, TimeUnit.SECONDS);
		return value;
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof AccessToken) {
			var facade = ((AccessToken) event.getEntity()).getFacade();
			transactionManager.runAfterCommit(() -> cache.put(facade.getId(), facade));
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof AccessToken) {
			var id = event.getEntity().getId();
			transactionManager.runAfterCommit(() -> cache.remove(id));
		}
	}
	
}