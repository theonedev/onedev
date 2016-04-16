package com.pmease.gitplex.web.page.depot.setting.integrationpolicy;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.wicket.component.SubmitTypeAjaxSubmitLink;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.gitplex.core.entity.component.IntegrationPolicy;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
abstract class IntegrationPolicyEditor extends Panel {

	private final IntegrationPolicy policy;
	
	public IntegrationPolicyEditor(String id, IntegrationPolicy policy) {
		super(id);
		
		this.policy = policy;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		Form<?> form = new Form<Void>("form");
		form.add(new NotificationPanel("feedback", form));
		form.add(BeanContext.editBean("editor", policy));
		form.add(new SubmitTypeAjaxSubmitLink("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				onSave(target, policy);
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
				onCancel(target);
			}
			
		});
		add(form);
	}

	protected abstract void onSave(AjaxRequestTarget target, IntegrationPolicy policy);
	
	protected abstract void onCancel(AjaxRequestTarget target);
}
