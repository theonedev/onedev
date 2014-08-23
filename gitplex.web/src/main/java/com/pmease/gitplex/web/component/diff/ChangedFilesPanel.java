package com.pmease.gitplex.web.component.diff;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.git.Change;

@SuppressWarnings("serial")
public abstract class ChangedFilesPanel extends Panel {

	private final IModel<List<Change>> changesModel;
	
	public ChangedFilesPanel(String id, IModel<List<Change>> changesModel) {
		super(id);
		
		this.changesModel = changesModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<Change>("changes", changesModel) {

			@Override
			protected void populateItem(ListItem<Change> item) {
				Change change = item.getModelObject();
				WebMarkupContainer link = newBlobLink("link", change);
				link.add(new Label("label", change.getPath()));
				link.add(AttributeAppender.append("title", change.getHint()));
				item.add(link);
				item.add(AttributeAppender.append("class", " file " + change.getStatus().name().toLowerCase()));
			}
			
		});
	}

	protected abstract WebMarkupContainer newBlobLink(String id, Change change);

}
