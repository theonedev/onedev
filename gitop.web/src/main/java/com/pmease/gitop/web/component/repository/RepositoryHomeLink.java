package com.pmease.gitop.web.component.repository;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.common.wicket.component.link.LinkPanel;
import com.pmease.gitop.web.page.repository.source.RepositoryHomePage;

public class RepositoryHomeLink extends LinkPanel {
	private static final long serialVersionUID = 1L;

	private final IModel<Repository> repoModel;
	
	@SuppressWarnings("serial")
	public RepositoryHomeLink(String id, final IModel<Repository> repoModel) {
		super(id, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Repository repository = repoModel.getObject();
				return repository.getOwner().getName() + "/" + repository.getName();
			}
			
		});
		
		this.repoModel = repoModel;
	}

	@Override
	protected AbstractLink createLink(String id) {
		return new BookmarkablePageLink<Void>(id, 
				RepositoryHomePage.class, RepositoryHomePage.paramsOf(repoModel.getObject()));
	}

	@Override
	public void onDetach() {
		if (repoModel != null) {
			repoModel.detach();
		}
		
		super.onDetach();
	}
}
