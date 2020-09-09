package io.onedev.server.web.page.project;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;

@SuppressWarnings("serial")
public class NewProjectPage extends LayoutPage {

	public NewProjectPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Project project = new Project();
		
		Collection<String> properties = Sets.newHashSet("name", "description", "issueManagementEnabled");
		
		BeanEditor editor = BeanContext.edit("editor", project, properties, false);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
				Project projectWithSameName = projectManager.find(project.getName());
				if (projectWithSameName != null) {
					editor.error(new Path(new PathNode.Named("name")),
							"This name has already been used by another project");
				} else {
					projectManager.create(project);
					Session.get().success("New project created");
					setResponsePage(ProjectBlobPage.class, ProjectBlobPage.paramsOf(project));
				}
			}
			
		};
		form.add(editor);
		
		add(form);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canCreateProjects();
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Create Project");
	}
	
}
