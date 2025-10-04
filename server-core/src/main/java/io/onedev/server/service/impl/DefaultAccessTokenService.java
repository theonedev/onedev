package io.onedev.server.service.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.DisabledAccountException;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import io.onedev.server.cluster.ClusterService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.exception.ServerNotReadyException;
import io.onedev.server.model.AccessToken;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.UserService;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.util.facade.AccessTokenCache;

@Singleton
public class DefaultAccessTokenService extends BaseEntityService<AccessToken> implements AccessTokenService {

	@Inject
	private UserService userService;

	@Inject
	private ClusterService clusterService;

	@Inject
	private TransactionService transactionService;

	@Inject
	private SessionService sessionService;
	
	private volatile AccessTokenCache cache;

	private volatile IMap<String, Long> temporalAccessTokens;

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
		return sessionService.call(new Callable<AccessToken>() {

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
							accessToken.setOwner(userService.load(userId));
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
		HazelcastInstance hazelcastInstance = clusterService.getHazelcastInstance();

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
			transactionService.runAfterCommit(() -> cache.put(facade.getId(), facade));
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof AccessToken) {
			var id = event.getEntity().getId();
			transactionService.runAfterCommit(() -> cache.remove(id));
		}
	}
	
}