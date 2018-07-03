package io.onedev.server.web.component.build;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;

@SuppressWarnings("serial")
public class BuildDetailPanel extends GenericPanel<List<Build>> {

	public BuildDetailPanel(String id, IModel<List<Build>> model) {
		super(id, model);
		
		add(new ListView<Build>("builds", getModel()) {

			@Override
			protected void populateItem(ListItem<Build> item) {
				Build build = item.getModelObject();

				WebMarkupContainer result = new WebMarkupContainer("result");
				item.add(result);
				
				if (build.getStatus() == Status.SUCCESS) {
					result.add(AttributeAppender.append("class", "success fa fa-check"));
					result.add(AttributeAppender.append("title", "Build is successful"));
				} else if (build.getStatus() == Status.ERROR) {
					result.add(AttributeAppender.append("class", "error fa fa-warning"));
					result.add(AttributeAppender.append("title", "Build is in error"));
				} else if (build.getStatus() == Status.FAILURE) {
					result.add(AttributeAppender.append("class", "failure fa fa-times"));
					result.add(AttributeAppender.append("title", "Build is failed"));
				} else {
					result.add(AttributeAppender.append("class", "running fa fa-circle"));
					result.add(AttributeAppender.append("title", "Build is running"));
				}
				
				item.add(new Label("configuration", build.getConfiguration().getName()));
				
				if (build.getDescription() != null) {
					if (build.getUrl() != null) {
						Fragment fragment = new Fragment("content", "linkFrag", BuildDetailPanel.this);
						ExternalLink link = new ExternalLink("link", build.getUrl());
						link.add(new Label("label", build.getDescription())); 
						fragment.add(link);
						item.add(fragment);
					} else {
						item.add(new Label("content", build.getDescription()));
					}
				} else {
					item.add(new WebMarkupContainer("content").setVisible(false));
				}
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
		response.render(CssHeaderItem.forReference(new BuildResourceReference()));
	}
	
}
