package io.onedev.server.web.component.build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.Build.Status;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestBuild;
import io.onedev.server.web.behavior.dropdown.DropdownHover;
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
				item.add(new Label("configuration", build.getConfiguration().getName()));
				
				WebMarkupContainer result = new WebMarkupContainer("result");
				String tooltip;
				if (build.getBuild() != null) {
					if (build.getBuild().getDescription() != null) {
						if (build.getBuild().getUrl() != null) {
							Fragment fragment = new Fragment("content", "linkFrag", PullRequestBuildsPanel.this);
							ExternalLink link = new ExternalLink("link", build.getBuild().getUrl());
							link.add(new Label("label", build.getBuild().getDescription()));
							fragment.add(link);
							item.add(fragment);
						} else {
							item.add(new Label("content", build.getBuild().getDescription()));
						}
					} else {
						item.add(new Label("content").setVisible(false));
					}
					if (build.getBuild().getStatus() == Status.SUCCESS) {
						result.add(AttributeAppender.append("class", "success fa fa-check"));
						tooltip = "Build is successful";
					} else if (build.getBuild().getStatus() == Status.ERROR) {
						result.add(AttributeAppender.append("class", "error fa fa-warning"));
						tooltip = "Build is in error";
					} else if (build.getBuild().getStatus() == Status.FAILURE) {
						result.add(AttributeAppender.append("class", "failure fa fa-times"));
						tooltip = "Build is failed";
					} else {
						result.add(AttributeAppender.append("class", "running fa fa-circle"));
						tooltip = "Build is running";
					}
				} else {
					item.add(new Label("content").setVisible(false));
					result.add(AttributeAppender.append("class", "awaiting fa fa-clock-o"));
					tooltip = "Waiting for build";
				}
				
				result.add(new DropdownHover() {
					
					@Override
					protected Component newContent(String id) {
						return new Label(id, tooltip).add(AttributeAppender.append("class", "build-result"));
					}
					
				});
				
				item.add(result);
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
		response.render(JavaScriptHeaderItem.forReference(new BuildResourceReference()));
	}

}
