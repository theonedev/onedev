package io.onedev.server.web.page.layout;

import java.io.Serializable;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.web.component.floating.FloatingPanel;

public class SidebarMenu implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Header menuHeader;
	
	private final List<SidebarMenuItem> menuItems;
	
	public SidebarMenu(@Nullable Header menuHeader, List<SidebarMenuItem> menuItems) {
		this.menuHeader = menuHeader;
		this.menuItems = menuItems;
	}

	@Nullable
	public Header getMenuHeader() {
		return menuHeader;
	}

	public List<SidebarMenuItem> getMenuItems() {
		return menuItems;
	}
	
	@Nullable
	protected Component newMenuHeader() {
		return null;
	}

	public void insertMenuItem(SidebarMenuItem menuItem) {
		insertMenuItem(menuItems, menuItem);
	}

	private void insertMenuItem(List<SidebarMenuItem> menuItems, SidebarMenuItem menuItem) {
		if (menuItem instanceof SidebarMenuItem.SubMenu) {
			var subMenu = (SidebarMenuItem.SubMenu) menuItem;
			for (var existingMenuItem: menuItems) {
				if (existingMenuItem instanceof SidebarMenuItem.SubMenu) {
					var existingSubMenu = (SidebarMenuItem.SubMenu) existingMenuItem;
					if (existingSubMenu.getLabel().equals(subMenu.getLabel())) {
						for (var childMenuItem: subMenu.getMenuItems()) 
							insertMenuItem(existingSubMenu.getMenuItems(), childMenuItem);
						return;
					}
				}
			}
		} 
		menuItems.add(menuItem);
	}
	
	public void cleanup() {
		for (var it = menuItems.iterator(); it.hasNext();) {
			var menuItem = it.next();
			if (menuItem instanceof SidebarMenuItem.SubMenu) {
				var subMenu = (SidebarMenuItem.SubMenu) menuItem;
				subMenu.cleanup();
				if (subMenu.getMenuItems().isEmpty())
					it.remove();
			}
		}
	}
	
	public static abstract class Header implements Serializable {
		
		private static final long serialVersionUID = 1L;

		private final String imageUrl;
		
		private final String label;
		
		public Header(String imageUrl, String label) {
			this.imageUrl = imageUrl;
			this.label = label;
		}

		public String getImageUrl() {
			return imageUrl;
		}

		public String getLabel() {
			return label;
		}

		protected abstract Component newMoreInfo(String componentId, FloatingPanel dropdown);
		
	}
	
}
