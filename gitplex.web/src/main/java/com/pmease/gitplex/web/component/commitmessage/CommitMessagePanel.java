package com.pmease.gitplex.web.component.commitmessage;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.git.Commit;
import com.pmease.commons.wicket.component.MultilineLabel;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.commit.RepoCommitPage;

@SuppressWarnings("serial")
public class CommitMessagePanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final IModel<Commit> commitModel;
	
	public CommitMessagePanel(String id, IModel<Repository> repoModel, IModel<Commit> commitModel) {
		super(id);
		
		this.repoModel = repoModel;
		this.commitModel = commitModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		AbstractLink link = new BookmarkablePageLink<Void>("link",
				RepoCommitPage.class,
				RepoCommitPage.paramsOf(repoModel.getObject(), commitModel.getObject().getHash()));
		
		add(link);
		link.add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return commitModel.getObject().getSubject();
			}
		}));

		add(new MultilineLabel("detail", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return commitModel.getObject().getBody();
			}
			
		}) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(commitModel.getObject().getBody() != null);
			}
		});
		
		WebMarkupContainer detailedToggle = new WebMarkupContainer("toggle") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(commitModel.getObject().getBody() != null);
			}
		};
		add(detailedToggle);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(CommitMessagePanel.class, "commit-message.css")));
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		commitModel.detach();
		
		super.onDetach();
	}
	
}
