package io.onedev.server.web.component.menu;

import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;

import org.jspecify.annotations.Nullable;
import java.util.List;

public abstract class MenuLink extends DropdownLink {

	public MenuLink(String id) {
		super(id);
	}
	
	public MenuLink(String id, AlignPlacement placement) {
		super(id, placement);
	}

	@Override
	protected void onInitialize(FloatingPanel dropdown) {
		super.onInitialize(dropdown);
		dropdown.add(AttributeAppender.append("class", " menu"));
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new MenuPanel(id) {

			@Override
			protected List<MenuItem> getMenuItems() {
				return MenuLink.this.getMenuItems(dropdown);
			}

			@Override
			protected String getHelp() {
				return MenuLink.this.getHelp();
			}
			
		};
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MenuCssResourceReference()));
	}

	protected abstract List<MenuItem> getMenuItems(FloatingPanel dropdown);
	
	@Nullable
	protected String getHelp() {
		return null;
	}
	
}
