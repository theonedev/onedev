package io.onedev.server.web.page.help;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.Path;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

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
				for (var clazz: ApiHelpUtils.getResourceClasses())
					pairs.add(new Pair<>(clazz, getResourceTitle(clazz)));
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
