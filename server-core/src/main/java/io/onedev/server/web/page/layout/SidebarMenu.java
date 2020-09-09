package io.onedev.server.web.page.layout;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

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
