package io.onedev.server.entitymanager;

import io.onedev.server.model.LabelSpec;
import io.onedev.server.persistence.dao.EntityManager;

import javax.annotation.Nullable;
import java.util.List;

public interface LabelSpecManager extends EntityManager<LabelSpec> {
	
	@Nullable
	LabelSpec find(String name);

	void sync(List<LabelSpec> labelSpecs);
	
	void create(LabelSpec labelSpec);
	
	void update(LabelSpec labelSpec);
	
}
