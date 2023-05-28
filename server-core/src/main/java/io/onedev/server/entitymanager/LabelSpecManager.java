package io.onedev.server.entitymanager;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.LabelSpec;
import io.onedev.server.persistence.dao.EntityManager;

public interface LabelSpecManager extends EntityManager<LabelSpec> {
	
	@Nullable
	LabelSpec find(String name);
	
	int count(@Nullable String term);
	
	List<LabelSpec> query(@Nullable String term, int firstResult, int maxResults);

	void sync(List<LabelSpec> labelSpecs);
	
	void create(LabelSpec labelSpec);
	
	void update(LabelSpec labelSpec);
	
}
