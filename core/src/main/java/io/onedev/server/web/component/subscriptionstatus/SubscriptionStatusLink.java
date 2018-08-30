package io.onedev.server.web.component.subscriptionstatus;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;

@SuppressWarnings("serial")
public abstract class SubscriptionStatusLink extends DropdownLink {

	public SubscriptionStatusLink(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (getSubscriptionStatus())
					return "subscribed";
				else
					return "not-subscribed";
			}
			
		}));
		
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new SubscriptionStatusPanel(id) {

			@Override
			protected boolean getSubscriptionStatus() {
				return SubscriptionStatusLink.this.getSubscriptionStatus();
			}

			@Override
			protected void onSubscriptionStatusChange(AjaxRequestTarget target, boolean subscriptionStatus) {
				SubscriptionStatusLink.this.onSubscriptionStatusChange(target, subscriptionStatus);
				dropdown.close();
			}
			
		};
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SubscriptionStatusResourceReference()));
	}

	protected abstract boolean getSubscriptionStatus();
	
	protected abstract void onSubscriptionStatusChange(AjaxRequestTarget target, boolean subscriptionStatus);
	
}
