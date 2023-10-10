package io.onedev.server.web.page.layout;

import com.google.common.collect.Lists;
import io.onedev.server.web.page.admin.pluginsettings.ContributedAdministrationSettingPage;
import io.onedev.server.web.page.project.setting.pluginsettings.ContributedProjectSettingPage;
import io.onedev.server.web.util.WicketUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

public abstract class SidebarMenuItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String iconHref;
	
	private final String label;
	
	public SidebarMenuItem(@Nullable String iconHref, String label) {
		this.iconHref = iconHref;
		this.label = label;
	}

	@Nullable
	public String getIconHref() {
		return iconHref;
	}
	
	public String getLabel() {
		return label;
	}

	public abstract boolean isActive();
	
	public static class Page extends SidebarMenuItem {

		private static final long serialVersionUID = 1L;

		private final Class<? extends org.apache.wicket.Page> pageClass;
		
		private final PageParameters pageParams;
		
		private final List<Class<? extends org.apache.wicket.Page>> additionalPageClasses;
		
		public Page(String icon, String label, Class<? extends org.apache.wicket.Page> pageClass, PageParameters pageParams) {
			this(icon, label, pageClass, pageParams, Lists.newArrayList());
		}
		
		public Page(String icon, String label, Class<? extends org.apache.wicket.Page> pageClass, PageParameters pageParams, 
				List<Class<? extends org.apache.wicket.Page>> additionalPageClasses) {
			super(icon, label);
			this.pageClass = pageClass;
			this.pageParams = pageParams;
			this.additionalPageClasses = additionalPageClasses;
		}
		
		public Class<? extends org.apache.wicket.Page> getPageClass() {
			return pageClass;
		}
		
		public PageParameters getPageParams() {
			return pageParams;
		}
		
		public List<Class<? extends org.apache.wicket.Page>> getAdditionalPageClasses() {
			return additionalPageClasses;
		}
		
		@Override
		public boolean isActive() {
			org.apache.wicket.Page currentPage = WicketUtils.getPage();
			if (pageClass.isAssignableFrom(currentPage.getClass())) { 
				if (pageClass.isAssignableFrom(ContributedAdministrationSettingPage.class) 
						|| pageClass.isAssignableFrom(ContributedProjectSettingPage.class)) { 
					return currentPage.getPageParameters().equals(pageParams);
				} else { 
					return true;
				}
			}
			
			for (Class<?> pageClass: additionalPageClasses) {
				if (pageClass.isAssignableFrom(currentPage.getClass())) 
					return true;
			}
			return false;
		}

	}
	
	public static class SubMenu extends SidebarMenuItem {

		private static final long serialVersionUID = 1L;

		private final List<SidebarMenuItem> menuItems;
		
		public SubMenu(String icon, String label, List<SidebarMenuItem> menuItems) {
			super(icon, label);
			this.menuItems = menuItems;
		}

		public List<SidebarMenuItem> getMenuItems() {
			return menuItems;
		}

		@Override
		public boolean isActive() {
			return menuItems.stream().anyMatch(it->it.isActive());
		}
		
	}

}
