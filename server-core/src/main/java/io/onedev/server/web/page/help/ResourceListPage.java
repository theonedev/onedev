package io.onedev.server.web.page.help;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Path;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.glassfish.jersey.server.ResourceConfig;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;

@SuppressWarnings("serial")
public class ResourceListPage extends ApiHelpPage {

	public ResourceListPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<Class<?>>("resources", new LoadableDetachableModel<List<Class<?>>>() {

			@Override
			protected List<Class<?>> load() {
				List<Class<?>> clazzes = new ArrayList<>();
				ResourceConfig config = OneDev.getInstance(ResourceConfig.class);
				for (Class<?> clazz: config.getClasses()) {
					if (clazz.getAnnotation(Path.class) != null) { 
						Api api = clazz.getAnnotation(Api.class);
						if (api == null || !api.internal())
							clazzes.add(clazz);
					}
				}
				Collections.sort(clazzes, new ApiComparator());
				return clazzes;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Class<?>> item) {
				Class<?> clazz = item.getModelObject();
				
				Link<Void> link = new ViewStateAwarePageLink<Void>("link", ResourceDetailPage.class, 
						ResourceDetailPage.paramsOf(clazz));				
				link.add(new Label("label", getResourceTitle(clazz)));
						
				item.add(link);
				
				item.add(new Label("path", clazz.getAnnotation(Path.class).value()));
			}
			
		});
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Resources");
	}

}
