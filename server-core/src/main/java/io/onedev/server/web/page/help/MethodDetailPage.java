package io.onedev.server.web.page.help;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.WordUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.rest.TriggerJobResource;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.jersey.ParamCheckFilter;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.util.TextUtils;
import org.apache.wicket.Component;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.annotation.Nullable;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

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

	@Nullable
	private Parameter getRequestBodyParam() {
		for (Parameter param: getResourceMethod().getParameters()) {
			if (param.getAnnotation(PathParam.class) == null 
					&& param.getAnnotation(QueryParam.class) == null 
					&& param.getAnnotation(Context.class) == null) {
				return param;
			}
		}
		return null;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Method method = getResourceMethod();
		
		add(new Label("title", getMethodTitle(getResourceMethod())));
		
		String description = getMethodDescription(method);
		add(new Label("description", description).setEscapeModelStrings(false).setVisible(description!=null));

		String httpMethod = getHttpMethod(method);
		add(new Label("method", httpMethod));
		
		Class<?> requestBodyClass;
		
		Parameter param = getRequestBodyParam();
		
		if (param != null) {
			if (InputStream.class.isAssignableFrom(param.getType())) {
				add(new Label("contentType", MediaType.APPLICATION_OCTET_STREAM));
				add(new WebMarkupContainer("requestBodyExample").setVisible(false));
				add(new WebMarkupContainer("copyRequestBodyExample").setVisible(false));
				requestBodyClass = InputStream.class;
			} else {
				add(new Label("contentType", MediaType.APPLICATION_JSON));
				Serializable exampleValue = new ExampleProvider(resourceClass, param.getAnnotation(Api.class)).getExample();
				if (exampleValue == null)
					exampleValue = ApiHelpUtils.getExampleValue(param.getParameterizedType(), ValueInfo.Origin.REQUEST_BODY);
				requestBodyClass = exampleValue.getClass();
				IModel<ValueInfo> valueInfoModel = new LoadableDetachableModel<>() {

					@Override
					protected ValueInfo load() {
						Api api = getResourceMethod().getAnnotation(Api.class);
						return new ValueInfo(ValueInfo.Origin.REQUEST_BODY,
								getRequestBodyParam().getParameterizedType(), null);
					}

				};

				Model<Serializable> valueModel = Model.of(exampleValue);

				ExampleValuePanel valuePanel = new ExampleValuePanel("requestBodyExample", valueModel,
						valueInfoModel, requestBodyClass);
				add(valuePanel);

				add(new CopyToClipboardLink("copyRequestBodyExample", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						return valuePanel.getValueAsJson();
					}

				}) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!valuePanel.isScalarValue());
					}

					@Override
					public void onEvent(IEvent<?> event) {
						super.onEvent(event);
						if (event.getPayload() instanceof ExampleValueChanged)
							((ExampleValueChanged)event.getPayload()).getHandler().add(this);
					}

				}.setOutputMarkupId(true));					
			}
		} else { 
			requestBodyClass = null;
			add(new WebMarkupContainer("contentType").setVisible(false));
			add(new WebMarkupContainer("requestBodyExample").setVisible(false));
			add(new WebMarkupContainer("copyRequestBodyExample").setVisible(false));
		}
		
		String resourcePathValue = resourceClass.getAnnotation(Path.class).value();
		Path methodPath = method.getAnnotation(Path.class);
		
		String endPoint;
		if (methodPath != null)
			endPoint = "/~api" + resourcePathValue + methodPath.value();
		else
			endPoint = "/~api" + resourcePathValue;
		
		add(new Label("path", endPoint));
		
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
				
				Serializable exampleValue = new ExampleProvider(getResourceMethod().getDeclaringClass(), api).getExample(); 
				if (exampleValue == null)
					exampleValue = ApiHelpUtils.getExampleValue(param.getParameterizedType(), ValueInfo.Origin.PATH_PLACEHOLDER);
				
				IModel<ValueInfo> valueInfoModel = new LoadableDetachableModel<ValueInfo>() {

					@Override
					protected ValueInfo load() {
						return new ValueInfo(ValueInfo.Origin.PATH_PLACEHOLDER, 
								item.getModelObject().getParameterizedType());
					}
					
				};
				
				item.add(new ExampleValuePanel("example", Model.of(exampleValue), valueInfoModel, requestBodyClass));
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getModelObject().isEmpty());
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
					item.add(new Label("description", api.description()).setEscapeModelStrings(false));
				else
					item.add(new Label("description", StringUtils.capitalize(WordUtils.uncamel(queryParam.value()).toLowerCase())));
				item.add(new Label("required", TextUtils.getDisplayValue(ParamCheckFilter.isRequired(param))));

				Serializable exampleValue = new ExampleProvider(getResourceMethod().getDeclaringClass(), api).getExample();
				if (exampleValue == null)
					exampleValue = ApiHelpUtils.getExampleValue(param.getParameterizedType(), ValueInfo.Origin.QUERY_PARAM);
				
				IModel<ValueInfo> valueInfoModel = new LoadableDetachableModel<ValueInfo>() {

					@Override
					protected ValueInfo load() {
						return new ValueInfo(ValueInfo.Origin.QUERY_PARAM, 
								item.getModelObject().getParameterizedType());
					}
					
				};
				
				item.add(new ExampleValuePanel("example", Model.of(exampleValue), valueInfoModel, requestBodyClass));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getModelObject().isEmpty());
			}
			
		});
		
		if (method.getReturnType() == Response.class) {
			add(new Label("successResponseBody", "No response body"));
		} else {
			Fragment fragment = new Fragment("successResponseBody", "hasResponseBodyFrag", MethodDetailPage.this);
			
			if (StreamingOutput.class.isAssignableFrom(method.getReturnType())) {
				fragment.add(new Label("contentType", MediaType.APPLICATION_OCTET_STREAM));
				fragment.add(new WebMarkupContainer("example").setVisible(false));
				fragment.add(new WebMarkupContainer("copyExample").setVisible(false));
			} else {
				fragment.add(new Label("contentType", MediaType.APPLICATION_JSON));
				
				Serializable exampleValue = new ExampleProvider(resourceClass, method.getAnnotation(Api.class)).getExample();
				if (exampleValue == null) 
					exampleValue = ApiHelpUtils.getExampleValue(method.getGenericReturnType(), ValueInfo.Origin.RESPONSE_BODY);
			
				IModel<ValueInfo> valueInfoModel = new LoadableDetachableModel<>() {

					@Override
					protected ValueInfo load() {
						return new ValueInfo(ValueInfo.Origin.RESPONSE_BODY,
								getResourceMethod().getGenericReturnType());
					}

				};

				IModel<Serializable> valueModel = Model.of(exampleValue);

				ExampleValuePanel valuePanel = new ExampleValuePanel("example", valueModel, valueInfoModel, requestBodyClass);
				fragment.add(valuePanel);

				fragment.add(new CopyToClipboardLink("copyExample", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						return valuePanel.getValueAsJson();
					}

				}) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!valuePanel.isScalarValue());
					}

					@Override
					public void onEvent(IEvent<?> event) {
						super.onEvent(event);
						if (event.getPayload() instanceof ExampleValueChanged)
							((ExampleValueChanged)event.getPayload()).getHandler().add(this);
					}

				}.setOutputMarkupPlaceholderTag(true));
			}

			add(fragment);
		}

		for (Parameter pathParam: getResourceMethod().getParameters()) {
			if (pathParam.getAnnotation(PathParam.class) != null) {
				Api api = pathParam.getAnnotation(Api.class);
				Serializable exampleValue = new ExampleProvider(getResourceMethod().getDeclaringClass(), api).getExample(); 
				if (exampleValue == null)
					exampleValue = ApiHelpUtils.getExampleValue(pathParam.getParameterizedType(), ValueInfo.Origin.PATH_PLACEHOLDER);
				endPoint = endPoint.replaceFirst(
						"\\{" + pathParam.getAnnotation(PathParam.class).value() + ".*?\\}",
						Matcher.quoteReplacement(String.valueOf(exampleValue)));
			}
		}
		
		Map<String, String> queryParams = new LinkedHashMap<>();
		
		for (Parameter queryParam: getResourceMethod().getParameters()) {
			if (queryParam.getAnnotation(QueryParam.class) != null) {
				String paramName = queryParam.getAnnotation(QueryParam.class).value();
				
				Api api = queryParam.getAnnotation(Api.class);
				Serializable exampleValue = new ExampleProvider(getResourceMethod().getDeclaringClass(), api).getExample();
				if (exampleValue == null)
					exampleValue = ApiHelpUtils.getExampleValue(queryParam.getParameterizedType(), ValueInfo.Origin.QUERY_PARAM);
				
				queryParams.put(paramName, String.valueOf(exampleValue));
			}
		}
		
		StringBuilder curlExample = new StringBuilder("$ curl ");

		if (resourceClass != TriggerJobResource.class)
			curlExample.append("-u <login name>:<password or access token> ");
		
		if (!queryParams.isEmpty())
			curlExample.append("-G ");
		if (StreamingOutput.class.isAssignableFrom(method.getReturnType()))
			curlExample.append("-O ");
		
		switch (httpMethod) {
		case "DELETE":
			curlExample.append("-X DELETE ");
			break;
		case "PUT":
		case "POST":
			if (resourceClass == TriggerJobResource.class) {
				curlExample.append(String.format("-X %s -H \"Content-Type: %s\" ",
						httpMethod, MediaType.APPLICATION_JSON));
			} else if (requestBodyClass == null) {
				curlExample.append(String.format("-X %s ", httpMethod));
			} else if (InputStream.class.isAssignableFrom(requestBodyClass)) {
				curlExample.append(String.format("-X %s --data-binary \"@upload-file\" -H \"Content-Type: %s\" ",
						httpMethod, MediaType.APPLICATION_OCTET_STREAM));
			} else {
				curlExample.append(String.format("-X %s -d@request-body.json -H \"Content-Type: %s\" ", 
						httpMethod, MediaType.APPLICATION_JSON));
			}				
			break;
		}
		
		curlExample.append(OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl()).append(endPoint);
		
		for (Map.Entry<String, String> entry: queryParams.entrySet()) {
			if (entry.getValue().contains(" "))
				curlExample.append(" --data-urlencode '").append(entry.getKey()).append("=").append(entry.getValue()).append("'");
			else
				curlExample.append(" --data-urlencode ").append(entry.getKey()).append("=").append(entry.getValue());
		}
		
		add(new Label("curlExample", curlExample));
		
		add(new CopyToClipboardLink("copyCurlExample", Model.of(curlExample.substring(1))));
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
