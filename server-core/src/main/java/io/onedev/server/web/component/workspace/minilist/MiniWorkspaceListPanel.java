package io.onedev.server.web.component.workspace.minilist;

import static io.onedev.server.web.translation.Translation._T;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.Workspace.Status;
import io.onedev.server.web.component.workspace.status.WorkspaceStatusIcon;
import io.onedev.server.web.page.project.workspaces.detail.dashboard.WorkspaceDashboardPage;
import io.onedev.server.workspace.WorkspaceService;

public class MiniWorkspaceListPanel extends GenericPanel<List<Workspace>> {

	public MiniWorkspaceListPanel(String id, IModel<List<Workspace>> model) {
		super(id, model);
	}

	protected List<Workspace> getWorkspaces() {
		return getModelObject();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (!getWorkspaces().isEmpty()) {
			Fragment fragment = new Fragment("content", "hasWorkspacesFrag", this);
			fragment.add(newListLink("showInList"));

			fragment.add(new ListView<Workspace>("workspaces", getModel()) {

				@Override
				protected void populateItem(ListItem<Workspace> item) {
					Workspace workspace = item.getModelObject();

					Link<Void> workspaceLink = new BookmarkablePageLink<Void>("workspace",
							WorkspaceDashboardPage.class, WorkspaceDashboardPage.paramsOf(workspace));

					Long workspaceId = workspace.getId();
					workspaceLink.add(new WorkspaceStatusIcon("status", new LoadableDetachableModel<Status>() {

						@Override
						protected Status load() {
							return OneDev.getInstance(WorkspaceService.class).load(workspaceId).getStatus();
						}

					}));

					String title = "#" + workspace.getNumber() + " (" + workspace.getUser().getDisplayName() + ")";
					workspaceLink.add(new Label("title", title));
					item.add(workspaceLink);
				}

			});
			add(fragment);
		} else {
			add(new Label("content", _T("No workspaces")).add(AttributeAppender.append("class", "no-workspaces font-italic mx-5 my-4 text-nowrap")));
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MiniWorkspaceListCssResourceReference()));
	}

	protected Component newListLink(String componentId) {
		return new WebMarkupContainer(componentId).setVisible(false);
	}

}
