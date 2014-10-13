package com.pmease.gitplex.web.page.account.subscription;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.wicket.behavior.ConfirmBehavior;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.gitplex.core.model.ChangeSubscription;

@SuppressWarnings("serial")
abstract class ChangeSubscriptionPanel extends Panel {

	private final ChangeSubscription subscription;
	
	public ChangeSubscriptionPanel(String id, ChangeSubscription subscription) {
		super(id);
		
		this.subscription = subscription;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				ChangeSubscriptionEditor editor = new ChangeSubscriptionEditor("subscription", subscription) {

					@Override
					protected void onSave(AjaxRequestTarget target, ChangeSubscription subscription) {
						ChangeSubscriptionPanel.this.onSave(target, subscription);
						Component subscriptionViewer = BeanContext.viewBean("subscription", subscription).setOutputMarkupId(true);
						ChangeSubscriptionPanel.this.replace(subscriptionViewer);
						target.add(subscriptionViewer);
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						Component subscriptionViewer = BeanContext.viewBean("subscription", subscription).setOutputMarkupId(true);
						ChangeSubscriptionPanel.this.replace(subscriptionViewer);
						target.add(subscriptionViewer);
					}
					
				};
				ChangeSubscriptionPanel.this.replace(editor);
				target.add(editor);
			}
			
		});
		
		add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDelete(target);
			}
			
		}.add(new ConfirmBehavior("Do you really want to delete this subscription?")));
		
		add(BeanContext.viewBean("subscription", subscription).setOutputMarkupId(true));
	}
	
	protected abstract void onDelete(AjaxRequestTarget target);

	protected abstract void onSave(AjaxRequestTarget target, ChangeSubscription subscription);
	
}
