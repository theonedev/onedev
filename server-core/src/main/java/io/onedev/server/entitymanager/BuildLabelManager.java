package io.onedev.server.entitymanager;

import io.onedev.server.model.Build;
import io.onedev.server.model.BuildLabel;

import java.util.List;

public interface BuildLabelManager extends EntityLabelManager<BuildLabel> {

	void create(BuildLabel buildLabel);

	void populateLabels(List<Build> builds);
}
