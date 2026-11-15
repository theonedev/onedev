package io.onedev.server.service.impl;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shiro.authc.DisabledAccountException;

import com.hazelcast.map.IMap;

import io.onedev.server.cluster.ClusterService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.exception.ServerNotReadyException;
import io.onedev.server.model.AccessToken;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Restrictions;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.UserService;

@Singleton
public class DefaultAccessTokenService extends BaseEntityService<AccessToken> implements AccessTokenService {

	@Inject
	private UserService userService;

	@Inject
	private ClusterService clusterService;

	@Inject
	private SessionService sessionService;
	
	private volatile IMap<String, Long> temporalAccessTokens;

	@Transactional
	@Override
	public void createOrUpdate(AccessToken token) {
		dao.persist(token);
	}

	@Override
	public final AccessToken findByValue(String value) {
		return sessionService.call(() -> {
			if (temporalAccessTokens == null)
				throw new ServerNotReadyException();

			var valueHash = DigestUtils.sha256Hex(value);
			var token = find(newCriteria().add(Restrictions.eq(AccessToken.PROP_VALUE_HASH, valueHash)));
			if (token != null) {
				if (token.isExpired())
					return null;
				if (token.getOwner().isDisabled())
					throw new DisabledAccountException("Account is disabled");
				if (token.getOauthGrant() != null && !token.getOauthGrant().isActive())
					return null;
				return token;
			}
			var userId = temporalAccessTokens.get(valueHash);
			if (userId == null)
				return null;
			var user = userService.load(userId);
			if (user.isDisabled())
				throw new DisabledAccountException("Account is disabled");
			token = new AccessToken();
			token.setOwner(user);
			token.setHasOwnerPermissions(true);
			return token;
		});
	}

	@Listen
	public void on(SystemStarting event) {
		temporalAccessTokens = Objects.requireNonNull(clusterService.getHazelcastInstance()).getMap("temporalAccessTokens");
	}

	@Override
	public String createTemporal(Long userId, long secondsToExpire) {
		var value = RandomStringUtils.secure().nextAlphanumeric(40);
		temporalAccessTokens.put(DigestUtils.sha256Hex(value), userId, secondsToExpire, TimeUnit.SECONDS);
		return value;
	}
	
}
