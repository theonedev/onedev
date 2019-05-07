package io.onedev.server.web.component.build.status;

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

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.model.Build;
import io.onedev.server.web.page.project.builds.detail.BuildLogPage;

@SuppressWarnings("serial")
public class StatusListPanel extends GenericPanel<List<Build>> {

	public StatusListPanel(String id, IModel<List<Build>> model) {
		super(id, model);
		
		add(new ListView<Build>("builds", new LoadableDetachableModel<List<Build>>() {

			@Override
			protected List<Build> load() {
				List<Build> builds = model.getObject();
				if (!builds.isEmpty()) {
					List<String> jobNames = builds.iterator().next().getProject().getJobNames();
					Collections.sort(builds, new Comparator<Build>() {

						@Override
						public int compare(Build o1, Build o2) {
							int index1 = jobNames.indexOf(o1.getJobName());
							int index2 = jobNames.indexOf(o2.getJobName());
							if (index1 != index2)
								return index1 - index2;
							else if (!o1.getJobName().equals(o2.getJobName()))
								return o1.getJobName().compareTo(o2.getJobName());
							else
								return (int) (o1.getNumber() - o2.getNumber());
						}
						
					});
				}
				return builds;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Build> item) {
				item.add(new BuildStatusIcon("icon", item.getModel()));
				item.add(new Label("name", item.getModel().getObject().getStatus().getDisplayName()));

				Build build = item.getModelObject();
				
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
