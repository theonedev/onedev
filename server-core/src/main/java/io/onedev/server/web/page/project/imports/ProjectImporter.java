package io.onedev.server.web.page.project.imports;

import java.io.Serializable;

public interface ProjectImporter extends Serializable {

	String getName();
	
	ProjectImportPanel render(String componentId);
	
}
