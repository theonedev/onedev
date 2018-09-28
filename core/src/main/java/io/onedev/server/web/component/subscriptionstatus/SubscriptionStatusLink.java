package io.onedev.server.web.component.subscriptionstatus;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;

@SuppressWarnings("serial")
public abstract class SubscriptionStatusLink extends AjaxLink<Void> {

	public SubscriptionStatusLink(String id) {
		super(id);
	}

	@Override
	public void onClick(AjaxRequestTarget target) {
		onSubscriptionStatusChange(target, !isSubscribed());
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		String classes = tag.getAttribute("class");
		if (classes == null)
			classes = "";
		if (isSubscribed()) {
			tag.put("class", classes + " subscription-status subscribed");
			tag.put("title", "Subscribed. Click to unsubscribe");
		} else {
			tag.put("class", classes + " subscription-status unsubscribed");
			tag.put("title", "Unsubscribed. Click to subscribe");
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SubscriptionStatusResourceReference()));
	}

	protected abstract boolean isSubscribed();
	
	protected abstract void onSubscriptionStatusChange(AjaxRequestTarget target, boolean subscribed);
	
}
