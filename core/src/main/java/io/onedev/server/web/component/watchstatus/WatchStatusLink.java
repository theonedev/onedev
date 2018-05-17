package io.onedev.server.web.component.watchstatus;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;

@SuppressWarnings("serial")
public abstract class WatchStatusLink extends DropdownLink {

	public WatchStatusLink(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				WatchStatus status = getWatchStatus();
				if (status == WatchStatus.IGNORE)
					return "ignore";
				else if (status == WatchStatus.WATCHING)
					return "watching";
				else
					return "not-watching";
			}
			
		}));
		
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new WatchStatusPanel(id) {

			@Override
			protected WatchStatus getWatchStatus() {
				return WatchStatusLink.this.getWatchStatus();
			}

			@Override
			protected void onWatchStatusChange(AjaxRequestTarget target, WatchStatus watchStatus) {
				WatchStatusLink.this.onWatchStatusChange(target, watchStatus);
				dropdown.close();
			}
			
		};
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new WatchStatusResourceReference()));
	}

	protected abstract WatchStatus getWatchStatus();
	
	protected abstract void onWatchStatusChange(AjaxRequestTarget target, WatchStatus watchStatus);
	
}
