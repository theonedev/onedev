package io.onedev.server.web.component.project.info;

import java.util.Collection;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;

@SuppressWarnings("serial")
abstract class ForkOptionPanel extends Panel {

	private final IModel<Project> projectModel;
	
	public ForkOptionPanel(String id, IModel<Project> projectModel) {
		super(id);
		this.projectModel = projectModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Project project = new Project();
		project.setForkedFrom(getProject());
		project.setName(getProject().getName() + "." + SecurityUtils.getUser().getName());
		project.setIssueManagementEnabled(false);
		
		Collection<String> properties = Sets.newHashSet("name", "description", "issueManagementEnabled");
		
		BeanEditor editor = BeanContext.edit("editor", project, properties, false);
		
		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		
		form.add(editor);
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
				Project projectWithSameName = projectManager.find(project.getName());
				if (projectWithSameName != null) {
					editor.error(new Path(new PathNode.Named("name")),
							"This name has already been used by another project");
					target.add(form);
				} else {
					projectManager.fork(getProject(), project);
					Session.get().success("Project forked");
					setResponsePage(ProjectBlobPage.class, ProjectBlobPage.paramsOf(project));
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
		
		add(form);
	}
	
	private Project getProject() {
		return projectModel.getObject();
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		super.onDetach();
	}

	protected abstract void onClose(AjaxRequestTarget target);
	
}
