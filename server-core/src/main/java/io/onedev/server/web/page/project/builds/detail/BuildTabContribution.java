package io.onedev.server.web.page.project.builds.detail;

import java.util.List;

import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.server.model.Build;

@ExtensionPoint
public interface BuildTabContribution {
	
	List<BuildTab> getTabs(Build build);
	
	int getOrder();
	
}
