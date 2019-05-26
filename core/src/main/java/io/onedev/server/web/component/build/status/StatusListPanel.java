package io.onedev.server.web.component.build.status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.Build;
import io.onedev.server.web.page.project.builds.detail.BuildLogPage;

@SuppressWarnings("serial")
public class StatusListPanel extends GenericPanel<Collection<Build>> {

	public StatusListPanel(String id, IModel<Collection<Build>> model) {
		super(id, model);
		
		add(new ListView<Build>("builds", new LoadableDetachableModel<List<Build>>() {

			@Override
			protected List<Build> load() {
				List<Build> builds = new ArrayList<>(model.getObject());
				builds.sort(Comparator.comparing(Build::getNumber));
				return builds;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Build> item) {
				Build build = item.getModelObject();
				
				item.add(new BuildStatusIcon("icon", build.getId(), true));

				Link<Void> buildLink = new BookmarkablePageLink<Void>("title", 
						BuildLogPage.class, BuildLogPage.paramsOf(build, null));

				StringBuilder builder = new StringBuilder("#" + build.getNumber());
				if (build.getVersion() != null)
					builder.append(" (" + build.getVersion() + ")");
				builder.append(" : ").append(build.getJobName());
				buildLink.add(new Label("label", builder.toString())); 
				item.add(buildLink);
			}
			
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BuildStatusCssResourceReference()));
	}
	
}
