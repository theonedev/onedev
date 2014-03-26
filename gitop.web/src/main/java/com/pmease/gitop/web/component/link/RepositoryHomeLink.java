package com.pmease.gitop.web.component.link;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.common.wicket.component.link.LinkPanel;
import com.pmease.gitop.web.page.PageSpec;

public class RepositoryHomeLink extends LinkPanel {
	private static final long serialVersionUID = 1L;

	private final IModel<Repository> repoModel;
	
	@SuppressWarnings("serial")
	public RepositoryHomeLink(String id, final IModel<Repository> repoModel) {
		super(id, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Repository project = repoModel.getObject();
				return project.getOwner().getName() + "/" + project.getName();
			}
			
		});
		
		this.repoModel = repoModel;
	}

	@Override
	protected AbstractLink createLink(String id) {
		return PageSpec.newRepositoryHomeLink(id, repoModel.getObject());
	}

	@Override
	public void onDetach() {
		if (repoModel != null) {
			repoModel.detach();
		}
		
		super.onDetach();
	}
}
