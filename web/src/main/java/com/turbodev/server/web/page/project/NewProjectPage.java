package com.turbodev.server.web.page.project;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.ProjectManager;
import com.turbodev.server.model.Project;
import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.web.ComponentRenderer;
import com.turbodev.server.web.component.link.ViewStateAwarePageLink;
import com.turbodev.server.web.editable.BeanContext;
import com.turbodev.server.web.editable.BeanEditor;
import com.turbodev.server.web.editable.PathSegment;
import com.turbodev.server.web.page.layout.LayoutPage;
import com.turbodev.server.web.page.project.blob.ProjectBlobPage;

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
				
				ProjectManager projectManager = TurboDev.getInstance(ProjectManager.class);
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

	@Override
	protected List<ComponentRenderer> getBreadcrumbs() {
		List<ComponentRenderer> breadcrumbs = super.getBreadcrumbs();
		
		breadcrumbs.add(new ComponentRenderer() {

			@Override
			public Component render(String componentId) {
				return new ViewStateAwarePageLink<Void>(componentId, ProjectListPage.class) {

					@Override
					public IModel<?> getBody() {
						return Model.of("Projects");
					}
					
				};
			}
			
		});

		breadcrumbs.add(new ComponentRenderer() {
			
			@Override
			public Component render(String componentId) {
				return new Label(componentId, "New Project") {

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						tag.setName("div");
					}
					
				};
			}
			
		});
		
		return breadcrumbs;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ProjectResourceReference()));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canCreateProjects();
	}
	
}
