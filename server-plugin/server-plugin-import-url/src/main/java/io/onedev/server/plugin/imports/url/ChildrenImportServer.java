package io.onedev.server.plugin.imports.url;

import io.onedev.server.annotation.Editable;

@Editable
public class ChildrenImportServer extends ImportServer {

	private static final long serialVersionUID = 1L;
		
	@Editable(order=200, name="Child Project", placeholderProvider="getProjectPlaceholder", description="Specify child project to import into at OneDev side")
	@Override
	public String getProject() {
		return super.getProject();
	}

	@Override
	public void setProject(String project) {
		super.setProject(project);
	}

}
