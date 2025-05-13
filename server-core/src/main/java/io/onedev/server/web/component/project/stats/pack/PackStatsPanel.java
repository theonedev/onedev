package io.onedev.server.web.component.project.stats.pack;

import io.onedev.server.model.Project;
import io.onedev.server.search.entity.pack.PackQuery;
import io.onedev.server.search.entity.pack.PackQueryLexer;
import io.onedev.server.search.entity.pack.TypeCriteria;
import io.onedev.server.web.page.project.packs.ProjectPacksPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PackStatsPanel extends Panel {

	private final IModel<Project> projectModel;
	
	private final IModel<Map<String, Long>> statsModel;
	
	public PackStatsPanel(String id, IModel<Project> projectModel, IModel<Map<String, Long>> statsModel) {
		super(id);
		
		this.projectModel = projectModel;
		this.statsModel = statsModel;
	}
	
	private long getTotalCount() {
		return getStats().values().stream().mapToLong(Long::longValue).sum();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PageParameters params = ProjectPacksPage.paramsOf(getProject());
		Link<Void> packsLink = new BookmarkablePageLink<Void>("packs", ProjectPacksPage.class, params);
		packsLink.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return getTotalCount() + " " + _T("packages");
			}
			
		}));
		add(packsLink);
		
		add(new ListView<Map.Entry<String, Long>>("types",
				new LoadableDetachableModel<>() {

					@Override
					protected List<Map.Entry<String, Long>> load() {
						return new ArrayList<>(getStats().entrySet());
					}

				}) {

			@Override
			protected void populateItem(ListItem<Map.Entry<String, Long>> item) {
				Map.Entry<String, Long> entry = item.getModelObject();
				PackQuery query = new PackQuery(
						new TypeCriteria(entry.getKey(), PackQueryLexer.Is));
				PageParameters params = ProjectPacksPage.paramsOf(getProject(), query.toString(), 0);
				Link<Void> typeLink = new BookmarkablePageLink<Void>("link", ProjectPacksPage.class, params);
				String type = entry.getKey();
				typeLink.add(new Label("label", entry.getValue() + " " + _T(type + "(s)")));
				
				item.add(typeLink);
			}
			
		});
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(getTotalCount() != 0);
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		statsModel.detach();
		super.onDetach();
	}

	private Project getProject() {
		return projectModel.getObject();
	}
	
	private Map<String, Long> getStats() {
		return statsModel.getObject();
	}

}
