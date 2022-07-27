package io.onedev.server.imports;

import java.util.Collection;

import io.onedev.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface IssueImporterContribution {

	Collection<IssueImporter> getImporters();
	
	int getOrder();
	
}
