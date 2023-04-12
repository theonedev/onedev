package io.onedev.server.entitymanager;

import io.onedev.server.model.LinkSpec;
import io.onedev.server.persistence.dao.EntityManager;

import javax.annotation.Nullable;
import java.util.List;

public interface LinkSpecManager extends EntityManager<LinkSpec> {

	@Nullable
	LinkSpec find(String name);
	
	List<LinkSpec> queryAndSort();

	void updateOrders(List<LinkSpec> links);
	
	void update(LinkSpec spec, @Nullable String oldName, @Nullable String oldOppositeName);

	void create(LinkSpec spec);
}
