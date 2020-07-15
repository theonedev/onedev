package io.onedev.server.web.editable.job.projectdependency;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.buildspec.job.ProjectDependency;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

@SuppressWarnings("serial")
abstract class ProjectDependencyEditPanel extends Panel implements JobAware {

	private final List<ProjectDependency> dependencies;
	
	private final int dependencyIndex;
	
	public ProjectDependencyEditPanel(String id, List<ProjectDependency> dependencies, int dependencyIndex) {
		super(id);
	
		this.dependencies = dependencies;
		this.dependencyIndex = dependencyIndex;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		ProjectDependency dependency;
		if (dependencyIndex != -1)
			dependency = dependencies.get(dependencyIndex);
		else
			dependency = new ProjectDependency();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}
			
		};
		
		form.add(new FencedFeedbackPanel("feedback", form));
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(ProjectDependencyEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		BeanEditor editor = BeanContext.edit("editor", dependency);
		form.add(editor);
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				if (dependencyIndex != -1) { 
					ProjectDependency oldDependency = dependencies.get(dependencyIndex);
					if (!dependency.getProjectName().equals(oldDependency.getProjectName()) && getDependency(dependency.getProjectName()) != null) {
						editor.error(new Path(new PathNode.Named("projectName")),
								"Dependency to this project is already defined");
					}
				} else if (getDependency(dependency.getProjectName()) != null) {
					editor.error(new Path(new PathNode.Named("projectName")),
							"Dependency to this project is already defined");
				}

				if (editor.isValid()) {
					if (dependencyIndex != -1) {
						dependencies.set(dependencyIndex, dependency);
					} else {
						dependencies.add(dependency);
					}
					onSave(target);
				} else {
					target.add(form);
				}
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(ProjectDependencyEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.setOutputMarkupId(true);
		
		add(form);
	}

	private ProjectDependency getDependency(String projectName) {
		for (ProjectDependency dependency: dependencies) {
			if (projectName.equals(dependency.getProjectName()))
				return dependency;
		}
		return null;
	}
	
	protected abstract void onSave(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);

}
