package io.onedev.server.web.editable.job.service;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.buildspec.job.JobService;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

@SuppressWarnings("serial")
abstract class JobServiceEditPanel extends Panel implements BuildSpecAware, JobAware {

	private final List<JobService> services;
	
	private final int serviceIndex;
	
	public JobServiceEditPanel(String id, List<JobService> services, int serviceIndex) {
		super(id);
	
		this.services = services;
		this.serviceIndex = serviceIndex;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		JobService service;
		if (serviceIndex != -1)
			service = services.get(serviceIndex);
		else
			service = new JobService();

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
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(JobServiceEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		BeanEditor editor = BeanContext.edit("editor", service);
		form.add(editor);
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				if (serviceIndex != -1) { 
					JobService oldService = services.get(serviceIndex);
					if (!service.getName().equals(oldService.getName()) && getService(service.getName()) != null) {
						editor.error(new Path(new PathNode.Named("name")),
								"Service is already defined");
					}
				} else if (getService(service.getName()) != null) {
					editor.error(new Path(new PathNode.Named("name")),
							"Service is already defined");
				}

				if (editor.isValid()) {
					if (serviceIndex != -1) {
						services.set(serviceIndex, service);
					} else {
						services.add(service);
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
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(JobServiceEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.setOutputMarkupId(true);
		
		add(form);
	}

	private JobService getService(String name) {
		for (JobService service: services) {
			if (name.equals(service.getName()))
				return service;
		}
		return null;
	}
	
	protected abstract void onSave(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);

}
