package io.onedev.server.util.facade;

import io.onedev.server.model.User;
import io.onedev.server.util.MapProxy;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.PersonIdent;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PackCache extends MapProxy<Long, PackFacade> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public PackCache(Map<Long, PackFacade> delegate) {
		super(delegate);
	}
	
	@Override
	public PackCache clone() {
		return new PackCache(new HashMap<>(delegate));
	}
	
	public boolean hasPacks(Long projectId) {
		return values().stream().anyMatch(it -> it.getProjectId().equals(projectId));
	}
	
}
