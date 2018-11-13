package io.onedev.server.web.page.my;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;

import io.onedev.server.web.component.sidebar.SideBar;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.page.security.LoginPage;

@SuppressWarnings("serial")
public abstract class MyPage extends LayoutPage {
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getLoginUser() == null) 
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
		
		add(new SideBar("mySidebar", "my.miniSidebar") {

			@Override
			protected List<? extends Tab> newTabs() {
				List<PageTab> tabs = new ArrayList<>();
				
				tabs.add(new MyTab("Profile", "fa fa-fw fa-list-alt", MyProfilePage.class));
				tabs.add(new MyTab("Edit Avatar", "fa fa-fw fa-picture-o", MyAvatarPage.class));
				tabs.add(new MyTab("Change Password", "fa fa-fw fa-key", MyPasswordPage.class));
				tabs.add(new MyTab("Access Token", "fa fa-fw fa-ticket", MyTokenPage.class));
				
				return tabs;
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MyCssResourceReference()));
	}
	
}
