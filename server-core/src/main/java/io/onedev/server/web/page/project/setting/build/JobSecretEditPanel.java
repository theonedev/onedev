package io.onedev.server.web.page.project.setting.build;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

public abstract class JobSecretEditPanel extends Panel {
	
	private final int index;
	
	public JobSecretEditPanel(String id, int index) {
		super(id);
		this.index = index;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		
		JobSecret editingSecret;
		if (index != -1) 
			editingSecret = getProject().getBuildSetting().getJobSecrets().get(index);
		else 
			editingSecret = new JobSecret();
		
		BeanEditor editor = BeanContext.edit("editor", editingSecret);
		form.add(editor);
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancelled(target);
			}
			
		});
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				List<JobSecret> secrets = getProject().getBuildSetting().getJobSecrets();
				String action;
				String oldAuditContent;
				if (index == -1) {
					secrets.add(editingSecret);
					action = "created job secret \"" + editingSecret.getName() + "\"";
					oldAuditContent = null;					
				} else {
					var oldSecret = secrets.set(index, editingSecret);
					action = "changed job secret \"" + editingSecret.getName() + "\"";
					oldAuditContent = VersionedXmlDoc.fromBean(oldSecret).toXML();
				}
				var newAuditContent = VersionedXmlDoc.fromBean(editingSecret).toXML();
				OneDev.getInstance(ProjectService.class).update(getProject());
				OneDev.getInstance(AuditService.class).audit(getProject(), action, oldAuditContent, newAuditContent);
				onSaved(target);
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
				onCancelled(target);
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}
			
		});
		add(form);
	}

	protected abstract Project getProject();
	
	protected abstract void onCancelled(AjaxRequestTarget target);

	protected abstract void onSaved(AjaxRequestTarget target);
}
