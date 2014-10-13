package com.pmease.gitplex.web.page.account.subscription;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.gitplex.core.model.ChangeSubscription;

@SuppressWarnings("serial")
abstract class ChangeSubscriptionEditor extends Panel {

	private final ChangeSubscription subscription;
	
	public ChangeSubscriptionEditor(String id, ChangeSubscription subscription) {
		super(id);
		
		this.subscription = subscription;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		Form<?> form = new Form<Void>("form");
		form.add(new FeedbackPanel("feedback", form));
		form.add(BeanContext.editBean("editor", subscription));
		form.add(new AjaxSubmitLink("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				onSave(target, subscription);
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

	protected abstract void onSave(AjaxRequestTarget target, ChangeSubscription subscription);
	
	protected abstract void onCancel(AjaxRequestTarget target);
}
