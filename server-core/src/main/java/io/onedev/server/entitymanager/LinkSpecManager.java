package io.onedev.server.entitymanager;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.LinkSpec;
import io.onedev.server.persistence.dao.EntityManager;

public interface LinkSpecManager extends EntityManager<LinkSpec> {

	@Nullable
	LinkSpec find(String name);
	
	List<LinkSpec> queryAndSort();

	void updateOrders(List<LinkSpec> links);
	
	void save(LinkSpec spec, @Nullable String oldName, @Nullable String oldOppositeName);
}
