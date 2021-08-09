package io.onedev.server.web.page.project.imports;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.imports.Importer;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.imports.ProjectImporterContribution;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.imports.ImportPanel;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.page.project.ProjectListPage;

@SuppressWarnings("serial")
public class ProjectImportPage<Where extends Serializable, What extends Serializable, How extends Serializable> extends LayoutPage {

	private static final String PARAM_IMPORTER = "importer";
	
	private ProjectImporter<Where, What, How> importer;
	
	@SuppressWarnings("unchecked")
	public ProjectImportPage(PageParameters params) {
		super(params);
		
		String importerName = params.get(PARAM_IMPORTER).toString();
		for (ProjectImporterContribution contribution: OneDev.getExtensions(ProjectImporterContribution.class)) {
			for (ProjectImporter<? extends Serializable, ? extends Serializable, ? extends Serializable> importer: contribution.getImporters()) {
				if (importer.getName().equals(importerName)) {
					this.importer = (ProjectImporter<Where, What, How>) importer;
					break;
				}
			}
		}
		
		if (importer == null)
			throw new RuntimeException("Undefined importer: " + importerName);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ImportPanel<Where, What, How>("importer") {

			@Override
			protected Importer<Where, What, How> getImporter() {
				return importer;
			}

			@Override
			protected String doImport(Where where, What what, How how, 
					boolean dryRun, TaskLogger logger) {
				return importer.doImport(where, what, how, dryRun, logger);
			}

			@Override
			protected void onImportSuccessful(AjaxRequestTarget target) {
				EntitySort sort = new EntitySort();
				sort.setField(Project.NAME_UPDATE_DATE);
				sort.setDirection(Direction.DESCENDING);
				ProjectQuery query = new ProjectQuery(null, Lists.newArrayList(sort));
				PageParameters params = ProjectListPage.paramsOf(query.toString(), 0, 0);
				throw new RestartResponseException(ProjectListPage.class, params); 
			}
			
		});
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getUser() != null;
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Importing projects from " + importer.getName());
	}
	
	public static PageParameters paramsOf(String importer) {
		PageParameters params = new PageParameters();
		params.add(PARAM_IMPORTER, importer);
		return params;
	}
	
}
