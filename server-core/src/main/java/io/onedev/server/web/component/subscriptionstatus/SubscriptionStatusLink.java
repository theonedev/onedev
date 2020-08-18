package io.onedev.server.web.component.subscriptionstatus;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.web.component.svg.SpriteImage;

@SuppressWarnings("serial")
public abstract class SubscriptionStatusLink extends AjaxLink<Void> {

	public SubscriptionStatusLink(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setEscapeModelStrings(false);
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
	public IModel<?> getBody() {
		return Model.of(String.format(""
				+ "<svg class='icon icon-bell-ring'><use xlink:href='%s'/></svg>"
				+ "<svg class='icon icon-bell-off'><use xlink:href='%s'/></svg>", 
				SpriteImage.getVersionedHref("bell-ring"), 
				SpriteImage.getVersionedHref("bell-off")));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SubscriptionStatusResourceReference()));
	}

	protected abstract boolean isSubscribed();
	
	protected abstract void onSubscriptionStatusChange(AjaxRequestTarget target, boolean subscribed);
	
}
