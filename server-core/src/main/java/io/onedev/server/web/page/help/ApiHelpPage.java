package io.onedev.server.web.page.help;

import static io.onedev.server.web.translation.Translation._T;

import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jspecify.annotations.Nullable;

import io.onedev.server.rest.ApiHelpJson;
import io.onedev.server.web.page.layout.LayoutPage;

public abstract class ApiHelpPage extends LayoutPage {

	public ApiHelpPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new ExternalLink("jsonHelp", ApiHelpJson.PATH, ApiHelpJson.PATH));
	}

	@Override
	protected String getPageTitle() {
		return _T("RESTful API Help");
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("RESTful API Help"));
	}
	
	protected String getResourceTitle(Class<?> resourceClass) {
		return ApiHelpUtils.getResourceTitle(resourceClass);
	}

	@Nullable
	protected String getResourceDescription(Class<?> resourceClass) {
		return ApiHelpUtils.getResourceDescription(resourceClass);
	}

	protected String getMethodTitle(Method resourceMethod) {
		return ApiHelpUtils.getMethodTitle(resourceMethod);
	}

	@Nullable
	protected String getMethodDescription(Method resourceMethod) {
		return ApiHelpUtils.getMethodDescription(resourceMethod);
	}

	protected String getHttpMethod(Method resourceMethod) {
		return ApiHelpUtils.getHttpMethod(resourceMethod);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ApiHelpCssResourceReference()));
	}
	
}
