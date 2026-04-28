package io.onedev.server.web.component.project.stats.workspace;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.Workspace.Status;
import io.onedev.server.search.entity.workspace.ActiveCriteria;
import io.onedev.server.search.entity.workspace.InactiveCriteria;
import io.onedev.server.search.entity.workspace.PendingCriteria;
import io.onedev.server.search.entity.workspace.WorkspaceQuery;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.page.project.workspaces.ProjectWorkspacesPage;

public class WorkspaceStatsPanel extends Panel {

	private final IModel<Project> projectModel;
	
	private final IModel<Map<Status, Long>> statsModel;
	
	public WorkspaceStatsPanel(String id, IModel<Project> projectModel, IModel<Map<Status, Long>> statsModel) {
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
		
		PageParameters params = ProjectWorkspacesPage.paramsOf(getProject(), 0);
		Link<Void> workspacesLink = new BookmarkablePageLink<Void>("workspaces", ProjectWorkspacesPage.class, params);
		workspacesLink.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return getTotalCount() + " " + _T("workspaces");
			}
			
		}));
		add(workspacesLink);
		
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
				Criteria<Workspace> criteria;
				if (entry.getKey() == Status.PENDING) {
					criteria = new PendingCriteria();
				} else if (entry.getKey() == Status.ACTIVE) {
					criteria = new ActiveCriteria();
				} else {
					criteria = new InactiveCriteria();
				}
				WorkspaceQuery query = new WorkspaceQuery(criteria);
				PageParameters params = ProjectWorkspacesPage.paramsOf(getProject(), query.toString(), 0);
				Link<Void> statusLink = new BookmarkablePageLink<Void>("link", ProjectWorkspacesPage.class, params);
				String statusName = entry.getKey().toString();
				statusLink.add(new Label("label", entry.getValue() + " " + _T(statusName)));
				
				String cssClass;
				switch (entry.getKey()) {
				case ACTIVE:
					cssClass = "link-success";
					break;
				case INACTIVE:
					cssClass = "link-danger";
					break;
				default:
					cssClass = "link-warning";
					break;
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
