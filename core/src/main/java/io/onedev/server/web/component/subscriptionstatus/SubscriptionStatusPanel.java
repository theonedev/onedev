package io.onedev.server.web.component.subscriptionstatus;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
abstract class SubscriptionStatusPanel extends Panel {

	public SubscriptionStatusPanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("subscribe") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onSubscriptionStatusChange(target, true);
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (getSubscriptionStatus())
					add(AttributeAppender.append("class", "active"));
			}

		});
		add(new AjaxLink<Void>("doNotSubscribe") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onSubscriptionStatusChange(target, false);
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (!getSubscriptionStatus())
					add(AttributeAppender.append("class", "active"));
			}
			
		});		
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SubscriptionStatusResourceReference()));
	}

	protected abstract boolean getSubscriptionStatus();
	
	protected abstract void onSubscriptionStatusChange(AjaxRequestTarget target, boolean subscriptionStatus);
	
}