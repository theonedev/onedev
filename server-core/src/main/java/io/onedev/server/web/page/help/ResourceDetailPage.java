package io.onedev.server.web.page.help;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;

public class ResourceDetailPage extends ApiHelpPage {

	private static final String PARAM_RESOURCE = "resource";
	
	private final Class<?> resourceClass;
	
	public ResourceDetailPage(PageParameters params) {
		super(params);
		try {
			resourceClass = Class.forName(params.get(PARAM_RESOURCE).toString());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("title", getResourceTitle(resourceClass)));
		
		String description = getResourceDescription(resourceClass);
		add(new Label("description", description).setEscapeModelStrings(false).setVisible(description!=null));
		
		add(new ListView<Method>("methods", new LoadableDetachableModel<List<Method>>() {

			@Override
			protected List<Method> load() {
				List<Method> methods = new ArrayList<>();
				
				for (Method method: resourceClass.getMethods()) {
					if (method.getAnnotation(GET.class) != null 
							|| method.getAnnotation(POST.class) != null 
							|| method.getAnnotation(DELETE.class) != null 
							|| method.getAnnotation(PUT.class) != null) {
						methods.add(method);
					}
				}
				
				Collections.sort(methods, new ApiComparator());
				return methods;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Method> item) {
				Method method = item.getModelObject();
				
				Link<Void> link = new ViewStateAwarePageLink<Void>("link", MethodDetailPage.class, 
						MethodDetailPage.paramsOf(resourceClass, method.getName()));
				link.add(new Label("label", getMethodTitle(method)));
				
				item.add(link);
				
				item.add(new Label("httpMethod", getHttpMethod(method)));

				String resourcePathValue = resourceClass.getAnnotation(Path.class).value();
				Path methodPath = method.getAnnotation(Path.class);
				if (methodPath != null)
					item.add(new Label("path", resourcePathValue + methodPath.value()));
				else
					item.add(new Label("path", resourcePathValue));
			}
			
		});
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "topbarTitleFrag", this);
		fragment.add(new ViewStateAwarePageLink<Void>("resources", ResourceListPage.class));
		fragment.add(new Label("resource", getResourceTitle(resourceClass)));
		return fragment;
	}

	public static PageParameters paramsOf(Class<?> resourceClass) {
		PageParameters params = new PageParameters();
		params.add(PARAM_RESOURCE, resourceClass.getName());
		return params;
	}

}
