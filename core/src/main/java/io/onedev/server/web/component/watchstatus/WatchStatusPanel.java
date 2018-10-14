package io.onedev.server.web.component.watchstatus;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.util.watch.WatchStatus;

@SuppressWarnings("serial")
abstract class WatchStatusPanel extends Panel {

	public WatchStatusPanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("default") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onWatchStatusChange(target, WatchStatus.DEFAULT);
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (getWatchStatus() == WatchStatus.DEFAULT)
					add(AttributeAppender.append("class", "active"));
			}
			
		});
		add(new AjaxLink<Void>("watch") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onWatchStatusChange(target, WatchStatus.WATCH);
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (getWatchStatus() == WatchStatus.WATCH)
					add(AttributeAppender.append("class", "active"));
			}

		});
		add(new AjaxLink<Void>("doNotWatch") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onWatchStatusChange(target, WatchStatus.DO_NOT_WATCH);
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (getWatchStatus() == WatchStatus.DO_NOT_WATCH)
					add(AttributeAppender.append("class", "active"));
			}
			
		});		
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new WatchStatusResourceReference()));
	}

	protected abstract WatchStatus getWatchStatus();
	
	protected abstract void onWatchStatusChange(AjaxRequestTarget target, WatchStatus watchStatus);
	
}