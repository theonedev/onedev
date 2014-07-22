package com.pmease.gitplex.web.page.repository.info.code.component;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;

import com.google.common.base.Joiner;
import com.pmease.gitplex.web.page.repository.info.RepoInfoPanel;
import com.pmease.gitplex.web.page.repository.info.RepositoryInfoPage;
import com.pmease.gitplex.web.page.repository.info.code.tree.RepoTreePage;

@SuppressWarnings("serial")
public class PathsBreadcrumb extends RepoInfoPanel {

	public PathsBreadcrumb(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BookmarkablePageLink<Void> homeLink = new BookmarkablePageLink<Void>("home", 
				RepoTreePage.class, 
				RepositoryInfoPage.paramsOf(getRepository(), getRevision(), null));
		add(homeLink);
		homeLink.add(new Label("name", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getRepository().getName();
			}
		}));
		
		ListView<String> pathsView = new ListView<String>("paths", getObjPathSegments()) {

			@Override
			protected void populateItem(ListItem<String> item) {
				String path = item.getModelObject();
				
				BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>(
						"link", 
						RepoTreePage.class,
						RepoTreePage.paramsOf(getRepository(), 
													 getRevision(), 
													 Joiner.on("/").join(getObjPathSegments().subList(0, item.getIndex() + 1))));
				
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
