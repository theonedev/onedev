package io.onedev.server.util.facade;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.onedev.server.util.MapProxy;

public class GroupCache extends MapProxy<Long, GroupFacade> {

	private static final long serialVersionUID = 1L;
	
	public GroupCache(Map<Long, GroupFacade> delegate) {
		super(delegate);
	}
	
	@Nullable
	public GroupFacade find(String name) {
		for (GroupFacade facade: values()) {
			if (name.equals(facade.getName()))
				return facade;
		}
		return null;
	}
	
	@Override
	public GroupCache clone() {
		return new GroupCache(new HashMap<>(delegate));
	}
	
}
