package com.pmease.gitplex.web.page;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.git.DiffTreeNode;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.diff.BlobDiffInfo;
import com.pmease.gitplex.web.component.diff.BlobDiffPanel;
import com.pmease.gitplex.web.component.diff.DiffTreePanel;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private IModel<Repository> repoModel;
	
	public TestPage(PageParameters params) {
		super(params);
		
		repoModel = new LoadableDetachableModel<Repository>() {

			@Override
			protected Repository load() {
				return GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
			}
		};
		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new DiffTreePanel("test", repoModel, "master", "dev") {

			@Override
			protected Link<Void> newFileLink(String id, final DiffTreeNode node) {
				return new Link<Void>(id) {

					@Override
					public void onClick() {
						TestPage.this.replace(new BlobDiffPanel("diff", repoModel, new LoadableDetachableModel<BlobDiffInfo>() {

							@Override
							protected BlobDiffInfo load() {
								return BlobDiffInfo.from(repoModel.getObject().git(), node, "master", "dev");
							}
							
						}));
					}
					
				};
			}
			
		});
		
		add(new WebMarkupContainer("diff"));
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}
	
}
