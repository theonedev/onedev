package io.onedev.server.web.page.help;

import static io.onedev.server.web.translation.Translation._T;

import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.WordUtils;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.web.page.layout.LayoutPage;

public abstract class ApiHelpPage extends LayoutPage {

	public ApiHelpPage(PageParameters params) {
		super(params);
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
		var api = resourceClass.getAnnotation(Api.class);
		if (api != null && api.name().length() != 0) {
			return api.name();
		} else {
			return WordUtils.capitalize(
					WordUtils.uncamel(
							StringUtils.substringBeforeLast(
									resourceClass.getSimpleName(), "Resource")));
		}
	}
	
	@Nullable
	protected String getResourceDescription(Class<?> resourceClass) {
		String description = "";
		Api api = resourceClass.getAnnotation(Api.class);
		if (api != null && api.description().length() != 0) 
			description = api.description();
		if (description.length() != 0)
			return description;
		else
			return null;
	}
	
	protected String getMethodTitle(Method resourceMethod) {
		var api = resourceMethod.getAnnotation(Api.class);
		if (api != null && api.name().length() != 0) {
			return api.name();
		} else {
			return WordUtils.capitalize(WordUtils.uncamel(resourceMethod.getName()));
		}
	}

	@Nullable
	protected String getMethodDescription(Method resourceMethod) {
		String description = "";
		Api api = resourceMethod.getAnnotation(Api.class);
		if (api != null && api.description().length() != 0) 
			description = api.description();
		if (description.length() != 0)
			return description;
		else
			return null;
	}
	
	protected String getHttpMethod(Method resourceMethod) {
		if (resourceMethod.getAnnotation(GET.class) != null)
			return "GET";
		else if (resourceMethod.getAnnotation(POST.class) != null)
			return "POST";
		else if (resourceMethod.getAnnotation(PUT.class) != null)
			return "PUT";
		else
			return "DELETE";
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ApiHelpCssResourceReference()));
	}
	
}
