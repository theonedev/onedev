package io.onedev.server.entitymanager;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.LabelSpec;
import io.onedev.server.persistence.dao.EntityManager;

public interface LabelManager extends EntityManager<LabelSpec> {
	
	@Nullable
	LabelSpec find(String name);
	
	int count(@Nullable String term);
	
	List<LabelSpec> query(@Nullable String term, int firstResult, int maxResults);

	void sync(List<LabelSpec> labels);
	
}
