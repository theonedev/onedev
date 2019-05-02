package io.onedev.server.web.component.build.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestBuild;
import io.onedev.server.web.page.project.builds.detail.BuildLogPage;
import io.onedev.server.web.websocket.PageDataChanged;

@SuppressWarnings("serial")
public class PullRequestBuildsPanel extends GenericPanel<PullRequest> {

	public PullRequestBuildsPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}

	private PullRequest getPullRequest() {
		return getModelObject();
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (event.getPayload() instanceof PageDataChanged && isVisibleInHierarchy()) {
			PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
			pageDataChanged.getHandler().add(this);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<PullRequestBuild>("builds", new LoadableDetachableModel<List<PullRequestBuild>>() {

			@Override
			protected List<PullRequestBuild> load() {
				List<PullRequestBuild> builds = new ArrayList<>(getPullRequest().getBuilds());
				Collections.sort(builds, new Comparator<PullRequestBuild>() {

					@Override
					public int compare(PullRequestBuild o1, PullRequestBuild o2) {
						return (int)(o1.getId() - o2.getId());
					}
					
				});
				return builds;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<PullRequestBuild> item) {
				PullRequestBuild build = item.getModelObject();
				item.add(new Label("job", build.getJobName()));
				
				if (build.getBuild() != null) {
					item.add(new BuildStatusIcon("status", new AbstractReadOnlyModel<Build>() {

						@Override
						public Build getObject() {
							return item.getModelObject().getBuild();
						}
						
					}));
					
					Link<Void> link = new BookmarkablePageLink<Void>("number", BuildLogPage.class, BuildLogPage.paramsOf(build.getBuild(), null));
					link.add(new Label("label", "#" + build.getBuild().getNumber()));
					item.add(link);
				} else {
					WebMarkupContainer status = new WebMarkupContainer("status");
					status.add(AttributeAppender.append("class", "pending fa fa-clock-o"));
					status.add(AttributeAppender.append("title", "Build pending"));
					item.add(status);
					WebMarkupContainer description = new WebMarkupContainer("version");
					description.add(new WebMarkupContainer("label"));
					item.add(description.setVisible(false));
				}
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!getPullRequest().getBuilds().isEmpty());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BuildCssResourceReference()));
	}

}
