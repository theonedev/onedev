package io.onedev.server.web.component.watchstatus;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
abstract class WatchStatusPanel extends Panel {

	public WatchStatusPanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("watching") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onWatchStatusChange(target, WatchStatus.WATCHING);
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (getWatchStatus() == WatchStatus.WATCHING)
					add(AttributeAppender.append("class", "active"));
			}

		});
		add(new AjaxLink<Void>("notWatching") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onWatchStatusChange(target, WatchStatus.NOT_WATCHING);
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (getWatchStatus() == WatchStatus.NOT_WATCHING)
					add(AttributeAppender.append("class", "active"));
			}
			
		});
		add(new AjaxLink<Void>("ignore") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onWatchStatusChange(target, WatchStatus.IGNORE);
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (getWatchStatus() == WatchStatus.IGNORE)
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