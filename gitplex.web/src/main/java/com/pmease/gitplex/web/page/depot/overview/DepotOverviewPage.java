package com.pmease.gitplex.web.page.depot.overview;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.pmease.commons.git.Blob;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.wicket.component.markdownviewer.MarkdownViewer;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.CommitInfoManager;
import com.pmease.gitplex.core.manager.UrlManager;
import com.pmease.gitplex.web.component.depotfile.filelist.FileListPanel;
import com.pmease.gitplex.web.page.depot.DepotPage;
import com.pmease.gitplex.web.page.depot.branches.DepotBranchesPage;
import com.pmease.gitplex.web.page.depot.commit.DepotCommitsPage;
import com.pmease.gitplex.web.page.depot.tags.DepotTagsPage;

@SuppressWarnings("serial")
public class DepotOverviewPage extends DepotPage {

	private final IModel<BlobIdent> readmeModel = new LoadableDetachableModel<BlobIdent>() {

		@Override
		protected BlobIdent load() {
			try (	RevWalk revWalk = new RevWalk(getDepot().getRepository());
					TreeWalk treeWalk = new TreeWalk(getDepot().getRepository());) {
				RevCommit commit = revWalk.parseCommit(getDepot().getObjectId(getDepot().getDefaultBranch()));
				treeWalk.addTree(commit.getTree());
				while (treeWalk.next()) {
					String fileName = treeWalk.getNameString();
					String readme = StringUtils.substringBefore(fileName, ".");
					if (readme.equalsIgnoreCase(FileListPanel.README_NAME)) {
						return new BlobIdent(getDepot().getDefaultBranch(), 
								treeWalk.getPathString(), treeWalk.getRawMode(0));
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return null;
		}
		
	};
	
	public DepotOverviewPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		UrlManager urlManager = GitPlex.getInstance(UrlManager.class);
		Model<String> cloneUrlModel = Model.of(urlManager.urlFor(getDepot()));
		add(new TextField<String>("cloneUrl", cloneUrlModel));
		
		if (getDepot().getDescription() != null) {
			add(new MarkdownViewer("description", Model.of(getDepot().getDescription()), false));
		} else {
			add(new WebMarkupContainer("description").setVisible(false));
		}
		
		add(new BookmarkablePageLink<Void>("commitsLink", 
				DepotCommitsPage.class, DepotCommitsPage.paramsOf(getDepot())) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(GitPlex.getInstance(CommitInfoManager.class).getCommitCount(getDepot()) != 0);
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				CommitInfoManager commitInfoManager = GitPlex.getInstance(CommitInfoManager.class);
				add(new Label("count", commitInfoManager.getCommitCount(getDepot()) + " commits"));
			}
			
		});
		
		add(new BookmarkablePageLink<Void>("branchesLink", 
				DepotBranchesPage.class, DepotBranchesPage.paramsOf(getDepot())) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("count", getDepot().getRefs(Constants.R_HEADS).size() + " branches"));
			}
			
		});
		
		add(new BookmarkablePageLink<Void>("tagsLink", 
				DepotTagsPage.class, DepotTagsPage.paramsOf(getDepot())) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("count", getDepot().getRefs(Constants.R_TAGS).size() + " tags"));
			}
			
		});
		
		add(new MarkdownViewer("readme", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				Blob blob = depotModel.getObject().getBlob(readmeModel.getObject());
				Blob.Text text = blob.getText();
				if (text != null)
					return text.getContent();
				else
					return "This seems like a binary file!";
			}
			
		}, false) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(readmeModel.getObject() != null);
			}
			
		});
	}

	@Override
	protected void onDetach() {
		readmeModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				DepotOverviewPage.class, "depot-overview.css")));
	}

}
