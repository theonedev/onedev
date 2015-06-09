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
import org.eclipse.jgit.revwalk.RevCommit;

import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.component.MultilineLabel;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.commit.RepoCommitPage;

@SuppressWarnings("serial")
public class CommitMessagePanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final IModel<RevCommit> commitModel;
	
	public CommitMessagePanel(String id, IModel<Repository> repoModel, IModel<RevCommit> commitModel) {
		super(id);
		
		this.repoModel = repoModel;
		this.commitModel = commitModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		AbstractLink link = new BookmarkablePageLink<Void>("link",
				RepoCommitPage.class,
				RepoCommitPage.paramsOf(repoModel.getObject(), commitModel.getObject().name()));
		
		add(link);
		link.add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				// do not use jgit shortMessage here as it concatenates all lines in the first 
				// paragraph to cause detail message toggle inaccurate
				return StringUtils.substringBefore(commitModel.getObject().getFullMessage(), "\n").trim();
			}
		}));

		add(new MultilineLabel("detail", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				RevCommit commit = commitModel.getObject();
				return StringUtils.substringAfter(commit.getFullMessage(), "\n").trim();
			}
			
		}) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				RevCommit commit = commitModel.getObject();
				setVisible(StringUtils.substringAfter(commit.getFullMessage(), "\n").trim().length()!=0);
			}
		});
		
		WebMarkupContainer detailedToggle = new WebMarkupContainer("toggle") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				RevCommit commit = commitModel.getObject();
				setVisible(StringUtils.substringAfter(commit.getFullMessage(), "\n").trim().length()!=0);
			}
		};
		add(detailedToggle);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(CommitMessagePanel.class, "commit-message.css")));
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		commitModel.detach();
		
		super.onDetach();
	}
	
}
