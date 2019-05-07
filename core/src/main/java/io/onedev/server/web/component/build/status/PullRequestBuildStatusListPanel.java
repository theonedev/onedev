package io.onedev.server.web.component.build.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
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
public class PullRequestBuildStatusListPanel extends GenericPanel<PullRequest> {

	public PullRequestBuildStatusListPanel(String id, IModel<PullRequest> model) {
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
				List<String> jobNames = getPullRequest().getTargetProject().getJobNames();
				Collections.sort(builds, new Comparator<PullRequestBuild>() {

					@Override
					public int compare(PullRequestBuild o1, PullRequestBuild o2) {
						int index1 = jobNames.indexOf(o1.getJobName());
						int index2 = jobNames.indexOf(o2.getJobName());
						if (index1 != index2)
							return index1 - index2;
						else if (!o1.getJobName().equals(o2.getJobName()))
							return o1.getJobName().compareTo(o2.getJobName());
						else if (o1.getBuild() != null && o2.getBuild() != null)
							return (int) (o1.getBuild().getNumber() - o2.getBuild().getNumber());
						else
							return 0;
					}
					
				});
				return builds;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<PullRequestBuild> item) {
				PullRequestBuild build = item.getModelObject();
				
				if (build.getBuild() != null) {
					item.add(new BuildStatusIcon("icon", new AbstractReadOnlyModel<Build>() {

						@Override
						public Build getObject() {
							return item.getModelObject().getBuild();
						}
						
					}));
					item.add(new Label("name", build.getBuild().getStatus().getDisplayName()));
					
					Link<Void> link = new BookmarkablePageLink<Void>("title", BuildLogPage.class, 
							BuildLogPage.paramsOf(build.getBuild(), null));
					StringBuilder builder = new StringBuilder("#" + build.getBuild().getNumber());
					if (build.getBuild().getVersion() != null)
						builder.append(" (" + build.getBuild().getVersion() + ")");
					builder.append(" : ").append(build.getBuild().getJobName());
					link.add(new Label("label", builder.toString())); 
					item.add(link);
				} else {
					WebMarkupContainer icon = new WebMarkupContainer("icon");
					icon.add(AttributeAppender.append("class", "build-status build-status-pending fa fa-fw"));
					icon.add(AttributeAppender.append("title", "Build is pending"));
					item.add(icon);
					item.add(new Label("name", "Pending"));
					
					WebMarkupContainer titleLink = new WebMarkupContainer("title") {

						@Override
						protected void onComponentTag(ComponentTag tag) {
							super.onComponentTag(tag);
							tag.setName("span");
						}
						
					};
					titleLink.add(new Label("label", build.getJobName()));
					item.add(titleLink);
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
		response.render(CssHeaderItem.forReference(new BuildStatusCssResourceReference()));
	}

}
