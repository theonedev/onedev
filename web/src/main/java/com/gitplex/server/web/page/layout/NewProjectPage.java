package com.gitplex.server.web.page.layout;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.server.web.editable.BeanEditor;
import com.gitplex.server.web.editable.PathSegment;
import com.gitplex.server.web.page.project.blob.ProjectBlobPage;

@SuppressWarnings("serial")
public class NewProjectPage extends LayoutPage {

	public NewProjectPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Project project = new Project();
		
		BeanEditor<?> editor = BeanContext.editBean("editor", project);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				ProjectManager projectManager = GitPlex.getInstance(ProjectManager.class);
				Project projectWithSameName = projectManager.find(project.getName());
				if (projectWithSameName != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another project");
				} else {
					projectManager.save(project, null);
					Session.get().success("New project created");
					setResponsePage(ProjectBlobPage.class, ProjectBlobPage.paramsOf(project));
				}
			}
			
		};
		form.add(editor);
		
		add(form);
	}

}
