package io.onedev.server.web.page.project.setting.webhook;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.editable.BeanContext;

abstract class WebHookEditPanel extends Panel {

	private final int hookIndex;

	public WebHookEditPanel(String id, int hookIndex) {
		super(id);
		this.hookIndex = hookIndex;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebHook hook = hookIndex != -1
				? SerializationUtils.clone(getProject().getWebHooks().get(hookIndex))
				: new WebHook();

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
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(WebHookEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}

		});

		var editor = BeanContext.edit("editor", hook);
		form.add(editor);

		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				if (editor.isValid()) {
					String oldAuditContent = null;
					String verb;
					if (hookIndex != -1) {
						oldAuditContent = VersionedXmlDoc.fromBean(getProject().getWebHooks().get(hookIndex)).toXML();
						getProject().getWebHooks().set(hookIndex, hook);
						verb = "changed";
					} else {
						getProject().getWebHooks().add(hook);
						verb = "added";
					}
					var newAuditContent = VersionedXmlDoc.fromBean(hook).toXML();
					OneDev.getInstance(ProjectService.class).update(getProject());
					OneDev.getInstance(AuditService.class).audit(getProject(), verb + " web hook \"" + hook.getPostUrl() + "\"", oldAuditContent, newAuditContent);
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
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(WebHookEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}

		});

		form.setOutputMarkupId(true);
		add(form);
	}

	protected abstract Project getProject();

	protected abstract void onSave(AjaxRequestTarget target);

	protected abstract void onCancel(AjaxRequestTarget target);

}
