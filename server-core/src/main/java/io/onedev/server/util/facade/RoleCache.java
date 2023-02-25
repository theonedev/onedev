package io.onedev.server.util.facade;

import io.onedev.server.util.MapProxy;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RoleCache extends MapProxy<Long, RoleFacade> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public RoleCache(Map<Long, RoleFacade> delegate) {
		super(delegate);
	}
	
	@Nullable
	public RoleFacade find(String name) {
		for (RoleFacade facade: values()) {
			if (name.equals(facade.getName()))
				return facade;
		}
		return null;
	}
	
	@Override
	public RoleCache clone() {
		return new RoleCache(new HashMap<>(delegate));
	}
	
}
