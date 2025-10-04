package io.onedev.server.web.page.admin;

import io.onedev.server.OneDev;
import io.onedev.server.service.SettingService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.layout.LayoutPage;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class AdministrationPage extends LayoutPage {

	public AdministrationPage(PageParameters params) {
		super(params);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new AdministrationCssResourceReference()));
	}

	@Override
	protected String getPageTitle() {
		return "Administration - " + OneDev.getInstance(SettingService.class).getBrandingSetting().getName();
	}
	
}
