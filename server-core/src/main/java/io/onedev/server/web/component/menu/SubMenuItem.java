package io.onedev.server.web.component.menu;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.html.WebMarkupContainer;

import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.floating.FloatingPanel;

public abstract class SubMenuItem extends MenuItem {

	@Override
	public WebMarkupContainer newLink(String id) {
		return new MenuLink(id, new AlignPlacement(100, 0, 0, 0)) {

			private FloatingPanel submenu;

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setEventNames("mouseenter", "click");
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (submenu == null || submenu.getParent() == null)
					super.onClick(target);
			}

			@Override
			protected void onInitialize(FloatingPanel dropdown) {
				super.onInitialize(dropdown);
				submenu = dropdown;
				findParent(MenuPanel.class).onSubMenuOpened(dropdown);
			}

			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				return SubMenuItem.this.getMenuItems(dropdown);
			}

		};
	}

	protected abstract List<MenuItem> getMenuItems(FloatingPanel dropdown);

}
