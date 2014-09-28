package com.pmease.gitplex.web.page.repository.admin.integrationpolicy;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.wicket.behavior.ConfirmBehavior;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.gitplex.core.model.IntegrationPolicy;

@SuppressWarnings("serial")
abstract class IntegrationPolicyPanel extends Panel {

	private final IntegrationPolicy policy;
	
	public IntegrationPolicyPanel(String id, IntegrationPolicy policy) {
		super(id);
		
		this.policy = policy;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				IntegrationPolicyEditor editor = new IntegrationPolicyEditor("policy", policy) {

					@Override
					protected void onSave(AjaxRequestTarget target, IntegrationPolicy policy) {
						IntegrationPolicyPanel.this.onSave(target, policy);
						Component policyViewer = BeanContext.viewBean("policy", policy).setOutputMarkupId(true);
						IntegrationPolicyPanel.this.replace(policyViewer);
						target.add(policyViewer);
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						Component policyViewer = BeanContext.viewBean("policy", policy).setOutputMarkupId(true);
						IntegrationPolicyPanel.this.replace(policyViewer);
						target.add(policyViewer);
					}
					
				};
				IntegrationPolicyPanel.this.replace(editor);
				target.add(editor);
			}
			
		});
		
		add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDelete(target);
			}
			
		}.add(new ConfirmBehavior("Do you really want to delete this policy?")));
		
		add(BeanContext.viewBean("policy", policy).setOutputMarkupId(true));
	}
	
	protected abstract void onDelete(AjaxRequestTarget target);

	protected abstract void onSave(AjaxRequestTarget target, IntegrationPolicy policy);
	
}
