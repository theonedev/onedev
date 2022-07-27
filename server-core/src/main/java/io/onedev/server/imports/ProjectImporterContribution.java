package io.onedev.server.imports;

import java.util.Collection;

import io.onedev.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface ProjectImporterContribution {

	Collection<ProjectImporter> getImporters();
	
	int getOrder();
	
}
