package io.onedev.server.service;

import io.onedev.server.model.Build;
import io.onedev.server.model.BuildLabel;

import java.util.List;

public interface BuildLabelService extends EntityLabelService<BuildLabel> {

	void create(BuildLabel buildLabel);

	void populateLabels(List<Build> builds);
}
