package io.onedev.server.web.component.build;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Build;

@SuppressWarnings("serial")
public class BuildDetailPanel extends GenericPanel<List<Build>> {

	public BuildDetailPanel(String id, IModel<List<Build>> model) {
		super(id, model);
		
		add(new ListView<Build>("builds", getModel()) {

			@Override
			protected void populateItem(ListItem<Build> item) {
				item.add(new BuildStatusIcon("status", item.getModel()));

				Build build = item.getModelObject();
				item.add(new Label("configuration", build.getConfiguration().getName()));
				
				ExternalLink link = new ExternalLink("name", build.getUrl());
				link.add(new Label("label", build.getName())); 
				item.add(link);
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
		response.render(CssHeaderItem.forReference(new BuildCssResourceReference()));
	}
	
}
