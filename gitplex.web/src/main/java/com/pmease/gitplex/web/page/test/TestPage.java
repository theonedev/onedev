package com.pmease.gitplex.web.page.test;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.lang.TokenPosition;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.web.component.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.blobview.source.SourceViewPanel;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("reload") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				TestPage.this.replace(newSourceView());
				target.add(TestPage.this.get("sourceView"));
				target.appendJavaScript("$('.source-view').trigger('autofit', [1200, 800]);");
			}
			
		});
		
		add(newSourceView());
	}
	
	private Component newSourceView() {
		return new SourceViewPanel("sourceView", new BlobViewContext(new BlobIdent("master", "MAINTAINERS", FileMode.REGULAR_FILE.getBits())) {

			@Override
			public Repository getRepository() {
				return GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
			}

			@Override
			public TokenPosition getTokenPosition() {
				return null;
			}

			@Override
			public void onSelect(AjaxRequestTarget target, BlobIdent blobIdent,
					TokenPosition tokenPos) {
				
			}

			@Override
			public void onSearchComplete(AjaxRequestTarget target,
					List<QueryHit> hits) {
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(OnDomReadyHeaderItem.forScript("$('.source-view').trigger('autofit', [1200, 800]);"));
	}

}
