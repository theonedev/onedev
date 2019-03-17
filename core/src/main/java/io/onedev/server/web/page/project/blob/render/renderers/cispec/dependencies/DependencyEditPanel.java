package io.onedev.server.web.page.project.blob.render.renderers.cispec.dependencies;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.Dependency;
import io.onedev.server.ci.Job;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.util.ajaxlistener.ConfirmLeaveListener;

@SuppressWarnings("serial")
public abstract class DependencyEditPanel extends Panel {

	private final List<Dependency> dependencies;
	
	private final int dependencyIndex;
	
	public DependencyEditPanel(String id, List<Dependency> dependencies, int dependencyIndex) {
		super(id);
	
		this.dependencies = dependencies;
		this.dependencyIndex = dependencyIndex;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Dependency dependency;
		if (dependencyIndex != -1)
			dependency = dependencies.get(dependencyIndex);
		else
			dependency = new Dependency();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}
			
		};
		
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
		
		BeanEditor editor = BeanContext.editBean("editor", dependency);
		form.add(editor);
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				if (dependencyIndex != -1) { 
					Dependency oldDependency = dependencies.get(dependencyIndex);
					if (!dependency.getJob().equals(oldDependency.getJob()) && getDependency(dependency.getJob()) != null) {
						editor.getErrorContext(new PathSegment.Property("job"))
								.addError("Dependency to this job is already defined");
					}
				} else if (getDependency(dependency.getJob()) != null) {
					editor.getErrorContext(new PathSegment.Property("job"))
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

	private Dependency getDependency(String job) {
		for (Dependency dependency: dependencies) {
			if (job.equals(dependency.getJob()))
				return dependency;
		}
		return null;
	}
	
	protected abstract void onSave(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);

	public abstract CISpec getEditingCISpec();
	
	public abstract Job getBelongingJob();
	
}
