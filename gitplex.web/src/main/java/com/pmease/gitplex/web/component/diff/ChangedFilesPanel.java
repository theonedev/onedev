package com.pmease.gitplex.web.component.diff;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.git.Change;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public abstract class ChangedFilesPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final String fromRev;
	
	private final String toRev;
	
	public ChangedFilesPanel(String id, IModel<Repository> repoModel, String fromRev, String toRev) {
		super(id);
		
		this.repoModel = repoModel;
		this.fromRev = fromRev;
		this.toRev = toRev;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<Change> changes = repoModel.getObject().getChanges(fromRev, toRev);
		Fragment fragment;
		if (!changes.isEmpty()) {
			fragment = new Fragment("content", "changesFrag", this);
			fragment.add(new ListView<Change>("changes", repoModel.getObject().getChanges(fromRev, toRev)) {

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
		} else {
			fragment = new Fragment("content", "noChangesFrag", this);
		}
		add(fragment);
	}

	protected abstract WebMarkupContainer newBlobLink(String id, Change change);

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

}
