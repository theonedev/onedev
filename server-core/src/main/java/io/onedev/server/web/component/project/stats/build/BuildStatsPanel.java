package io.onedev.server.web.component.project.stats.build;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;

@SuppressWarnings("serial")
public class BuildStatsPanel extends Panel {

	private final IModel<Project> projectModel;
	
	private final IModel<Map<Status, Long>> statsModel;
	
	public BuildStatsPanel(String id, IModel<Project> projectModel, IModel<Map<Status, Long>> statsModel) {
		super(id);

		this.projectModel = projectModel;
		this.statsModel = statsModel;
	}
	
	private long getTotalCount() {
		return getStats().values().stream().collect(Collectors.summingLong(Long::longValue));
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PageParameters params = ProjectBuildsPage.paramsOf(getProject());
		Link<Void> pullRequestsLink = new BookmarkablePageLink<Void>("builds", ProjectBuildsPage.class, params);
		pullRequestsLink.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return getTotalCount() + " builds";
			}
			
		}));
		add(pullRequestsLink);
		
		add(new ListView<Map.Entry<Status, Long>>("statuses", 
				new LoadableDetachableModel<List<Map.Entry<Status, Long>>>() {

			@Override
			protected List<Map.Entry<Status, Long>> load() {
				return new ArrayList<>(getStats().entrySet());
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Map.Entry<Status, Long>> item) {
				Map.Entry<Status, Long> entry = item.getModelObject();
				BuildQuery query = new BuildQuery(
						new io.onedev.server.search.entity.build.StatusCriteria(entry.getKey()));
				PageParameters params = ProjectBuildsPage.paramsOf(getProject(), query.toString(), 0);
				Link<Void> statusLink = new BookmarkablePageLink<Void>("link", ProjectBuildsPage.class, params);
				String statusName = entry.getKey().toString();
				statusLink.add(new Label("label", entry.getValue() + " " + statusName));
				
				String cssClass;
				switch (entry.getKey()) {
				case SUCCESSFUL:
					cssClass = "link-success";
					break;
				case FAILED:
				case TIMED_OUT:
				case CANCELLED:
					cssClass = "link-danger";
					break;
				default:
					cssClass = "link-warning";
				}
				statusLink.add(AttributeAppender.append("class", cssClass));
				item.add(statusLink);
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
	
	private Map<Status, Long> getStats() {
		return statsModel.getObject();
	}

}
