package io.onedev.server.web.page.admin;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.scriptidentity.ScriptIdentity;
import io.onedev.server.util.scriptidentity.ScriptIdentityAware;
import io.onedev.server.util.scriptidentity.SiteAdministrator;
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
	public ScriptIdentity getScriptIdentity() {
		return new SiteAdministrator();
	}

}
