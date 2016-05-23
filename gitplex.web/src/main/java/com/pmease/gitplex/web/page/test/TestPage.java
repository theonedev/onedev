package com.pmease.gitplex.web.page.test;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import com.pmease.commons.wicket.assets.jqueryui.JQueryUIResourceReference;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.web.component.diff.revision.DiffMark;
import com.pmease.gitplex.web.page.base.BasePage;
import com.pmease.gitplex.web.page.depot.commit.CommitDetailPage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Depot depot = GitPlex.getInstance(DepotManager.class).load(1L);
		
		CommitDetailPage.State state = new CommitDetailPage.State();
		state.mark = new DiffMark("dir a.java", false, 0, 0, 1, 1);
		state.revision = "master";
		add(new BookmarkablePageLink<Void>("link", 
				CommitDetailPage.class, CommitDetailPage.paramsOf(depot, state)));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(JQueryUIResourceReference.INSTANCE));
	}

}
