package io.onedev.server.imports;

import java.io.Serializable;
import java.util.Collection;

import io.onedev.commons.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface ProjectImporterContribution {

	Collection<ProjectImporter<? extends Serializable, ? extends Serializable, ? extends Serializable>> getImporters();
	
	int getOrder();
	
}
