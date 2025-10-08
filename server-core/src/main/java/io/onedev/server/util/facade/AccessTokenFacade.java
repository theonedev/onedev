package io.onedev.server.util.facade;

import io.onedev.server.model.AccessToken;

import org.jspecify.annotations.Nullable;
import java.util.Date;

public class AccessTokenFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final Long ownerId;
	
	private final String name;
	
	private final String value;
	
	private final Date expireDate;
	
	public AccessTokenFacade(Long id, Long ownerId, String name, String value, @Nullable Date expireDate) {
		super(id);
		this.ownerId = ownerId;
		this.name = name;
		this.value = value;
		this.expireDate = expireDate;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	@Nullable
	public Date getExpireDate() {
		return expireDate;
	}

	public boolean isExpired() {
		return getExpireDate() != null && getExpireDate().before(new Date());
	}

	public static AccessTokenFacade of(@Nullable AccessToken token) {
		if (token != null)
			return token.getFacade();
		else
			return null;
	}
	
}
