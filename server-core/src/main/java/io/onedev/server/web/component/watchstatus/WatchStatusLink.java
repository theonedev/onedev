package io.onedev.server.web.component.watchstatus;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import io.onedev.server.util.watch.WatchStatus;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.svg.SpriteImage;

@SuppressWarnings("serial")
public abstract class WatchStatusLink extends DropdownLink {

	public WatchStatusLink(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setEscapeModelStrings(false);
		
		add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				WatchStatus status = getWatchStatus();
				if (status == WatchStatus.DO_NOT_WATCH)
					return "do-not-watch";
				else if (status == WatchStatus.WATCH)
					return "watch";
				else
					return "default";
			}
			
		}));
	}

	@Override
	public IModel<?> getBody() {
		return Model.of(String.format(""
				+ "<svg class='icon icon-bell-ring'><use xlink:href='%s'/></svg>"
				+ "<svg class='icon icon-bell'><use xlink:href='%s'/></svg>"
				+ "<svg class='icon icon-bell-off'><use xlink:href='%s'/></svg>", 
				SpriteImage.getVersionedHref("bell-ring"), 
				SpriteImage.getVersionedHref("bell"), 
				SpriteImage.getVersionedHref("bell-off")));
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
