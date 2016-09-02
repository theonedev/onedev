package com.pmease.commons.wicket.component.menu;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;

import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.floating.AlignPlacement;
import com.pmease.commons.wicket.component.floating.FloatingPanel;

@SuppressWarnings("serial")
public abstract class MenuLink extends DropdownLink {

	public MenuLink(String id) {
		super(id);
	}
	
	public MenuLink(String id, AlignPlacement placement) {
		super(id, placement);
	}

	public MenuLink(String id, boolean alignTargetMouse) {
		super(id, alignTargetMouse);
	}
	
	public MenuLink(String id, boolean alignTargetMouse, AlignPlacement placement) {
		super(id, alignTargetMouse, placement);
	}
	
	@Override
	protected void onInitialize(FloatingPanel dropdown) {
		super.onInitialize(dropdown);
		dropdown.add(AttributeAppender.append("class", " menu"));
	}

	@Override
	protected Component newContent(String id) {
		return new MenuPanel(id) {

			@Override
			protected List<MenuItem> getMenuItems() {
				return MenuLink.this.getMenuItems();
			}
			
		};
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MenuResourceReference()));
	}

	protected abstract List<MenuItem> getMenuItems();
}
