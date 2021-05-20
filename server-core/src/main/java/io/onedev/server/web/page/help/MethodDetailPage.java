package io.onedev.server.web.page.help;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.WordUtils;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.jersey.ParamCheckFilter;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.util.TextUtils;

@SuppressWarnings("serial")
public class MethodDetailPage extends ApiHelpPage {

	private static final String PARAM_RESOURCE = "resource";
	
	private static final String PARAM_METHOD = "method";
	
	private final Class<?> resourceClass;
	
	private final String methodName;
	
	private final IModel<Method> methodModel = new LoadableDetachableModel<Method>() {

		@Override
		protected Method load() {
			for (Method method: resourceClass.getMethods()) {
				if (method.getName().equals(methodName))
					return method;
			}
			String errorMessage = String.format("Unable to find resource method (resource: %s, method: %s)", 
					resourceClass.getName(), methodName);
			throw new ExplicitException(errorMessage);
		}
		
	};
	
	public MethodDetailPage(PageParameters params) {
		super(params);

		try {
			resourceClass = Class.forName(params.get(PARAM_RESOURCE).toString());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		methodName = params.get(PARAM_METHOD).toString();
	}

	@Override
	protected void onDetach() {
		methodModel.detach();
		super.onDetach();
	}
	
	private Method getResourceMethod() {
		return methodModel.getObject();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Method method = getResourceMethod();
		add(new Label("title", getMethodDescription(method)));
		
		if (method.getAnnotation(GET.class) != null)
			add(new Label("method", "GET"));
		else if (method.getAnnotation(POST.class) != null)
			add(new Label("method", "POST"));
		else if (method.getAnnotation(PUT.class) != null)
			add(new Label("method", "PUT"));
		else
			add(new Label("method", "DELETE"));
		
		String resourcePathValue = resourceClass.getAnnotation(Path.class).value();
		Path methodPath = method.getAnnotation(Path.class);
		if (methodPath != null)
			add(new Label("path", "/api" + resourcePathValue + methodPath.value()));
		else
			add(new Label("path", "/api" + resourcePathValue));
		
		add(new ListView<Parameter>("pathPlaceholders", new LoadableDetachableModel<List<Parameter>>() {

			@Override
			protected List<Parameter> load() {
				List<Parameter> params = new ArrayList<>();
				for (Parameter param: getResourceMethod().getParameters()) {
					if (param.getAnnotation(PathParam.class) != null)
						params.add(param);
				}
				return params;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Parameter> item) {
				Parameter param = item.getModelObject();
				PathParam pathParam = param.getAnnotation(PathParam.class);
				item.add(new Label("name", "{" + pathParam.value() + "}"));
				Api api = param.getAnnotation(Api.class);
				if (api != null && api.description().length() != 0)
					item.add(new Label("description", api.description()));
				else
					item.add(new Label("description", StringUtils.capitalize(WordUtils.uncamel(pathParam.value()))));
				
				Object exampleValue = new ExampleProvider(getResourceMethod().getDeclaringClass(), api).getExample(); 
				if (exampleValue == null)
					exampleValue = ApiHelpUtils.getExampleValue(param.getParameterizedType());
				item.add(new ExampleValuePanel("example", exampleValue, false));
			}
			
		});
		
		add(new ListView<Parameter>("queryParams", new LoadableDetachableModel<List<Parameter>>() {

			@Override
			protected List<Parameter> load() {
				List<Parameter> params = new ArrayList<>();
				for (Parameter param: getResourceMethod().getParameters()) {
					if (param.getAnnotation(QueryParam.class) != null)
						params.add(param);
				}
				return params;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Parameter> item) {
				Parameter param = item.getModelObject();
				QueryParam queryParam = param.getAnnotation(QueryParam.class);
				item.add(new Label("name", queryParam.value()));
				Api api = param.getAnnotation(Api.class);
				if (api != null && api.description().length() != 0)
					item.add(new Label("description", api.description()));
				else
					item.add(new Label("description", StringUtils.capitalize(WordUtils.uncamel(queryParam.value()))));
				item.add(new Label("required", TextUtils.describe(ParamCheckFilter.isRequired(param))));

				Object exampleValue = new ExampleProvider(getResourceMethod().getDeclaringClass(), api).getExample();
				if (exampleValue == null)
					exampleValue = ApiHelpUtils.getExampleValue(param.getParameterizedType());
				item.add(new ExampleValuePanel("example", exampleValue, false));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getModelObject().isEmpty());
			}
			
		});
		
		Parameter requestBodyParam = null;
		for (Parameter param: method.getParameters()) {
			if (param.getAnnotation(PathParam.class) == null && param.getAnnotation(QueryParam.class) == null) {
				requestBodyParam = param;
				break;
			}
		}
		
		if (requestBodyParam != null) { 
			Object exampleValue = new ExampleProvider(resourceClass, requestBodyParam.getAnnotation(Api.class)).getExample(); 
			if (exampleValue == null) {
				exampleValue = ApiHelpUtils.getExampleValue(requestBodyParam.getParameterizedType());
			}
			add(new ExampleValuePanel("requestBodyExample", exampleValue, true));
		} else { 
			add(new WebMarkupContainer("requestBodyExample").setVisible(false));
		}
		
		Api api = method.getAnnotation(Api.class);
		if (api != null && api.permission().length() != 0) 
			add(new Label("permission", api.permission()));
		else 
			add(new Label("permission", "Anyone can perform this operation"));
		
		if (method.getReturnType() == Response.class) {
			add(new Label("successResponseBody", "No response body if successful; error"));
		} else {
			Fragment fragment = new Fragment("successResponseBody", "hasResponseBodyFrag", MethodDetailPage.this);
			Object exampleValue = new ExampleProvider(resourceClass, method.getAnnotation(Api.class)).getExample();
			if (exampleValue == null) 
				exampleValue = ApiHelpUtils.getExampleValue(method.getGenericReturnType());
			fragment.add(new ExampleValuePanel("example", exampleValue, false));
			add(fragment);
		}

	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "topbarTitleFrag", this);
		fragment.add(new ViewStateAwarePageLink<Void>("resources", ResourceListPage.class));
		Link<Void> resourceLink = new ViewStateAwarePageLink<Void>("resource", ResourceDetailPage.class, 
				ResourceDetailPage.paramsOf(resourceClass));
		resourceLink.add(new Label("label", getResourceTitle(resourceClass)));
		fragment.add(resourceLink);
		fragment.add(new Label("method", getMethodTitle(getResourceMethod())));
		return fragment;
	}

	public static PageParameters paramsOf(Class<?> resourceClass, String resourceMethod) {
		PageParameters params = new PageParameters();
		params.add(PARAM_RESOURCE, resourceClass.getName());
		params.add(PARAM_METHOD, resourceMethod);
		return params;
	}
	
}
