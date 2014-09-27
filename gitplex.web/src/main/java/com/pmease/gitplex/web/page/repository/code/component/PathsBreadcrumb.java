package com.pmease.gitplex.web.page.repository.code.component;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.git.PathUtils;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.code.tree.RepoTreePage;

@SuppressWarnings("serial")
public class PathsBreadcrumb extends Panel {

	private final IModel<Repository> repoModel;
	
	private final String currentRevision;
	
	private final String currentPath;
	
	public PathsBreadcrumb(String id, IModel<Repository> repoModel, String currentRevision, String currentPath) {
		super(id);
		
		this.repoModel = repoModel;
		this.currentRevision = currentRevision;
		this.currentPath = currentPath;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BookmarkablePageLink<Void> homeLink = new BookmarkablePageLink<Void>("home", 
				RepoTreePage.class, 
				RepositoryPage.paramsOf(repoModel.getObject(), currentRevision));
		add(homeLink);
		homeLink.add(new Label("name", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return repoModel.getObject().getName();
			}
		}));
		
		ListView<String> pathsView = new ListView<String>("paths", PathUtils.split(currentPath)) {

			@Override
			protected void populateItem(ListItem<String> item) {
				String path = item.getModelObject();
				
				BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>(
						"link", 
						RepoTreePage.class,
						RepoTreePage.paramsOf(repoModel.getObject(), 
													 currentRevision, 
													 PathUtils.join(PathUtils.split(currentPath).subList(0, item.getIndex() + 1))));
				
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

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}
	
}
