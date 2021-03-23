package io.onedev.server.web.page.admin;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.script.identity.ScriptIdentity;
import io.onedev.server.util.script.identity.ScriptIdentityAware;
import io.onedev.server.util.script.identity.SiteAdministrator;
import io.onedev.server.web.page.layout.LayoutPage;

@SuppressWarnings("serial")
public abstract class AdministrationPage extends LayoutPage implements ScriptIdentityAware {

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
		response.render(CssHeaderItem.forReference(new AdministrationResourceReference()));
	}

	@Override
	protected String getPageTitle() {
		return "Administration - OneDev";
	}

	@Override
	public ScriptIdentity getScriptIdentity() {
		return new SiteAdministrator();
	}
	
	
}
