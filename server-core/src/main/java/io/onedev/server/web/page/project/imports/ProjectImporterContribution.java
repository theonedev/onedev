package io.onedev.server.web.page.project.imports;

import java.util.Collection;

import io.onedev.commons.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface ProjectImporterContribution {

	Collection<ProjectImporter> getImporters();
	
}
