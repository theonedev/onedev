package com.pmease.gitplex.web.page.repository.source.component;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.source.tree.SourceTreePage;

@SuppressWarnings("serial")
public class PathsBreadcrumb extends AbstractSourcePagePanel {

	public PathsBreadcrumb(String id, IModel<Repository> repositoryModel, 
			IModel<String> revisionModel, 
			IModel<List<String>> pathsModel) {
		super(id, repositoryModel, revisionModel, pathsModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BookmarkablePageLink<Void> homeLink = new BookmarkablePageLink<Void>("home", 
				SourceTreePage.class, 
				SourceTreePage.paramsOf(getRepo()).add(RepositoryPage.PARAM_OBJECT_ID, getRevision()));
		add(homeLink);
		homeLink.add(new Label("name", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getRepo().getName();
			}
		}));
		
		ListView<String> pathsView = new ListView<String>("paths", pathsModel) {

			@Override
			protected void populateItem(ListItem<String> item) {
				String path = item.getModelObject();
				
				BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>(
						"link", 
						SourceTreePage.class,
						SourceTreePage.paramsOf(getRepo(), 
													 getRevision(), 
													 getPaths().subList(0, item.getIndex() + 1)));
				
				item.add(link);
				link.add(new Label("name", path));
				if (item.getIndex() == getList().size() - 1) {
					item.add(AttributeAppender.append("class", "active"));
					link.setEnabled(false);
				}

			}
		};
		
		add(pathsView);
	}
}
