package io.onedev.server.web.page.project.imports;

import org.apache.wicket.markup.html.panel.Panel;

public abstract class ProjectImportPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public ProjectImportPanel(String id) {
		super(id);
	}

	protected ProjectImportPage getImportPage() {
		return (ProjectImportPage) getPage();
	}
	
}
