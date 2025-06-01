package io.onedev.server.web.component.watchstatus;

import io.onedev.server.util.watch.WatchStatus;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.svg.SpriteImage;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

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
				if (status == WatchStatus.IGNORE)
					return "ignore";
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
				+ "<span class='default'><svg class='icon'><use xlink:href='%s'/></svg> <span class='text'>" + _T("Watch if involved") + "</span></span>"
				+ "<span class='watch'><svg class='icon'><use xlink:href='%s'/></svg> <span class='text'>" + _T("Watch") + "</span></span>"
				+ "<span class='ignore'><svg class='icon'><use xlink:href='%s'/></svg> <span class='text'>" + _T("Ignore") + "</span></span>", 
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
