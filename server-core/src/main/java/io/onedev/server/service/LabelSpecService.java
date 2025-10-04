package io.onedev.server.service;

import io.onedev.server.model.LabelSpec;

import javax.annotation.Nullable;
import java.util.List;

public interface LabelSpecService extends EntityService<LabelSpec> {
	
	@Nullable
	LabelSpec find(String name);

	void sync(List<LabelSpec> labelSpecs);
	
	void createOrUpdate(LabelSpec labelSpec);
	
}
