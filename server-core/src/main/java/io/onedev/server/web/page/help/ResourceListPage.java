package io.onedev.server.web.page.help;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Comparator;
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

import io.onedev.server.OneDev;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.util.Pair;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;

public class ResourceListPage extends ApiHelpPage {

	public ResourceListPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<Pair<Class<?>, String>>("resources", new LoadableDetachableModel<List<Pair<Class<?>, String>>>() {

			@Override
			protected List<Pair<Class<?>, String>> load() {
				List<Pair<Class<?>, String>> pairs = new ArrayList<>();
				ResourceConfig config = OneDev.getInstance(ResourceConfig.class);
				for (Class<?> clazz: config.getClasses()) {
					if (clazz.getAnnotation(Path.class) != null) { 
						Api api = clazz.getAnnotation(Api.class);
						if (api == null || !api.internal())
							pairs.add(new Pair<>(clazz, getResourceTitle(clazz)));
					}
				}				
				pairs.sort(new Comparator<>() {

					@Override
					public int compare(Pair<Class<?>, String> o1, Pair<Class<?>, String> o2) {
						return o1.getRight().compareTo(o2.getRight());
					}

				});
				return pairs;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Pair<Class<?>, String>> item) {
				Class<?> clazz = item.getModelObject().getLeft();
				
				Link<Void> link = new ViewStateAwarePageLink<Void>("link", ResourceDetailPage.class, 
						ResourceDetailPage.paramsOf(clazz));				
				link.add(new Label("label", item.getModelObject().getRight()));
						
				item.add(link);
				
				item.add(new Label("path", clazz.getAnnotation(Path.class).value()));
			}
			
		});
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Resources"));
	}

}
