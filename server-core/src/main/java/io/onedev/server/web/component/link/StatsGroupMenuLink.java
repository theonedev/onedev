package io.onedev.server.web.component.link;

import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.util.StatsGroup;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;

import java.util.ArrayList;
import java.util.List;

public abstract class StatsGroupMenuLink extends MenuLink {

	public StatsGroupMenuLink(String id) {
		super(id);
	}

	@Override
	protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
		var menuItems = new ArrayList<MenuItem>();
		for (var group: StatsGroup.values()) {
			menuItems.add(new MenuItem() {
				@Override
				public String getLabel() {
					return StringUtils.capitalize(group.name().replace("_", " ").toLowerCase());
				}

				@Override
				public boolean isSelected() {
					return group == getSelectedGroup();
				}

				@Override
				public WebMarkupContainer newLink(String id) {
					return new AjaxLink<Void>(id) {

						@Override
						public void onClick(AjaxRequestTarget target) {
							onSelect(target, group);
							dropdown.close();
						}
					};
				}
			});
		}
		return menuItems;
	}
	
	protected abstract StatsGroup getSelectedGroup();
	
	protected abstract void onSelect(AjaxRequestTarget target, StatsGroup group);
	
}
