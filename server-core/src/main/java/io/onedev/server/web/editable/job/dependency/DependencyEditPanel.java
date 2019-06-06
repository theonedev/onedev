package io.onedev.server.web.editable.job.dependency;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.ci.CISpecAware;
import io.onedev.server.ci.JobDependency;
import io.onedev.server.ci.job.JobAware;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathElement;

@SuppressWarnings("serial")
abstract class DependencyEditPanel extends Panel implements CISpecAware, JobAware {

	private final List<JobDependency> dependencies;
	
	private final int dependencyIndex;
	
	public DependencyEditPanel(String id, List<JobDependency> dependencies, int dependencyIndex) {
		super(id);
	
		this.dependencies = dependencies;
		this.dependencyIndex = dependencyIndex;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		JobDependency dependency;
		if (dependencyIndex != -1)
			dependency = dependencies.get(dependencyIndex);
		else
			dependency = new JobDependency();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}
			
		};
		
		form.add(new NotificationPanel("feedback", form));
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(DependencyEditPanel.this));
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
					JobDependency oldDependency = dependencies.get(dependencyIndex);
					if (!dependency.getJobName().equals(oldDependency.getJobName()) && getDependency(dependency.getJobName()) != null) {
						editor.getErrorContext(new PathElement.Named("jobName"))
								.addError("Dependency to this job is already defined");
					}
				} else if (getDependency(dependency.getJobName()) != null) {
					editor.getErrorContext(new PathElement.Named("jobName"))
							.addError("Dependency to this job is already defined");
				}

				if (!editor.hasErrors(true)) {
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
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(DependencyEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.setOutputMarkupId(true);
		
		add(form);
	}

	private JobDependency getDependency(String job) {
		for (JobDependency dependency: dependencies) {
			if (job.equals(dependency.getJobName()))
				return dependency;
		}
		return null;
	}
	
	protected abstract void onSave(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);

}
