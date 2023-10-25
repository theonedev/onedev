package io.onedev.server.entitymanager;

import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.dao.EntityManager;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface PackManager extends EntityManager<Pack> {

	Collection<String> getPackNames(@Nullable Project project);

	List<Pack> query(Project project, @Nullable String type, @Nullable String term, int firstResult, int maxResults);

	@Nullable
	Pack find(Project project, String packName);
	
	int count(Project project, @Nullable String type, @Nullable String term);
	
    void create(Pack pack);

	void update(Pack pack);
	
}
