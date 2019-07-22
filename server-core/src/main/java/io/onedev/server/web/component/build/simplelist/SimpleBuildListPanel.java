package io.onedev.server.web.component.build.simplelist;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.web.component.build.status.BuildStatusIcon;
import io.onedev.server.web.page.project.builds.detail.BuildLogPage;

@SuppressWarnings("serial")
public class SimpleBuildListPanel extends GenericPanel<List<Build>> {

	public SimpleBuildListPanel(String id, IModel<List<Build>> model) {
		super(id, model);
	}

	protected List<Build> getBuilds() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newListLink("showInList"));
		
		add(new ListView<Build>("builds", getModel()) {

			@Override
			protected void populateItem(ListItem<Build> item) {
				Build build = item.getModelObject();
				
				Long buildId = build.getId();
				item.add(new BuildStatusIcon("status", new LoadableDetachableModel<Status>() {

					@Override
					protected Status load() {
						return OneDev.getInstance(BuildManager.class).load(buildId).getStatus();
					}
					
				}));

				Link<Void> buildLink = new BookmarkablePageLink<Void>("title", 
						BuildLogPage.class, BuildLogPage.paramsOf(build, null));

				StringBuilder builder = new StringBuilder("#" + build.getNumber());
				if (build.getVersion() != null)
					builder.append(" (" + build.getVersion() + ")");
				buildLink.add(new Label("label", builder.toString())); 
				item.add(buildLink);
			}
			
		});
		add(new WebMarkupContainer("noBuilds") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuilds().isEmpty());
			}
			
		});
	}

	protected Component newListLink(String componentId) {
		return new WebMarkupContainer(componentId).setVisible(false);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SimpleBuildListCssResourceReference()));
	}
	
}
