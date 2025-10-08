package io.onedev.server.web.component.build.minilist;

import static io.onedev.server.web.translation.Translation._T;

import java.util.List;

import org.jspecify.annotations.Nullable;

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
import io.onedev.server.service.BuildService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.web.component.build.status.BuildStatusIcon;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;

public class MiniBuildListPanel extends GenericPanel<List<Build>> {

	public MiniBuildListPanel(String id, IModel<List<Build>> model) {
		super(id, model);
	}

	protected List<Build> getBuilds() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (!getBuilds().isEmpty()) {
			Fragment fragment = new Fragment("content", "hasBuildsFrag", this);
			fragment.add(newListLink("showInList"));
			
			fragment.add(new ListView<Build>("builds", getModel()) {

				@Override
				protected void populateItem(ListItem<Build> item) {
					Build build = item.getModelObject();
					
					Link<Void> buildLink = new BookmarkablePageLink<Void>("build", 
							BuildDashboardPage.class, BuildDashboardPage.paramsOf(build));
					
					Long buildId = build.getId();
					buildLink.add(new BuildStatusIcon("status", new LoadableDetachableModel<Status>() {

						@Override
						protected Status load() {
							return OneDev.getInstance(BuildService.class).load(buildId).getStatus();
						}
						
					}));

					StringBuilder builder = new StringBuilder("#" + build.getNumber());
					if (build.getVersion() != null)
						builder.append(" (" + build.getVersion() + ")");
					buildLink.add(new Label("title", builder.toString())); 
					item.add(buildLink);

					if (build.equals(getActiveBuild()))
						item.add(AttributeAppender.append("class", "active"));
				}
				
			});
			add(fragment);
		} else {
			add(new Label("content", _T("No builds")).add(AttributeAppender.append("class", "no-builds font-italic mx-5 my-4 text-nowrap")));
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MiniBuildListCssResourceReference()));
	}

	protected Component newListLink(String componentId) {
		return new WebMarkupContainer(componentId).setVisible(false);
	}

	@Nullable
	protected Build getActiveBuild() {
		return null;
	}
	
}
