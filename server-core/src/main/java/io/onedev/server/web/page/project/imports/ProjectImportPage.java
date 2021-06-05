package io.onedev.server.web.page.project.imports;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.web.page.layout.LayoutPage;

@SuppressWarnings("serial")
public class ProjectImportPage extends LayoutPage {

	public static final String MOUNT_PATH = "import";
	
	private static final String PARAM_STAGE = "stage";
	
	public static final String STAGE_INITIATE = "initiate";
	
	public static final String STAGE_CALLBACK = "callback";
	
	private static final String PARAM_IMPORTER = "importer";
	
	private ProjectImporter importer;
	
	public ProjectImportPage(PageParameters params) {
		super(params);
		
		String importerName = params.get(PARAM_IMPORTER).toString();
		for (ProjectImporterContribution contribution: OneDev.getExtensions(ProjectImporterContribution.class)) {
			for (ProjectImporter importer: contribution.getImporters()) {
				if (importer.getName().equals(importerName)) {
					this.importer = importer;
					break;
				}
			}
		}
		
		if (importer == null)
			throw new RuntimeException("Unexpected importer: " + importerName);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(importer.render("content"));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Importing Projects from " + importer.getName());
	}
	
	public boolean isCallbackStage() {
		return STAGE_CALLBACK.equals(getPageParameters().get(PARAM_STAGE).toString());
	}

	public static void addParams(PageParameters params, String stage, String importer) {
		params.add(PARAM_STAGE, stage);
		params.add(PARAM_IMPORTER, importer);
	}
	
}
