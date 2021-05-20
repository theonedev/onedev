package io.onedev.server.web.page.help;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.WordUtils;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.web.page.layout.LayoutPage;

@SuppressWarnings("serial")
public abstract class ApiHelpPage extends LayoutPage {

	public ApiHelpPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return "RESTful API Help";
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "RESTful API Help");
	}
	
	protected String getResourceTitle(Class<?> resourceClass) {
		return StringUtils.capitalize(
				WordUtils.uncamel(
						StringUtils.substringBeforeLast(
								resourceClass.getSimpleName(), "Resource")).toLowerCase());
	}
	
	protected String getResourceDescription(Class<?> resourceClass) {
		Api api = resourceClass.getAnnotation(Api.class);
		if (api != null && api.description().length() != 0) 
			return api.description();
		else
			return getResourceTitle(resourceClass);
	}
	
	protected String getMethodTitle(Method resourceMethod) {
		return StringUtils.capitalize(WordUtils.uncamel(resourceMethod.getName()).toLowerCase());
	}
	
	protected String getMethodDescription(Method resourceMethod) {
		Api api = resourceMethod.getAnnotation(Api.class);
		if (api != null && api.description().length() != 0) 
			return api.description();
		else
			return getMethodTitle(resourceMethod);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ApiHelpCssResourceReference()));
	}
	
}
